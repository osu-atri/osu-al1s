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
import moe.orangemc.osu.al1s.api.match.Match;
import moe.orangemc.osu.al1s.api.match.MatchEvent;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A record of an individual match.<br>
 * Start and end time not included.
 * @param id the ID of the match
 * @param name the name (i.e. multiplayer room name) of the match
 * @param users a list of {@link User}, showing all users involved in this match
 * @param events a list of {@link MatchEventData}, showing events of the match
 */
public record MatchData(int id, String name, List<User> users, List<MatchEvent> events) implements Match {
    public static class Adapter extends TypeAdapter<MatchData> {
        @Override
        public void write(JsonWriter jsonWriter, MatchData matchData) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonWriter.beginObject();

            // "match" section
            jsonWriter.name("match").beginObject();
            jsonWriter.name("id").value(matchData.id);
            jsonWriter.name("name").value(matchData.name);
            jsonWriter.endObject();

            // "events" section
            jsonWriter.name("events").beginArray();
            for (MatchEvent event : matchData.events()) {
                jsonWriter.beginObject();
                jsonWriter.jsonValue(gf.gson.toJson(event));
                jsonWriter.endObject();
            }
            jsonWriter.endArray();

            // "users" section
            jsonWriter.name("users").beginArray();
            for (User user : matchData.users()) {
                jsonWriter.beginObject();
                jsonWriter.name("id").value(user.getId());
                jsonWriter.name("username").value(user.getUsername());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();

            jsonWriter.endObject();
        }

        @Override
        public MatchData read(JsonReader jsonReader) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonReader.beginObject();
            int id = -1;
            String name = "";
            List<User> users = new ArrayList<>();
            List<MatchEvent> events = new ArrayList<>();

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "match":
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            switch (jsonReader.nextName()) {
                                case "id":
                                    id = jsonReader.nextInt();
                                    break;
                                case "name":
                                    name = jsonReader.nextString();
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                        break;
                    case "events":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            events.add(gf.gson.fromJson(jsonReader, MatchEventData.class));
                        }
                        jsonReader.endArray();
                        break;
                    case "users":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            users.add(gf.gson.fromJson(jsonReader, MatchUserData.class));
                        }
                        jsonReader.endArray();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new MatchData(id, name, users, events);
        }
    }

    @Override
    public int getId() { return id; }

    @Override
    public List<MatchEvent> getEvents() { return events; }

    @Override
    public List<User> getUsers() { return users; }

    @Override
    public <T> T getMetadata(String key) {
        // TODO: Extend or throw as unimplemented
        throw new UnsupportedOperationException("MatchData knows nothing but data, not metadata!");
    }
}
