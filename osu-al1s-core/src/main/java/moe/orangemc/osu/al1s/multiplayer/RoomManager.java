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

import moe.orangemc.osu.al1s.api.chat.ChatManager;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.util.HashSet;
import java.util.Set;

public class RoomManager {
    @Inject
    private ChatManager chatManager;

    private final Set<RoomImpl> managedRooms = new HashSet<>();

    public RoomImpl createRoom(String roomName) {
        RoomImpl result = new RoomImpl(roomName);
        managedRooms.add(result);
        return result;
    }
}
