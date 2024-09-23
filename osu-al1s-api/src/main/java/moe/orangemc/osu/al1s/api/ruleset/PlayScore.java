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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a score entry associated with one single play.
 * @param id the ID of this score
 * @param result the ({@link PlayResult}) of this score
 * @param ruleset the game mode ({@link Ruleset}) of this score
 * @param map the {@link Beatmap} played in this score
 * @param score the score value
 * @param mods a {@link Set} of all {@link Mod} used in this score
 * @param accuracy the accuracy of this score
 * @param maxCombo the maximum combo that can be obtained on this beatmap
 * @param isPerfect whether this score is a perfect play (a.k.a. Full Combo)
 * @param count50 count of 50s
 * @param count100 count of 100s (including Katu)
 * @param count300 count of 300s (including Geki and Katu)
 * @param countMiss count of misses (literally)
 * @param pp performance point
 * @param grade {@link PlayGrade} obtained in this score
 * @param player the ID of user played this
 */
public record PlayScore(long id, PlayResult result, Ruleset ruleset, Beatmap map, int score, Set<Mod> mods,
                        double accuracy, int maxCombo, int isPerfect,
                        int count50, int count100, int count300, int countMiss,
                        double pp, PlayGrade grade, User player) {
    // TODO: Use User / UserImpl when possible for better integration
}
