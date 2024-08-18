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

package moe.orangemc.osu.al1s.api.mutltiplayer;

public enum PlayerWaitStatus {
    READY,
    NOT_READY,
    NO_MAP,
    // RED_PIGGED
    ;
    public static PlayerWaitStatus fromString(String status) {
        return switch (status) {
            case "Ready" -> READY;
            case "Not Ready" -> NOT_READY;
            case "No Map" -> NO_MAP;
            default -> throw new IllegalArgumentException("Unknown player wait status: " + status);
        };
    }
}
