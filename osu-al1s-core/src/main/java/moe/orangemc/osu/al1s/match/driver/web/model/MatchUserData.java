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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public record MatchUserData(int id, String name, String countryCode, String country) {
    public static class Adapter extends TypeAdapter<MatchUserData> {
        @Override
        public void write(JsonWriter jsonWriter, MatchUserData matchUserData) throws IOException {
            jsonWriter.beginObject();

            jsonWriter.name("id").value(matchUserData.id);
            jsonWriter.name("username").value(matchUserData.name);
            jsonWriter.name("country_code").value(matchUserData.countryCode);

            jsonWriter.name("country").beginObject();
            jsonWriter.name("code").value(matchUserData.countryCode);
            jsonWriter.name("name").value(matchUserData.country);
            jsonWriter.endObject();

            jsonWriter.endObject();
        }

        @Override
        public MatchUserData read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();

            int id = -1;
            String name = "";
            String countryCode = "";
            String country = "";

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "id":
                        id = jsonReader.nextInt();
                        break;
                    case "username":
                        name = jsonReader.nextString();
                        break;
                    case "country":
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            switch (jsonReader.nextName()) {
                                case "code":
                                    countryCode = jsonReader.nextString();
                                    break;
                                case "name":
                                    country = jsonReader.nextString();
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
            return new MatchUserData(id, name, countryCode, country);
        }
    }
}
