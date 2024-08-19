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
import moe.orangemc.osu.al1s.api.beatmap.BeatmapSet;
import moe.orangemc.osu.al1s.api.match.MatchGame;
import moe.orangemc.osu.al1s.api.mutltiplayer.TeamMode;
import moe.orangemc.osu.al1s.api.mutltiplayer.WinCondition;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.ruleset.PlayScore;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record MatchGameData(long id, MatchBeatmapData map, Ruleset mode, String start, String end,
                            TeamMode teamType, WinCondition scoringType,
                            Set<Mod> mods, List<PlayScore> scores) implements MatchGame {
    public static class Adapter extends TypeAdapter<MatchGameData> {
        @Override
        public void write(JsonWriter jsonWriter, MatchGameData matchGameData) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonWriter.beginObject();

            jsonWriter.name("id").value(matchGameData.id);
            jsonWriter.name("beatmap_id").value(matchGameData.map.getId());
            jsonWriter.name("start_time").value(matchGameData.start);
            jsonWriter.name("end_time").value(matchGameData.end);
            jsonWriter.name("mode").value(matchGameData.mode.getName());
            jsonWriter.name("scoring_type").value(matchGameData.scoringType.getName());
            jsonWriter.name("team_type").value(matchGameData.teamType.name()
                    .replace('_', '-')
                    .toLowerCase()
            );

            if (!matchGameData.mods.isEmpty())
            {
                jsonWriter.name("mods").beginArray();
                for (Mod mod : matchGameData.mods) {
                    jsonWriter.value(mod.getShortName());
                }
                jsonWriter.endArray();
            }

            jsonWriter.name("beatmap").beginObject();
            jsonWriter.jsonValue(gf.gson.toJson(matchGameData.map));
            jsonWriter.endObject();

            if (!matchGameData.scores.isEmpty()) {
                jsonWriter.name("scores").beginArray();
                for (PlayScore score : matchGameData.scores) {
                    jsonWriter.beginObject();
                    jsonWriter.jsonValue(gf.gson.toJson(score));
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
            }
            jsonWriter.endObject();
        }

        @Override
        public MatchGameData read(JsonReader jsonReader) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonReader.beginObject();
            long id = -1;
            MatchBeatmapData map = null;
            Ruleset mode = null;
            String start = "";
            String end = "";
            TeamMode teamType = null;
            WinCondition scoringType = null;
            Set<Mod> mods = new java.util.HashSet<>(Collections.emptySet());
            List<PlayScore> scores = new java.util.ArrayList<>(Collections.emptyList());

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "id":
                        id = jsonReader.nextLong();
                        break;
                    case "start_time":
                        start = jsonReader.nextString();
                        break;
                    case "end_time":
                        end = jsonReader.nextString();
                        break;
                    case "mode":
                        mode = Ruleset.valueOf(jsonReader.nextString().toUpperCase());
                        break;
                    case "scoring_type":
                        scoringType = WinCondition.fromString(jsonReader.nextString());
                        break;
                    case "team_type":
                        teamType = TeamMode.valueOf(jsonReader.nextString().toUpperCase());
                        break;
                    case "mods":
                        jsonReader.beginArray();
                        // TODO: Add NoMod?
                        while (jsonReader.hasNext()) {
                            mods.add(Mod.valueOf(jsonReader.nextString()));
                        }
                        jsonReader.endArray();
                        break;
                    case "beatmap":
                        jsonReader.beginObject();
                        map = gf.gson.fromJson(jsonReader, MatchBeatmapData.class);
                        jsonReader.endArray();
                    case "scores":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            scores.add(gf.gson.fromJson(jsonReader, PlayScore.class));
                        }
                        jsonReader.endArray();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new MatchGameData(id, map, mode, start, end, teamType, scoringType, mods, scores);
        }
    }

    @Override
    public long getId() { return id; }

    @Override
    public String getStartTime() { return start; }

    @Override
    public String getEndTime() { return end; }

    @Override
    public Beatmap getMap() { return map; }

    @Override
    public BeatmapSet getMapSet() {
        throw new UnsupportedOperationException("I don't know ;w;");
    }

    @Override
    public Ruleset getMode() { return mode; }

    @Override
    public TeamMode getTeamMode() { return teamType; }

    @Override
    public WinCondition getWinCondition() { return scoringType; }

    @Override
    public Set<Mod> getMods() { return mods; }

    @Override
    public List<PlayScore> getScores() { return scores; }

    @Override
    public <T> T getMetadata(String key) {
        throw new UnsupportedOperationException("Ask someone else please.");
    }
}
