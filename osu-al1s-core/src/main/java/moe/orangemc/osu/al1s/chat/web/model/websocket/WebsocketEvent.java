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
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.IOException;

public record WebsocketEvent(String event, WebsocketChatData data) {
    public static class Adapter extends TypeAdapter<WebsocketEvent> {
        @Override
        public void write(JsonWriter jsonWriter, WebsocketEvent websocketEvent) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();

            jsonWriter.beginObject();
            jsonWriter.name("event").value(websocketEvent.event());
            jsonWriter.name("data").jsonValue(gf.gson.toJson(websocketEvent.data()));
            jsonWriter.endObject();
        }

        @Override
        public WebsocketEvent read(JsonReader jsonReader) throws IOException {
            class GsonFetch {
                @Inject
                private Gson gson;
            }
            GsonFetch gf = new GsonFetch();
            jsonReader.beginObject();
            String event = null;
            WebsocketChatData data = null;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "event":
                        event = jsonReader.nextString();
                        break;
                    case "data":
                        try {
                            data = gf.gson.fromJson(jsonReader, WebsocketChatData.class);
                        } catch (Exception e) {
                            e.printStackTrace(); // TODO: Remove
                            jsonReader.skipValue();
                            data = null;
                        }
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new WebsocketEvent(event, data);
        }
    }
}
