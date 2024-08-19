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

package moe.orangemc.osu.al1s.api.match;

import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.beatmap.BeatmapSet;
import moe.orangemc.osu.al1s.api.mutltiplayer.TeamMode;
import moe.orangemc.osu.al1s.api.mutltiplayer.WinCondition;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.ruleset.PlayScore;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;

import java.util.List;
import java.util.Set;

public interface MatchGame {
    // General map for storing attributes.
    <T> T getMetadata(String key);

    long getId();
    String getStartTime();
    String getEndTime();

    Beatmap getMap();
    BeatmapSet getMapSet();

    Ruleset getMode();
    Set<Mod> getMods();
    List<PlayScore> getScores();

    TeamMode getTeamMode();
    WinCondition getWinCondition();
}
