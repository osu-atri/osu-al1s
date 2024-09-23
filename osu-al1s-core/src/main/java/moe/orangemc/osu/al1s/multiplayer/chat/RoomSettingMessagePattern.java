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

package moe.orangemc.osu.al1s.multiplayer.chat;

import java.util.regex.Pattern;

public enum RoomSettingMessagePattern {
    ROOM_NAME_AND_LINK("Room name: (.+), History: https://osu\\.ppy\\.sh/mp/(\\d+)"),
    BEATMAP("Beatmap: https://osu\\.ppy\\.sh/b/(\\d+) (.+) - (.+).*"),
    MODE("Team mode: (.+), Win condition: (.+)"),
    PLAYER_COUNT("Players: (\\d+)"),
    SLOTS("Slot (\\d+) (.+) https://osu\\.ppy\\.sh/u/(\\d+) (.+) \\[(.+)\\]"),
    MODS("Active mods: (.+)"),;
    private final Pattern pattern;

    RoomSettingMessagePattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public static RoomSettingMessagePattern findMatchingPattern(String msg) {
        for (RoomSettingMessagePattern pattern : values()) {
            if (pattern.getPattern().matcher(msg).matches()) {
                return pattern;
            }
        }
        return null;
    }
}
