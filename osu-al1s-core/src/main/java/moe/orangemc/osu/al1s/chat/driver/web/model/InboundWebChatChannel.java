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

public record InboundWebChatChannel(int id, String name, String description, ChannelType type, int maxLength, boolean restricted) {
    public static class Adapter extends TypeAdapter<InboundWebChatChannel> {
        @Override
        public void write(JsonWriter jsonWriter, InboundWebChatChannel inboundWebChatChannel) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("channel_id").value(inboundWebChatChannel.id());
            jsonWriter.name("name").value(inboundWebChatChannel.name());
            jsonWriter.name("description").value(inboundWebChatChannel.description());
            jsonWriter.name("type").value(inboundWebChatChannel.type().name());
            jsonWriter.name("message_length_limit").value(inboundWebChatChannel.maxLength());
            jsonWriter.name("moderated").value(inboundWebChatChannel.restricted());
            jsonWriter.endObject();
        }

        @Override
        public InboundWebChatChannel read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            int id = -1;
            String name = null;
            String description = null;
            ChannelType type = null;
            int maxLength = -1;
            boolean restricted = false;
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
                switch (key) {
                    case "channel_id":
                        id = jsonReader.nextInt();
                        break;
                    case "name":
                        name = jsonReader.nextString();
                        break;
                    case "description":
                        description = jsonReader.nextString();
                        break;
                    case "type":
                        type = ChannelType.valueOf(jsonReader.nextString());
                        break;
                    case "message_length_limit":
                        maxLength = jsonReader.nextInt();
                        break;
                    case "moderated":
                        restricted = jsonReader.nextBoolean();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();
            return new InboundWebChatChannel(id, name, description, type, maxLength, restricted);
        }
    }
}
