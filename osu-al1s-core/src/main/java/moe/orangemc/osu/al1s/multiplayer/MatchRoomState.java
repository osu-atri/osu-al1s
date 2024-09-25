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

import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.multiplayer.game.BeatmapChangeEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.game.MatchEndEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.game.MatchStartEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.setting.HostChangeEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.setting.RoomNameUpdateEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.user.UserJoinRoomEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.user.UserLeaveRoomEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.user.UserMoveToSlotEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.user.UserSwitchTeamEvent;
import moe.orangemc.osu.al1s.api.mutltiplayer.MatchTeam;
import moe.orangemc.osu.al1s.api.mutltiplayer.TeamMode;
import moe.orangemc.osu.al1s.api.mutltiplayer.WinCondition;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.beatmap.BeatmapImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class MatchRoomState {
    @Inject
    private EventBus eventBus;

    private final MatchRoomImpl owner;
    private final Queue<Consumer<MatchRoomState>> stateUpdateQueue = new ConcurrentLinkedQueue<>();

    private String name;
    private String password;

    private final Map<User, MatchUserState> playerStates = new ConcurrentHashMap<>();

    private @Nullable User host;

    private final Set<User> referee = new HashSet<>();

    private WinCondition winCondition = WinCondition.SCORE;
    private TeamMode teamMode = TeamMode.HEAD_TO_HEAD;

    private BeatmapImpl currentBeatmap;
    private Set<Mod> enforcedMods = new HashSet<>();
    private Ruleset currentRuleset = Ruleset.OSU;

    private long lastActTimer;

    private boolean alive = true;
    private boolean playing = false;

    private int lastEventId = 0;

    public MatchRoomState(MatchRoomImpl owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name != null && !this.name.equals(name)) {
            RoomNameUpdateEvent event = new RoomNameUpdateEvent(owner, name);
            eventBus.fire(event);
            if (event.isCancelled()) {
                this.owner.setName(this.name);
                return;
            }
            name = event.getNewName();
        }

        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<User, MatchUserState> getPlayerStates() {
        return playerStates;
    }

    public @Nullable User getHost() {
        return host;
    }

    public void setHost(@Nullable User host) {
        if (host == null || this.host == null || (host != this.host && host.getId() != this.host.getId())) {
            HostChangeEvent event = new HostChangeEvent(owner, this.host, host);
            eventBus.fire(event);
            User newHost = event.getNewHost();
            if (event.isCancelled()) {
                newHost = this.host;
            }
            if (host == null || newHost == null || ((newHost != host) && newHost.getId() != host.getId())) {
                owner.setHost(newHost);
                return;
            }
        }
        this.host = host;
    }

    public Set<User> getReferee() {
        return referee;
    }

    public WinCondition getWinCondition() {
        return winCondition;
    }

    public void setWinCondition(WinCondition winCondition) {
        this.winCondition = winCondition;
    }

    public TeamMode getTeamMode() {
        return teamMode;
    }

    public void setTeamMode(TeamMode teamMode) {
        this.teamMode = teamMode;
    }

    public BeatmapImpl getCurrentBeatmap() {
        return currentBeatmap;
    }

    public void setCurrentBeatmap(BeatmapImpl currentBeatmap) {
        if (this.currentBeatmap.getId() != currentBeatmap.getId()) {
            BeatmapChangeEvent event = new BeatmapChangeEvent(owner, currentBeatmap);
            eventBus.fire(event);
            BeatmapImpl newMap = (BeatmapImpl) event.getBeatmap();
            if (event.isCancelled()) {
                newMap = this.currentBeatmap;
            }
            if (newMap.getId() != currentBeatmap.getId()) {
                owner.setCurrentBeatmap(this.currentBeatmap);
                return;
            }
        }

        this.currentBeatmap = currentBeatmap;
    }

    public Set<Mod> getEnforcedMods() {
        return enforcedMods;
    }

    public void setEnforcedMods(Set<Mod> enforcedMods) {
        this.enforcedMods = enforcedMods;
    }

    public Ruleset getCurrentRuleset() {
        return currentRuleset;
    }

    public void setCurrentRuleset(Ruleset currentRuleset) {
        this.currentRuleset = currentRuleset;
    }

    public long getLastActTimer() {
        return lastActTimer;
    }

    public void setLastActTimer(long lastActTimer) {
        this.lastActTimer = lastActTimer;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(int lastEventId) {
        this.lastEventId = lastEventId;
    }

    public void updatePlayerRemoval(User user) {
        if (!playerStates.containsKey(user)) {
            return;
        }

        playerStates.remove(user);
        UserLeaveRoomEvent event = new UserLeaveRoomEvent(owner, user);
        eventBus.fire(event);
    }

    public void updatePlayerJoin(User user, int slot) {
        if (playerStates.containsKey(user)) {
            return;
        }

        playerStates.put(user, new MatchUserState(owner, user));
        UserJoinRoomEvent event = new UserJoinRoomEvent(owner, user, slot);
        eventBus.fire(event);
        playerStates.get(user).setSlot(event.getSlot());
    }

    public void updatePlayerMove(User user, int slot) {
        UserMoveToSlotEvent event = new UserMoveToSlotEvent(owner, user, slot);
        eventBus.fire(event);
        playerStates.get(user).setSlot(event.getSlot());
    }

    public void updatePlayerTransferTeam(User user, MatchTeam team) {
        UserSwitchTeamEvent event = new UserSwitchTeamEvent(owner, user, team);
        eventBus.fire(event);
        if (event.isCancelled()) {
            playerStates.get(user).setTeam(playerStates.get(user).getTeam());
        } else {
            playerStates.get(user).setTeam(event.getTeam());
        }
    }

    public void scheduleStateUpdate(Consumer<MatchRoomState> stateUpdate) {
        stateUpdateQueue.add(stateUpdate);
    }

    public void executeStateUpdate() {
        Consumer<MatchRoomState> stateUpdate;
        while ((stateUpdate = stateUpdateQueue.poll()) != null) {
            stateUpdate.accept(this);
        }
    }

    public void setProperties(TeamMode teamMode, WinCondition winCondition) {
        this.setTeamMode(teamMode);
        this.setWinCondition(winCondition);
    }

    public Map<Integer, User> getUsers() {
        Map<Integer, User> users = new ConcurrentHashMap<>();
        playerStates.forEach((user, matchUserState) -> users.put(matchUserState.getSlot(), user));
        return users;
    }

    public MatchUserState getUserState(User user) {
        return playerStates.get(user);
    }

    public boolean checkClosed() {
        if (!alive) {
            return false;
        }
        alive = false;
        return true;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        if (this.playing ^ playing) {
            if (playing) {
                this.eventBus.fire(new MatchStartEvent(owner));
            } else {
                this.eventBus.fire(new MatchEndEvent(owner));
            }
        }

        this.playing = playing;
    }

    public int findMinAvailableSlot() {
        for (int i = 0; i < 16; i++) {
            int finalI = i;
            if (playerStates.values().stream().noneMatch(matchUserState -> matchUserState.getSlot() == finalI)) {
                return i;
            }
        }
        return -1;
    }
}
