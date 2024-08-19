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
import moe.orangemc.osu.al1s.api.beatmap.RankStatus;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;

import java.io.IOException;

public record MatchBeatmapData(int id, int setId, Ruleset mode,
                               double diffRating, RankStatus status, int length,
                               String diff, String artist, String artistU, User mapper,
                               String title, String titleU, String source, int playCount, int loveCount) implements Beatmap {
    public static class Adapter extends TypeAdapter<MatchBeatmapData> {
        @Override
        public void write(JsonWriter jsonWriter, MatchBeatmapData matchBeatmapData) throws IOException {
            jsonWriter.beginObject();

            jsonWriter.name("id").value(matchBeatmapData.id);
            jsonWriter.name("beatmapset_id").value(matchBeatmapData.setId);
            jsonWriter.name("difficulty_rating").value(matchBeatmapData.diffRating);
            jsonWriter.name("mode").value(matchBeatmapData.mode.getName());
            jsonWriter.name("status").value(matchBeatmapData.status.getName());
            jsonWriter.name("total_length").value(matchBeatmapData.length);
            jsonWriter.name("user_id").value(matchBeatmapData.mapper.getId());
            jsonWriter.name("version").value(matchBeatmapData.diff);

            jsonWriter.name("beatmapset").beginObject();
            jsonWriter.name("id").value(matchBeatmapData.setId);
            jsonWriter.name("creator").value(matchBeatmapData.mapper.getUsername());
            jsonWriter.name("user_id").value(matchBeatmapData.mapper.getId());
            jsonWriter.name("status").value(matchBeatmapData.status.getName());
            jsonWriter.name("artist").value(matchBeatmapData.artist);
            jsonWriter.name("artist_unicode").value(matchBeatmapData.artistU);
            jsonWriter.name("title").value(matchBeatmapData.title);
            jsonWriter.name("title_unicode").value(matchBeatmapData.titleU);
            jsonWriter.name("source").value(matchBeatmapData.source);
            jsonWriter.name("play_count").value(matchBeatmapData.playCount);
            jsonWriter.name("favourite_count").value(matchBeatmapData.loveCount);
            jsonWriter.endObject();

            jsonWriter.endObject();
        }

        @Override
        public MatchBeatmapData read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            int id = -1;
            int setId = -1;
            Ruleset mode = null;
            double diffRating = 0;
            RankStatus status = RankStatus.UNKNOWN;
            int length = -1;
            String diff = "";
            String artist = "";
            String artistU = "";
            User mapper = null;
            String title = "";
            String titleU = "";
            String source = "";
            int playCount = -1;
            int loveCount = -1;

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "id":
                        id = jsonReader.nextInt();
                        break;
                    case "beatmapset_id":
                        setId = jsonReader.nextInt();
                        break;
                    case "user_id":
                        mapper = UserImpl.get(jsonReader.nextInt());
                        break;
                    case "difficulty_rating":
                        diffRating = jsonReader.nextDouble();
                        break;
                    case "mode":
                        mode = Ruleset.valueOf(jsonReader.nextString().toUpperCase());
                        break;
                    case "status":
                        status = RankStatus.valueOf(jsonReader.nextString().toUpperCase());
                        break;
                    case "total_length":
                        length = jsonReader.nextInt();
                        break;
                    case "version":
                        diff = jsonReader.nextString();
                        break;
                    case "beatmapset":
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            switch (jsonReader.nextName()) {
                                case "artist":
                                    artist = jsonReader.nextString();
                                    break;
                                case "artist_unicode":
                                    artistU = jsonReader.nextString();
                                    break;
                                case "title":
                                    title = jsonReader.nextString();
                                    break;
                                case "title_unicode":
                                    titleU = jsonReader.nextString();
                                    break;
                                case "favourite_count":
                                    loveCount = jsonReader.nextInt();
                                    break;
                                case "play_count":
                                    playCount = jsonReader.nextInt();
                                    break;
                                case "source":
                                    source = jsonReader.nextString();
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
            return new MatchBeatmapData(id, setId, mode, diffRating, status, length,
                    diff, artist, artistU, mapper, title, titleU, source, playCount, loveCount);
        }
    }

    @Override
    public <T> T getMetadata(String key) {
        throw new UnsupportedOperationException("This is only a basic implementation, so don't expect much.");
    }

    @Override
    public int getId() { return id; }

    @Override
    public int getSetId() { return setId; }

    @Override
    public Ruleset getMode() { return mode; }
}
