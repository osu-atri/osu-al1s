/*
 * Copyright 2024 Astro angelfish
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package moe.orangemc.osu.al1s.multiplayer;

import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.chat.ChatManager;
import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.multiplayer.*;
import moe.orangemc.osu.al1s.api.mutltiplayer.*;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.ruleset.PlayResult;
import moe.orangemc.osu.al1s.api.ruleset.PlayScore;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.beatmap.BeatmapImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.ChatManagerImpl;
import moe.orangemc.osu.al1s.chat.OsuChannelImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoomImpl extends OsuChannelImpl implements MultiplayerRoom {
    private static final Pattern ROOM_CREATION_PATTERN = Pattern.compile("Created the tournament match https://osu.ppy.sh/mp/(\\d+).*");
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Inject
    private OsuBotImpl manager;
    @Inject
    private EventBus eventBus;
    @Inject
    private ChatManager chatManager;
    @Inject
    private Scheduler scheduler;

    private final int id;
    private String name;
    private String password;

    private final Map<User, UserState> playerStates = new ConcurrentHashMap<>();
    private @Nullable User host;

    private WinCondition winCondition = WinCondition.SCORE;
    private TeamMode teamMode = TeamMode.HEAD_TO_HEAD;

    private BeatmapImpl currentBeatmap;
    private Set<Mod> enforcedMods = new HashSet<>();
    private Ruleset currentRuleset = Ruleset.OSU;

    private long lastActTimer;
    private boolean alive = true;

    public RoomImpl(String roomName) {
        UserImpl banchobot = UserImpl.get(((ChatManagerImpl) chatManager).getServerBotName());
        banchobot.clearUnprocessedMessages();
        banchobot.sendMessage("!mp make " + roomName);
        CompletableFuture<String> response = new CompletableFuture<>();
        banchobot.pollServerMessages((msg) -> {
            response.complete(SneakyExceptionHelper.call(msg::removeFirst));
        });
        Matcher matcher = ROOM_CREATION_PATTERN.matcher(response.join());
        if (!matcher.find()) {
            throw new IllegalStateException("Bad BanchoBot");
        }

        String rawId = matcher.group(1);
        this.id = Integer.parseInt(rawId);
        this.name = roomName;

        SecureRandom random = new SecureRandom();
        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            passwordBuilder.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        this.password = passwordBuilder.toString();
        setPassword(password);
        lastActTimer = System.currentTimeMillis();

        scheduler.runTaskTimer(() -> manager.execute(this::refreshState), 2, 2, TimeUnit.MINUTES);
    }

    public RoomImpl(int roomId) {
        this.id = roomId;
        this.refreshState();

        scheduler.runTaskTimer(() -> manager.execute(this::refreshState), 2, 2, TimeUnit.MINUTES);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.sendMessage("!mp name " + name);
        this.name = name;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String password) {
        this.sendMessage("!mp password " + password);
        this.password = password;
    }

    @Override
    public void refreshState() {
        this.playerStates.clear();
        this.clearUnprocessedMessages();
        this.sendMessage("!mp settings");
        this.pollServerMessages(msgToNow -> msgToNow.removeIf(this::processRoomStateUpdate));
    }

    private void refreshRoomMetadata(Matcher matcher) {
        int id = Integer.parseInt(matcher.group(2));
        if (id != this.id) {
            throw new IllegalArgumentException("Room ID mismatch");
        }

        this.name = matcher.group(1);
    }

    private void refreshBeatmapMetadata(Matcher matcher) {
        int beatmapId = Integer.parseInt(matcher.group(1));
        this.currentBeatmap = BeatmapImpl.get(beatmapId);
    }

    private void refreshModeMetadata(Matcher matcher) {
        String teamModeStr = matcher.group(1);
        String winConditionStr = matcher.group(2);

        this.teamMode = TeamMode.fromString(teamModeStr);
        this.winCondition = WinCondition.fromString(winConditionStr);
    }

    private void refreshPlayerSlot(Matcher matcher) {
        int slot = Integer.parseInt(matcher.group(1));
        String rawWaitingStatus = matcher.group(2);
        int userId = Integer.parseInt(matcher.group(3));
        String modsStr = matcher.group(5);

        User user = UserImpl.get(userId);
        UserState state = new UserState(user);
        state.slot = slot;
        state.waitStatus = PlayerWaitStatus.fromString(rawWaitingStatus);

        String[] extraStrings = modsStr.split("\\s*/\\s*");
        Pattern teamMatcher = Pattern.compile("Team (Red|Blue)");
        for (String extra : extraStrings) {
            if (extra.equals("Host")) {
                this.host = user;
                continue;
            }

            Matcher teamMatch = teamMatcher.matcher(extra);
            if (teamMatch.find()) {
                state.team = MultiplayerTeam.fromString(teamMatch.group(1));
                continue;
            }

            String[] modStrings = extra.split("\\s*,\\s*");
            for (String modStr : modStrings) {
                state.mods.add(Mod.fromString(modStr));
            }
        }

        this.playerStates.put(user, state);
    }

    @Override
    public Map<Integer, User> getPlayers() {
        Map<Integer, User> slotMap = new HashMap<>();
        for (Map.Entry<User, UserState> entry : this.playerStates.entrySet()) {
            slotMap.put(entry.getValue().slot, entry.getKey());
        }
        return slotMap;
    }

    @Override
    public void kickPlayer(User user) {
        this.sendMessage("!mp kick " + user.getUsername());
        this.playerStates.remove(user);
    }

    @Override
    public void banPlayer(User user) {
        this.sendMessage("!mp ban " + user.getUsername());
        this.playerStates.remove(user);
    }

    @Override
    public void invitePlayer(User user) {
        this.sendMessage("!mp invite #" + user.getId());
    }

    private void ensureUserInRoom(User user) {
        if (!this.playerStates.containsKey(user)) {
            throw new IllegalArgumentException("User is not in the room");
        }
    }

    @Override
    public int getPlayerSlot(User user) {
        ensureUserInRoom(user);

        return this.playerStates.get(user).slot;
    }

    @Override
    public void movePlayer(User user, int slot) {
        ensureUserInRoom(user);

        this.playerStates.get(user).setSlot(slot);
    }

    @Override
    public PlayerWaitStatus getPlayerWaitStatus(User user) {
        return this.playerStates.get(user).waitStatus;
    }

    @Override
    public @Nullable MultiplayerTeam getTeam(User user) {
        ensureUserInRoom(user);

        if (!teamMode.isTeammed()) {
            return null;
        }

        return this.playerStates.get(user).team;
    }

    @Override
    public void setTeam(User user, MultiplayerTeam team) {
        ensureUserInRoom(user);

        if (!teamMode.isTeammed()) {
            throw new IllegalStateException("Room is not team-based");
        }

        this.playerStates.get(user).setTeam(team);
    }

    @Nullable
    @Override
    public User getHost() {
        return this.host;
    }

    @Override
    public void setHost(@Nullable User user) {
        if (user == null) {
            this.sendMessage("!mp clearhost");
        } else {
            this.sendMessage("!mp host " + user.getUsername());
        }
        this.host = user;
    }

    @Override
    public BeatmapImpl getCurrentBeatmap() {
        return currentBeatmap;
    }

    @Override
    public void setCurrentBeatmap(Beatmap beatmap, Ruleset ruleset) {
        this.sendMessage("!mp map " + beatmap.getId() + " " + ruleset.getId());
        this.currentBeatmap = (BeatmapImpl) beatmap;
        this.currentRuleset = ruleset;
    }

    @Override
    public void setCurrentBeatmap(Beatmap currentBeatmap) {
        this.sendMessage("!mp map " + currentBeatmap.getId());
        this.currentBeatmap = (BeatmapImpl) currentBeatmap;
    }

    @Override
    public Set<Mod> getRoomMods() {
        return Collections.unmodifiableSet(enforcedMods);
    }

    @Override
    public void setRoomMods(Set<Mod> mods) {
        boolean freeMod = mods.contains(Mod.FREE_MOD);

        this.enforcedMods = mods;
        if (freeMod) {
            mods.remove(Mod.FREE_MOD);
        }

        int modMask = 0;
        for (Mod mod : mods) {
            modMask |= mod.getValue();
        }
        this.sendMessage("!mp mods " + modMask + (freeMod ? " Freemod" : ""));
    }

    @Override
    public void setRoomMods(Mod... mods) {
        this.setRoomMods(Set.of(mods));
    }

    @Override
    public Set<Mod> getUserMods(User user) {
        ensureUserInRoom(user);
        Set<Mod> modSet = new HashSet<>(this.enforcedMods);
        if (modSet.contains(Mod.FREE_MOD)) {
            modSet.remove(Mod.FREE_MOD);
            modSet.addAll(this.playerStates.get(user).mods);
        }
        return modSet;
    }

    @Override
    public Set<User> getReferees() {
        this.clearUnprocessedMessages();
        this.sendMessage("!mp listrefs");
        Set<User> referees = new HashSet<>();
        this.pollServerMessages((msgToNow) -> {
            while (!msgToNow.contains("Match referees:")) {
                msgToNow = this.waitForNewServerMessages();
            }

            for (Iterator<String> iterator = msgToNow.iterator(); iterator.hasNext(); ) {
                String msg = iterator.next();
                if (!(msg.equals("Match referees:"))) {
                    continue;
                }

                if (!(msg.matches("^[0-9a-zA-Z \\[\\]\\-_]{1,15}$"))) {
                    break;
                }

                try {
                    referees.add(UserImpl.get(msg));
                    iterator.remove();
                } catch (Exception _) {

                }
            }
        });
        return referees;
    }

    @Override
    public void addReferee(User user) {
        this.sendMessage("!mp addref " + user.getUsername());
    }

    @Override
    public void removeReferee(User user) {
        this.sendMessage("!mp removeref " + user.getUsername());
    }

    @Override
    public TeamMode getTeamMode() {
        return teamMode;
    }

    @Override
    public WinCondition getWinCondition() {
        return winCondition;
    }

    @Override
    public void setProperties(TeamMode teamMode, WinCondition winCondition) {
        this.sendMessage("!mp set " + teamMode.getId() + " " + winCondition.getId());

        if (this.teamMode.isTeammed() ^ teamMode.isTeammed()) {
            refreshState();
        }
        this.teamMode = teamMode;
        this.winCondition = winCondition;
    }

    public boolean isInGame() {
        return this.playerStates.values().stream().anyMatch(state -> state.waitStatus == PlayerWaitStatus.IN_GAME);
    }

    @Override
    public void start(int delay) {
        this.sendMessage("!mp start " + delay);
    }

    @Override
    public void abort() {
        this.sendMessage("!mp abort");
    }

    @Override
    public void close() {
        if (!this.alive) {
            return;
        }

        this.sendMessage("!mp close");
        this.alive = false;
    }

    @Override
    public @NotNull OsuBot getManagingBot() {
        return manager;
    }

    private boolean processRoomStateUpdate(String message) {
        message = message.replaceAll("\\s+", " ");
        RoomSettingMessagePattern currentPattern = RoomSettingMessagePattern.findMatchingPattern(message);

        if (currentPattern != null) {
            Matcher matcher = currentPattern.getPattern().matcher(message);
            if (!matcher.matches()) {
                return false;
            }

            switch (currentPattern) {
                case ROOM_NAME_AND_LINK -> {
                    refreshRoomMetadata(matcher);
                }
                case BEATMAP -> refreshBeatmapMetadata(matcher);
                case MODE -> refreshModeMetadata(matcher);
                case PLAYER_COUNT -> {
                }
                case SLOTS -> refreshPlayerSlot(matcher);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void processServerMessages(List<String> messages) {
        this.lastActTimer = System.currentTimeMillis();
        for (String message : messages) {
            BanchoMessagePattern matching = BanchoMessagePattern.findMatchingPattern(message);
            if (matching == null) {
                continue;
            }

            Matcher matcher = matching.getPattern().matcher(message);
            if (!matcher.find()) {
                continue;
            }
            switch (matching) {
                case JOIN_ROOM -> {
                    String username = matcher.group(1);
                    int slot = Integer.parseInt(matcher.group(2));
                    User user = UserImpl.get(username);
                    this.playerStates.put(user, new UserState(user));
                    UserMoveToSlotEvent event = new UserJoinRoomEvent(this, user, slot);
                    this.eventBus.fire(event);

                    if (event.getSlot() != slot) {
                        this.movePlayer(user, event.getSlot());
                    }
                }
                case MOVE -> {
                    String username = matcher.group(1);
                    int slot = Integer.parseInt(matcher.group(2));
                    User user = UserImpl.get(username);
                    UserMoveToSlotEvent event = new UserMoveToSlotEvent(this, user, slot);
                    this.eventBus.fire(event);

                    if (event.getSlot() != slot) {
                        this.movePlayer(user, event.getSlot());
                    }
                }
                case TEAM_SWITCH -> {
                    String username = matcher.group(1);
                    MultiplayerTeam team = MultiplayerTeam.fromString(matcher.group(2));
                    User user = UserImpl.get(username);
                    UserSwitchTeamEvent event = new UserSwitchTeamEvent(this, user, team);
                    this.eventBus.fire(event);

                    if (event.isCancelled()) {
                        this.setTeam(user, getTeam(user));
                        continue;
                    }
                    this.setTeam(user, event.getTeam());
                }
                case LEAVE -> {
                    String username = matcher.group(1);
                    User user = UserImpl.get(username);
                    this.playerStates.remove(user);
                }
                case FINISHED_PLAYING -> {
                    String username = matcher.group(1);
                    int score = Integer.parseInt(matcher.group(2));
                    PlayResult result = matcher.group(3).equals("PASSED") ? PlayResult.PASSED : PlayResult.FAILED;
                    User user = UserImpl.get(username);
                    if (!this.playerStates.containsKey(user)) {
                        System.out.println("User " + user.getUsername() + " not in room");
                        continue;
                    }
                    this.eventBus.fire(new PlayerFinishPlayEvent(this, user, new PlayScore(result, this.currentBeatmap.getMode() == Ruleset.OSU ? this.currentRuleset : this.currentBeatmap.getMode(), this.currentBeatmap, score, this.playerStates.get(user).mods, 0, 0, 0, 0, 0, 0, 0)));

                    this.playerStates.get(user).waitStatus = PlayerWaitStatus.NOT_READY;
                    this.playerStates.get(user).lastScore = score;
                }
                case BEATMAP_CHANGED -> {
                    int beatmapId = Integer.parseInt(matcher.group(4));
                    BeatmapImpl newBeatmap = BeatmapImpl.get(beatmapId);

                    BeatmapChangeEvent event = new BeatmapChangeEvent(this, newBeatmap);
                    this.eventBus.fire(event);
                    if (event.isCancelled()) {
                        this.setCurrentBeatmap(currentBeatmap);
                        continue;
                    }
                }
                case HOST_CHANGED -> {
                    String username = matcher.group(1);
                    User user = UserImpl.get(username);

                    HostChangeEvent event = new HostChangeEvent(this, this.host, user);
                    this.eventBus.fire(event);

                    if (event.isCancelled()) {
                        this.setHost(getHost());
                        continue;
                    }

                    this.host = user;
                }
                case ALL_READY -> {
                    for (UserState state : this.playerStates.values()) {
                        state.waitStatus = PlayerWaitStatus.READY;
                    }
                    this.eventBus.fire(new AllReadyEvent(this));
                }
                case MATCH_STARTED -> {
                    for (UserState state : this.playerStates.values()) {
                        if (state.waitStatus == PlayerWaitStatus.NO_MAP) {
                            continue;
                        }
                        state.waitStatus = PlayerWaitStatus.IN_GAME;
                    }
                    this.eventBus.fire(new MatchStartEvent(this));
                }
                case MATCH_FINISHED -> {
                    for (UserState state : this.playerStates.values()) {
                        state.waitStatus = PlayerWaitStatus.NOT_READY;
                    }

                    this.eventBus.fire(new MatchEndEvent(this));
                }
            }
        }
    }

    public boolean isActive() {
        return this.alive && (Duration.of(System.currentTimeMillis() - lastActTimer, ChronoUnit.MILLIS).toMinutes() < 5 || !this.playerStates.isEmpty());
    }

    public boolean isAlive() {
        return this.alive;
    }

    private class UserState {
        private int slot;
        private MultiplayerTeam team;
        private final Set<Mod> mods = new HashSet<>();
        private PlayerWaitStatus waitStatus = PlayerWaitStatus.NOT_READY;
        private final User user;
        private int lastScore = 0;

        public UserState(User user) {
            this.user = user;
        }

        public void setSlot(int slot) {
            this.slot = slot;
            RoomImpl.this.sendMessage("!mp move " + user.getUsername() + " " + slot);
        }

        public void setTeam(MultiplayerTeam team) {
            this.team = team;
            RoomImpl.this.sendMessage("!mp team " + user.getUsername() + " " + team.getId());
        }
    }
}
