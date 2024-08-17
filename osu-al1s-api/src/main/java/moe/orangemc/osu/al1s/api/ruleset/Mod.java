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

package moe.orangemc.osu.al1s.api.ruleset;

public enum Mod {
    NO_MOD(0, "No Mod", "NM"),
    NO_FAIL(1, "No Fail", "NF"),
    EASY(2, "Easy", "EZ"),
    TOUCH_DEVICE(3, "Touch Device", "TD"),
    HIDDEN(4, "Hidden", "HD"),
    HARD_ROCK(5, "Hard Rock", "HR"),
    SUDDEN_DEATH(6, "Sudden Death", "SD"),
    DOUBLE_TIME(7, "Double Time", "DT"),
    RELAX(8, "Relax", "RX"),
    HALF_TIME(9, "Half Time", "HT"),
    NIGHTCORE(10, "Nightcore", "NC"),
    FLASHLIGHT(11, "Flashlight", "FL"),
    AUTOPLAY(12, "Autoplay", "AU"),
    SPUN_OUT(13, "Spun Out", "SO"),
    AUTOPILOT(14, "Autopilot", "AP"),
    PERFECT(15, "Perfect", "PF"),
    KEY4(16, "4K", "4K"),
    KEY5(17, "5K", "5K"),
    KEY6(18, "6K", "6K"),
    KEY7(19, "7K", "7K"),
    KEY8(20, "8K", "8K"),
    FADE_IN(21, "Fade In", "FI"),
    RANDOM(22, "Random", "RD"),
    CINEMA(23, "Cinema", "CM"),
    TARGET_PRATICE(24, "Target Practice", "TP"),
    KEY9(25, "9K", "9K"),
    KEY_COOP(26, "Co-op", "CP"),
    KEY1(27, "1K", "1K"),
    KEY3(28, "3K", "3K"),
    KEY2(29, "2K", "2K"),
    SCOREV2(30, "ScoreV2", "SV2"),
    MIRROR(31, "Mirror", "MR"),

    KEY_MOD(0, "Key Mod", "KEY", KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY_COOP),
    FREE_MOD(0, "Free Mod", "FM", NO_FAIL, EASY, FADE_IN, HARD_ROCK, SUDDEN_DEATH, RELAX, FLASHLIGHT, SPUN_OUT, AUTOPILOT, KEY_MOD),
    SCORE_INCREASE_MOD(0, "Score Increase Mod", "IM", HIDDEN, HARD_ROCK, FLASHLIGHT, DOUBLE_TIME, FADE_IN);

    private final int value;
    private final String name;
    private final String shortName;

    Mod(int id, String name, String shortName, Mod... overlay) {
        int value;
        if (id > 0) {
            value = 1 << id - 1;
        } else {
            value = 0;
        }
        this.name = name;
        this.shortName = shortName;
        for (Mod mod : overlay) {
            value |= mod.value;
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }
}