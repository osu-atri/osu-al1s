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
import moe.orangemc.osu.al1s.api.event.multiplayer.game.*;
import moe.orangemc.osu.al1s.api.mutltiplayer.*;
import moe.orangemc.osu.al1s.api.ruleset.*;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.beatmap.BeatmapImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.ChatManagerImpl;
import moe.orangemc.osu.al1s.chat.OsuChannelImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.multiplayer.chat.BanchoMessagePattern;
import moe.orangemc.osu.al1s.multiplayer.chat.RoomSettingMessagePattern;
import moe.orangemc.osu.al1s.multiplayer.web.MatchRequestAPI;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchRoomImpl extends OsuChannelImpl implements MatchRoom {
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
    @Inject
    private MatchRequestAPI requestAPI;

    private final int id;
    private final MatchRoomState roomState = new MatchRoomState(this);

    public MatchRoomImpl(String roomName) {
        UserImpl banchobot = UserImpl.get(((ChatManagerImpl) chatManager).getServerBotName());
        banchobot.clearUnprocessedMessages();
        banchobot.sendMessage("!mp make " + roomName);
        CompletableFuture<String> response = new CompletableFuture<>();
        banchobot.pollServerMessages((msg) -> {
            response.complete(SneakyExceptionHelper.call(msg::removeFirst));
        });
        Matcher matcher = ROOM_CREATION_PATTERN.matcher(response.join());
        if (!matcher.find()) {
            throw new IllegalStateException("Non-standard BanchoBot");
        }

        String rawId = matcher.group(1);
        this.id = Integer.parseInt(rawId);
        this.roomState.setName(roomName);

        SecureRandom random = new SecureRandom();
        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            passwordBuilder.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        String password = passwordBuilder.toString();
        setPassword(password);

        scheduler.runTaskTimer(() -> manager.execute(this::refreshState), 2, 2, TimeUnit.MINUTES);
    }

    public MatchRoomImpl(int roomId) {
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
        return this.roomState.getName();
    }

    @Override
    public void setName(String name) {
        this.sendMessage("!mp name " + name);
        this.roomState.setName(name);
    }

    @Override
    public String getPassword() {
        return this.roomState.getPassword();
    }

    @Override
    public void setPassword(String password) {
        this.sendMessage("!mp password " + password);
        roomState.setPassword(password);
    }

    @Override
    public void refreshState() {
        this.clearUnprocessedMessages();
        this.sendMessage("!mp settings");
        this.roomState.scheduleStateUpdate(state -> state.getPlayerStates().clear());
        this.pollServerMessages(msgToNow -> msgToNow.removeIf(this::processRoomStateUpdate));
        this.roomState.executeStateUpdate();
    }

    @Override
    public void refreshHistory() {

    }

    private void refreshRoomMetadata(Matcher matcher) {
        int id = Integer.parseInt(matcher.group(2));
        if (id != this.id) {
            throw new IllegalArgumentException("Room ID mismatch");
        }

        this.roomState.setName(matcher.group(1));
    }

    private void refreshBeatmapMetadata(Matcher matcher) {
        int beatmapId = Integer.parseInt(matcher.group(1));
        this.roomState.scheduleStateUpdate(state -> state.setCurrentBeatmap(BeatmapImpl.get(beatmapId)));
    }

    private void refreshModeMetadata(Matcher matcher) {
        String teamModeStr = matcher.group(1);
        String winConditionStr = matcher.group(2);

        this.roomState.scheduleStateUpdate(state -> state.setProperties(TeamMode.fromString(teamModeStr), WinCondition.fromString(winConditionStr)));
    }

    private void refreshPlayerSlot(Matcher matcher) {
        int slot = Integer.parseInt(matcher.group(1));
        String rawWaitingStatus = matcher.group(2);
        int userId = Integer.parseInt(matcher.group(3));
        String modsStr = matcher.group(5);

        User user = UserImpl.get(userId);
        MatchUserState state = new MatchUserState(this, user);
        state.setWaitStatus(PlayerWaitStatus.fromString(rawWaitingStatus));
        state.setSlot(slot);

        String[] extraStrings = modsStr.split("\\s*/\\s*");
        Pattern teamMatcher = Pattern.compile("Team (Red|Blue)");
        for (String extra : extraStrings) {
            if (extra.equals("Host")) {
                this.roomState.scheduleStateUpdate(s -> s.setHost(user));
                continue;
            }

            Matcher teamMatch = teamMatcher.matcher(extra);
            if (teamMatch.find()) {
                state.setTeam(MatchTeam.fromString(teamMatch.group(1)));
                continue;
            }

            String[] modStrings = extra.split("\\s*,\\s*");
            for (String modStr : modStrings) {
                state.addMod(Mod.fromString(modStr));
            }
        }

        this.roomState.scheduleStateUpdate(s -> {
            s.getPlayerStates().put(user, state);
        });
    }

    @Override
    public Map<Integer, User> getPlayers() {
        return this.roomState.getUsers();
    }

    @Override
    public void kickPlayer(User user) {
        this.sendMessage("!mp kick " + user.getUsername());
        this.roomState.updatePlayerRemoval(user);
    }

    @Override
    public void banPlayer(User user) {
        this.sendMessage("!mp ban " + user.getUsername());
        this.roomState.updatePlayerRemoval(user);
    }

    @Override
    public void invitePlayer(User user) {
        this.sendMessage("!mp invite #" + user.getId());
    }

    private void ensureUserInRoom(User user) {
        if (!this.roomState.getPlayerStates().containsKey(user)) {
            throw new IllegalArgumentException("User is not in the room");
        }
    }

    @Override
    public int getPlayerSlot(User user) {
        ensureUserInRoom(user);

        return this.roomState.getUserState(user).getSlot();
    }

    @Override
    public void movePlayer(User user, int slot) {
        ensureUserInRoom(user);
        this.roomState.getUserState(user).setSlot(slot);
    }

    @Override
    public PlayerWaitStatus getPlayerWaitStatus(User user) {
        return this.roomState.getUserState(user).getWaitStatus();
    }

    @Override
    public @Nullable MatchTeam getTeam(User user) {
        ensureUserInRoom(user);

        if (!this.roomState.getTeamMode().isTeammed()) {
            return null;
        }

        return this.roomState.getUserState(user).getTeam();
    }

    @Override
    public void setTeam(User user, MatchTeam team) {
        ensureUserInRoom(user);

        if (!this.roomState.getTeamMode().isTeammed()) {
            throw new IllegalStateException("Room is not team-based");
        }

        this.roomState.getUserState(user).setTeam(team);
    }

    @Nullable
    @Override
    public User getHost() {
        return this.roomState.getHost();
    }

    @Override
    public void setHost(@Nullable User user) {
        if (user == null) {
            this.sendMessage("!mp clearhost");
        } else {
            this.sendMessage("!mp host #" + user.getId());
        }
        this.roomState.setHost(user);
    }

    @Override
    public BeatmapImpl getCurrentBeatmap() {
        return this.roomState.getCurrentBeatmap();
    }

    @Override
    public void setCurrentBeatmap(Beatmap beatmap, Ruleset ruleset) {
        this.sendMessage("!mp map " + beatmap.getId() + " " + ruleset.getId());

        this.roomState.setCurrentBeatmap((BeatmapImpl) beatmap);
        this.roomState.setCurrentRuleset(ruleset);
    }

    @Override
    public void setCurrentBeatmap(Beatmap currentBeatmap) {
        this.sendMessage("!mp map " + currentBeatmap.getId());
        this.roomState.setCurrentBeatmap((BeatmapImpl) currentBeatmap);
    }

    @Override
    public Ruleset getCurrentRuleset() {
        return this.getCurrentBeatmap().getMode() == Ruleset.OSU ? this.roomState.getCurrentRuleset() : this.getCurrentBeatmap().getMode();
    }

    @Override
    public Set<Mod> getRoomMods() {
        return Collections.unmodifiableSet(this.roomState.getEnforcedMods());
    }

    @Override
    public void setRoomMods(Set<Mod> mods) {
        boolean freeMod = mods.contains(Mod.FREE_MOD);

        this.roomState.setEnforcedMods(mods);
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
        Set<Mod> modSet = new HashSet<>(this.roomState.getEnforcedMods());
        if (modSet.contains(Mod.FREE_MOD)) {
            modSet.remove(Mod.FREE_MOD);
            modSet.addAll(this.roomState.getUserState(user).getMods());
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
        this.sendMessage("!mp addref #" + user.getId());
    }

    @Override
    public void removeReferee(User user) {
        this.sendMessage("!mp removeref #" + user.getId());
    }

    @Override
    public TeamMode getTeamMode() {
        return this.roomState.getTeamMode();
    }

    @Override
    public WinCondition getWinCondition() {
        return this.roomState.getWinCondition();
    }

    @Override
    public void setProperties(TeamMode teamMode, WinCondition winCondition) {
        this.sendMessage("!mp set " + teamMode.getId() + " " + winCondition.getId());

        if (this.roomState.getTeamMode().isTeammed() ^ teamMode.isTeammed()) {
            refreshState();
        }
        this.roomState.setProperties(teamMode, winCondition);
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
        if (this.roomState.checkClosed()) {
            this.sendMessage("!mp close");
        }
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
        this.roomState.setLastActTimer(System.currentTimeMillis());
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
                    this.roomState.updatePlayerJoin(user, slot);
                }
                case MOVE -> {
                    String username = matcher.group(1);
                    int slot = Integer.parseInt(matcher.group(2));
                    User user = UserImpl.get(username);
                    this.roomState.updatePlayerMove(user, slot);
                }
                case TEAM_SWITCH -> {
                    String username = matcher.group(1);
                    MatchTeam team = MatchTeam.fromString(matcher.group(2));
                    User user = UserImpl.get(username);
                    this.roomState.updatePlayerTransferTeam(user, team);
                }
                case LEAVE -> {
                    String username = matcher.group(1);
                    User user = UserImpl.get(username);
                    this.roomState.updatePlayerRemoval(user);
                }
                case BEATMAP_CHANGED -> {
                    int beatmapId = Integer.parseInt(matcher.group(4));
                    BeatmapImpl newBeatmap = BeatmapImpl.get(beatmapId);

                    this.roomState.setCurrentBeatmap(newBeatmap);
                }
                case HOST_CHANGED -> {
                    String username = matcher.group(1);
                    User user = UserImpl.get(username);
                    this.roomState.setHost(user);
                }
                case ALL_READY -> {
                    for (MatchUserState state : this.roomState.getPlayerStates().values()) {
                        state.setWaitStatus(PlayerWaitStatus.READY);
                    }
                    this.eventBus.fire(new AllReadyEvent(this));
                }
                case MATCH_STARTED -> this.roomState.setPlaying(true);
                case MATCH_FINISHED -> {
                    // TODO: Score fetch.

                    this.roomState.setPlaying(false);
                }
            }
        }
    }

    public boolean isAlive() {
        return this.roomState.isAlive();
    }

}
