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

import moe.orangemc.osu.al1s.api.mutltiplayer.MatchTeam;
import moe.orangemc.osu.al1s.api.mutltiplayer.PlayerWaitStatus;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.user.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MatchUserState {
    private final MatchRoomImpl matchRoom;
    private int slot;
    private MatchTeam team;
    private final Set<Mod> mods = new HashSet<>();
    private PlayerWaitStatus waitStatus = PlayerWaitStatus.NOT_READY;
    private final User user;

    public MatchUserState(MatchRoomImpl matchRoom, User user) {
        this.matchRoom = matchRoom;
        this.user = user;
    }

    public void setSlot(int slot) {
        this.slot = slot;
        matchRoom.sendMessage("!mp move #" + user.getId() + " " + slot);
    }

    public void setTeam(MatchTeam team) {
        this.team = team;
        matchRoom.sendMessage("!mp team #" + user.getId() + " " + team.getId());
    }

    public int getSlot() {
        return slot;
    }

    public MatchTeam getTeam() {
        return team;
    }

    public Set<Mod> getMods() {
        return Collections.unmodifiableSet(mods);
    }

    public PlayerWaitStatus getWaitStatus() {
        return waitStatus;
    }

    public User getUser() {
        return user;
    }

    public void addMod(Mod mod) {
        mods.add(mod);
    }

    public void setWaitStatus(PlayerWaitStatus waitStatus) {
        this.waitStatus = waitStatus;
    }
}
