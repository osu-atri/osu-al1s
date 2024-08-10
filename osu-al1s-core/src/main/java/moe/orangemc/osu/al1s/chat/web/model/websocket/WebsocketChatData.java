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

package moe.orangemc.osu.al1s.chat.web.model.websocket;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.chat.web.model.InboundChatMessage;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record WebsocketChatData(List<User> who, List<InboundChatMessage> messages) {
    public static class Adapter extends TypeAdapter<WebsocketChatData> {
        @Override
        public void write(JsonWriter jsonWriter, WebsocketChatData websocketChatData) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonWriter.beginObject();

            jsonWriter.name("users").beginArray();
            for (User user : websocketChatData.who()) {
                jsonWriter.beginObject();
                jsonWriter.name("id").value(user.getId());
                jsonWriter.name("username").value(user.getUsername());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();

            jsonWriter.name("messages").beginArray();
            for (InboundChatMessage message : websocketChatData.messages()) {
                jsonWriter.jsonValue(gf.gson.toJson(message));
            }
            jsonWriter.endArray();

            jsonWriter.endObject();
        }

        @Override
        public WebsocketChatData read(JsonReader jsonReader) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonReader.beginObject();
            List<User> who = new ArrayList<>();
            List<InboundChatMessage> messages = new ArrayList<>();
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "users":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            jsonReader.beginObject();
                            int id = -1;
                            String username = null;
                            while (jsonReader.hasNext()) {
                                switch (jsonReader.nextName()) {
                                    case "id":
                                        id = jsonReader.nextInt();
                                        break;
                                    case "username":
                                        username = jsonReader.nextString();
                                        break;
                                    default:
                                        jsonReader.skipValue();
                                }
                            }
                            jsonReader.endObject();
                            if (id != -1) {
                                who.add(new UserImpl(id));
                            } else {
                                who.add(new UserImpl(username));
                            }
                        }
                        jsonReader.endArray();
                        break;
                    case "messages":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            messages.add(gf.gson.fromJson(jsonReader, InboundChatMessage.class));
                        }
                        jsonReader.endArray();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new WebsocketChatData(who, messages);
        }
    }
}
