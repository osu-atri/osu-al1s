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

package moe.orangemc.osu.al1s.auth.token;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.Validate;

import java.io.IOException;

public record ServerTokenResponse(String accessToken, long expires, String refreshToken) {
    public static class Adapter extends TypeAdapter<ServerTokenResponse> {
        @Override
        public void write(JsonWriter jsonWriter, ServerTokenResponse serverTokenResponse) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("access_token").value(serverTokenResponse.accessToken());
            jsonWriter.name("expires").value(serverTokenResponse.expires());
            jsonWriter.name("refresh_token").value(serverTokenResponse.refreshToken());
            jsonWriter.name("token_type").value("Bearer");
            jsonWriter.endObject();
        }

        @Override
        public ServerTokenResponse read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            String accessToken = null;
            long expires = 0;
            String refreshToken = null;
            String tokenType = null;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "access_token":
                        accessToken = jsonReader.nextString();
                        break;
                    case "expires":
                        expires = jsonReader.nextLong();
                        break;
                    case "refresh_token":
                        refreshToken = jsonReader.nextString();
                        break;
                    case "token_type":
                        tokenType = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();

            Validate.isTrue("Bearer".equals(tokenType), "Server responded with unknown token type: " + tokenType);
            return new ServerTokenResponse(accessToken, expires, refreshToken);
        }
    }
}
