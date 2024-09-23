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

package moe.orangemc.osu.al1s.multiplayer.web.model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import moe.orangemc.osu.al1s.multiplayer.MatchEventType;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;

import java.io.IOException;

public record MatchEventData(long id, String time, User user, MatchEventType type, String description, MatchGameData game) {
    public static class Adapter extends TypeAdapter<MatchEventData> {
        @Override
        public void write(JsonWriter jsonWriter, MatchEventData matchEventData) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonWriter.beginObject();

            jsonWriter.name("id").value(matchEventData.id);
            if (matchEventData.user != null && matchEventData.user.getId() != -1) {
                jsonWriter.name("user_id").value(matchEventData.user.getId());
            }
            jsonWriter.name("timestamp").value(matchEventData.time);

            jsonWriter.name("detail").beginObject();
            jsonWriter.name("type").value(matchEventData.type.getName());
            if (!matchEventData.description.isEmpty()) {
                jsonWriter.name("text").value(matchEventData.description);
            }
            jsonWriter.endObject();

            if (matchEventData.game != null) {
                jsonWriter.name("game").beginObject();
                jsonWriter.jsonValue(gf.gson.toJson(matchEventData.game));
                jsonWriter.endObject();
            }

            jsonWriter.endObject();
        }

        @Override
        public MatchEventData read(JsonReader jsonReader) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonReader.beginObject();
            long id = -1;
            String time = "";
            User user = null;
            MatchEventType type = null;
            String description = "";
            MatchGameData game = null;

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "id":
                        id = jsonReader.nextLong();
                        break;
                    case "user_id":
                        user = UserImpl.get(jsonReader.nextInt());
                        break;
                    case "timestamp":
                        time = jsonReader.nextString();
                        break;
                    case "detail":
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            switch (jsonReader.nextName()) {
                                case "type":
                                    type = MatchEventType.valueOf(jsonReader.nextString()
                                            .replace('-', '_')
                                            .toUpperCase()
                                    );
                                    break;
                                case "text":
                                    description = jsonReader.nextString();
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                        break;
                    case "game":
                        game = gf.gson.fromJson(jsonReader, MatchGameData.class);
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new MatchEventData(id, time, user, type, description, game);
        }
    }
}
