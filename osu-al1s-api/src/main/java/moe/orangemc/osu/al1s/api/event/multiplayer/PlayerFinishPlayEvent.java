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

package moe.orangemc.osu.al1s.api.event.multiplayer;

import moe.orangemc.osu.al1s.api.mutltiplayer.MultiplayerRoom;
import moe.orangemc.osu.al1s.api.ruleset.PlayResult;
import moe.orangemc.osu.al1s.api.user.User;

public class PlayerFinishPlayEvent extends UserActInRoomEvent {
    private final int score;
    private final PlayResult result;

    public PlayerFinishPlayEvent(MultiplayerRoom room, User user, int score, PlayResult result) {
        super(room, user);
        this.score = score;
        this.result = result;
    }

    public int getScore() {
        return score;
    }

    public PlayResult getResult() {
        return result;
    }
}
