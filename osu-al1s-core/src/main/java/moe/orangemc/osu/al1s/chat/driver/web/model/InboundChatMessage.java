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

public record InboundChatMessage(int channelId, String message, boolean action, int senderUid) {
    public static class Adapter extends TypeAdapter<InboundChatMessage> {
        @Override
        public void write(JsonWriter jsonWriter, InboundChatMessage inboundChatMessage) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("channel_id").value(inboundChatMessage.channelId());
            jsonWriter.name("content").value(inboundChatMessage.message());
            jsonWriter.name("is_action").value(inboundChatMessage.action());
            jsonWriter.name("sender_id").value(inboundChatMessage.senderUid());
            jsonWriter.endObject();
        }

        @Override
        public InboundChatMessage read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            int channelId = -1;
            String message = null;
            boolean action = false;
            int senderUid = -1;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "channel_id":
                        channelId = jsonReader.nextInt();
                        break;
                    case "content":
                        message = jsonReader.nextString();
                        break;
                    case "is_action":
                        action = jsonReader.nextBoolean();
                        break;
                    case "sender_id":
                        senderUid = jsonReader.nextInt();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new InboundChatMessage(channelId, message, action, senderUid);
        }
    }
}
