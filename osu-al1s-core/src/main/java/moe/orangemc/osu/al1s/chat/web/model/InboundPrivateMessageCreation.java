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

package moe.orangemc.osu.al1s.chat.web.model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.IOException;

public record InboundPrivateMessageCreation(InboundWebChatChannel channel, int newChannelId, InboundChatMessage message) {
    public static class Adapter extends TypeAdapter<InboundPrivateMessageCreation> {
        @Override
        public void write(JsonWriter jsonWriter, InboundPrivateMessageCreation inboundPrivateMessageCreation) throws IOException {
            class GsonProvider {
                @Inject
                private Gson gson;
            }
            GsonProvider gp = new GsonProvider();

            jsonWriter.beginObject();
            jsonWriter.name("channel").value(gp.gson.toJson(inboundPrivateMessageCreation.channel()));
            jsonWriter.name("new_channel_id").value(inboundPrivateMessageCreation.newChannelId());
            jsonWriter.name("message").value(gp.gson.toJson(inboundPrivateMessageCreation.message()));
            jsonWriter.endObject();
        }

        @Override
        public InboundPrivateMessageCreation read(JsonReader jsonReader) throws IOException {
            class GsonProvider {
                @Inject
                private Gson gson;
            }
            GsonProvider gp = new GsonProvider();
            jsonReader.beginObject();
            InboundWebChatChannel channel = null;
            int newChannelId = -1;
            InboundChatMessage message = null;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "channel":
                        channel = gp.gson.fromJson(jsonReader, InboundWebChatChannel.class);
                        break;
                    case "new_channel_id":
                        newChannelId = jsonReader.nextInt();
                        break;
                    case "message":
                        message = gp.gson.fromJson(jsonReader, InboundChatMessage.class);
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new InboundPrivateMessageCreation(channel, newChannelId, message);
        }
    }
}
