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

package moe.orangemc.osu.al1s.api.event.multiplayer.setting;

import moe.orangemc.osu.al1s.api.event.CancellableEvent;
import moe.orangemc.osu.al1s.api.event.multiplayer.MatchRoomEvent;
import moe.orangemc.osu.al1s.api.mutltiplayer.MatchRoom;
import moe.orangemc.osu.al1s.api.mutltiplayer.TeamMode;
import moe.orangemc.osu.al1s.api.mutltiplayer.WinCondition;

public class RoomSettingUpdateEvent extends MatchRoomEvent implements CancellableEvent {
    private WinCondition newWinCondition;
    private TeamMode newTeamMode;
    private boolean cancelled;

    public RoomSettingUpdateEvent(MatchRoom room, WinCondition newWinCondition, TeamMode newTeamMode) {
        super(room);
        this.newWinCondition = newWinCondition;
        this.newTeamMode = newTeamMode;
    }

    public WinCondition getNewWinCondition() {
        return newWinCondition;
    }

    public void setNewWinCondition(WinCondition newWinCondition) {
        this.newWinCondition = newWinCondition;
    }

    public TeamMode getNewTeamMode() {
        return newTeamMode;
    }

    public void setNewTeamMode(TeamMode newTeamMode) {
        this.newTeamMode = newTeamMode;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
