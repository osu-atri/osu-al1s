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

package moe.orangemc.osu.al1s.match.driver.web.model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.ruleset.*;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public record PlayScoreData() {
    public static class Adapter extends TypeAdapter<PlayScore> {
        @Override
        public void write(JsonWriter jsonWriter, PlayScore playScore) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("accuracy").value(playScore.accuracy());
            jsonWriter.name("id").value(playScore.id());
            jsonWriter.name("max_combo").value(playScore.maxCombo());
            jsonWriter.name("mode").value(playScore.ruleset().name().toLowerCase());
            jsonWriter.name("passed").value(playScore.result() == PlayResult.PASSED);
            jsonWriter.name("perfect").value(playScore.isPerfect());
            jsonWriter.name("pp").value(playScore.pp());
            jsonWriter.name("grade").value(playScore.grade().name());
            jsonWriter.name("score").value(playScore.score());
            jsonWriter.name("user_id").value(playScore.userId());

            jsonWriter.name("mods").beginArray();
            for (Mod mod : playScore.mods()) {
                jsonWriter.value(mod.getShortName());
            }
            jsonWriter.endArray();

            jsonWriter.name("statistics").beginObject();
            jsonWriter.name("count_300").value(playScore.count300());
            jsonWriter.name("count_100").value(playScore.count100());
            jsonWriter.name("count_50").value(playScore.count50());
            jsonWriter.name("count_miss").value(playScore.countMiss());
            jsonWriter.endObject();

            jsonWriter.endObject();
        }

        @Override
        public PlayScore read(JsonReader jsonReader) throws IOException {
            // TODO: We cannot fetch the BeatmapID from a single score?
            jsonReader.beginObject();
            long id = -1;
            Beatmap map = null;
            PlayResult result = null;
            Ruleset ruleset = null;
            int score = 0;
            Set<Mod> mods = new java.util.HashSet<>(Collections.emptySet());
            double accuracy = 0;
            int maxCombo = -1;
            int isPerfect = -1;
            int count50 = -1;
            int count100 = -1;
            int count300 = -1;
            int countMiss = -1;
            double pp = 0;
            PlayGrade grade = null;
            int userId = 0;

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    // TODO: Can be null (when failed), how to handle this?
                    case "id":
                        id = jsonReader.nextLong();
                        break;
                    case "accuracy":
                        accuracy = jsonReader.nextDouble();
                        break;
                    case "max_combo":
                        maxCombo = jsonReader.nextInt();
                        break;
                    case "mode":
                        ruleset = Ruleset.valueOf(jsonReader.nextString().toUpperCase());
                        break;
                    case "mods":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            mods.add(Mod.fromString(jsonReader.nextString()));
                        }
                        jsonReader.endArray();
                        break;
                    case "score":
                        score = jsonReader.nextInt();
                        break;
                    case "pp":
                        pp = jsonReader.nextInt();
                        break;
                    case "passed":
                        result = jsonReader.nextBoolean() ? PlayResult.PASSED : PlayResult.FAILED;
                        break;
                    case "perfect":
                        isPerfect = jsonReader.nextInt();
                        break;
                    case "user_id":
                        userId = jsonReader.nextInt();
                        break;
                    case "grade":
                        grade = PlayGrade.valueOf(jsonReader.nextString());
                        break;
                    case "statistics":
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            switch (jsonReader.nextName()) {
                                case "count_300":
                                    count300 = jsonReader.nextInt();
                                    break;
                                case "count_100":
                                    count100 = jsonReader.nextInt();
                                    break;
                                case "count_50":
                                    count50 = jsonReader.nextInt();
                                    break;
                                case "count_miss":
                                    countMiss = jsonReader.nextInt();
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new PlayScore(id, result, ruleset, map, score, mods, accuracy, maxCombo, isPerfect,
                    count50, count100, count300, countMiss, pp, grade, userId);
        }
    }
}
