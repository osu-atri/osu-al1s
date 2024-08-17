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

package moe.orangemc.osu.al1s.chat.driver.web.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public record OutboundChannelJoin(String username, String channel) {
    public static class Adapter extends TypeAdapter<OutboundChannelJoin> {
        @Override
        public void write(JsonWriter jsonWriter, OutboundChannelJoin outboundChannelJoin) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("user").value(outboundChannelJoin.username());
            jsonWriter.name("channel").value(outboundChannelJoin.channel());
            jsonWriter.endObject();
        }

        @Override
        public OutboundChannelJoin read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            String username = null;
            String channel = null;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "user":
                        username = jsonReader.nextString();
                        break;
                    case "channel":
                        channel = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new OutboundChannelJoin(username, channel);
        }
    }
}
