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

/**
 * Represents two multiplayer teams for Team VS mode.
 */
public enum MultiplayerTeam {
    RED("red"),
    BLUE("blue");

    private final String id;

    MultiplayerTeam(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Translate a string into a {@link MultiplayerTeam} if possible.
     * @param team the source string to be translated
     * @return a {@link MultiplayerTeam} from the given string
     * @throws IllegalArgumentException when the given string cannot be translated into a valid team name
     */
    public static MultiplayerTeam fromString(String team) {
        return switch (team) {
            case "Red" -> RED;
            case "Blue" -> BLUE;
            default -> throw new IllegalArgumentException("Unknown team: " + team);
        };
    }

    /**
     * Get the opposite team of the given team.
     * @return a {@link MultiplayerTeam} opposite of this team
     */
    public MultiplayerTeam getOpposite() {
        return switch (this) {
            case RED -> BLUE;
            case BLUE -> RED;
        };
    }
}
