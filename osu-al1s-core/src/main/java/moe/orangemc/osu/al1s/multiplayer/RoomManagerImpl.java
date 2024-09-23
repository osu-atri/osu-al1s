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

import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.api.mutltiplayer.MatchRoom;
import moe.orangemc.osu.al1s.api.mutltiplayer.RoomManager;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.multiplayer.web.MatchRequestAPIModule;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RoomManagerImpl implements RoomManager {
    @Inject
    private Scheduler scheduler;
    @Inject
    private OsuBotImpl referee;
    @Inject
    private Injector injector;

    private final Set<MatchRoomImpl> managedRooms = new HashSet<>();

    public RoomManagerImpl() {
        referee.execute(() -> injector.getCurrentContext().registerModule(new MatchRequestAPIModule()));
        scheduler.runTaskTimer(this::cleanupRoom, 20, 20, TimeUnit.SECONDS);
    }

    public MatchRoom createRoom(String roomName) {
        MatchRoomImpl result = new MatchRoomImpl(roomName);
        managedRooms.add(result);
        return result;
    }

    public MatchRoom findRoom(int id) {
        return managedRooms.stream().filter(room -> room.getId() == id).findFirst().orElse(null);
    }

    public void cleanupRoom() {
        managedRooms.removeIf(room -> !room.isAlive());
    }
}
