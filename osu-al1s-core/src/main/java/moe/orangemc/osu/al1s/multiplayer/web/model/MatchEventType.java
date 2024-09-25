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

package moe.orangemc.osu.al1s.multiplayer.web.model;

/**
 * An enum representing all match event types.
 */
public enum MatchEventType {
    HOST_CHANGED("host-changed"),
    MATCH_CREATED("match-created"),
    MATCH_DISBANDED("match-disbanded"),
    OTHER("other"),
    PLAYER_JOINED("player-joined"),
    PLAYER_KICKED("player-kicked"),
    PLAYER_LEFT("player-left");

    private final String name;

    MatchEventType(String name) { this.name = name; }

    public String getName() { return name; }
}
