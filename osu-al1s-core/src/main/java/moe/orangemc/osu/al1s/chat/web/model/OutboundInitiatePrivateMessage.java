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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.user.UserImpl;

import java.io.IOException;

public record OutboundInitiatePrivateMessage(User target, String message, boolean action) {
    public static class Adapter extends TypeAdapter<OutboundInitiatePrivateMessage> {
        @Override
        public void write(JsonWriter jsonWriter, OutboundInitiatePrivateMessage outboundInitiatePrivateMessage) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("target_id").value(outboundInitiatePrivateMessage.target().getId());
            jsonWriter.name("message").value(outboundInitiatePrivateMessage.message());
            jsonWriter.name("is_action").value(outboundInitiatePrivateMessage.action());
            jsonWriter.endObject();
        }

        @Override
        public OutboundInitiatePrivateMessage read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            int target = -1;
            String message = null;
            boolean action = false;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "target_id":
                        target = jsonReader.nextInt();
                        break;
                    case "message":
                        message = jsonReader.nextString();
                        break;
                    case "is_action":
                        action = jsonReader.nextBoolean();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new OutboundInitiatePrivateMessage(new UserImpl(target), message, action);
        }
    }
}
