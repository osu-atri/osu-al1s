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

public enum WinCondition {
    SCORE(0, "Score"),
    ACCURACY(1, "Accuracy"),
    COMBO(2, "Combo"),
    SCORE_V2(3, "ScoreV2");

    private final int id;
    private final String name;

    WinCondition(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() { return name; }

    public static WinCondition fromString(String condition) {
        return switch (condition) {
            case "Score" -> SCORE;
            case "Accuracy" -> ACCURACY;
            case "Combo" -> COMBO;
            case "ScoreV2" -> SCORE_V2;
            default -> throw new IllegalArgumentException("Unknown win condition: " + condition);
        };
    }
}
