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

import java.util.regex.Pattern;

public enum BanchoMessagePattern {
    JOIN_ROOM("(.+) joined in slot (\\d+).*"),
    MOVE("(.+) moved to slot (\\d+)"),
    TEAM_SWITCH("(.+) changed to (Red|Blue)"),
    LEAVE("(.+) left the game\\."),
    FINISHED_PLAYING("(.+) finished playing \\(Score: (\\d+), (PASSED|FAILED)\\)\\."),
    BEATMAP_CHANGED("Beatmap changed to: (.+) - (.+) \\[(.+)\\] \\(https://osu.ppy.sh/b/(\\d+)\\)"),
    HOST_CHANGED("(.+) became the host\\."),
    ALL_READY("All players are ready"),
    MATCH_STARTED("The match has started!"),
    MATCH_FINISHED("The match has finished!");

    private final Pattern pattern;

    BanchoMessagePattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public static BanchoMessagePattern findMatchingPattern(String message) {
        for (BanchoMessagePattern banchoMessagePattern : values()) {
            if (banchoMessagePattern.getPattern().matcher(message).matches()) {
                return banchoMessagePattern;
            }
        }
        return null;
    }
}
