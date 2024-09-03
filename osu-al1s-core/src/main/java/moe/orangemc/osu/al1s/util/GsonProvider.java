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

package moe.orangemc.osu.al1s.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.orangemc.osu.al1s.auth.token.ServerTokenResponse;
import moe.orangemc.osu.al1s.chat.driver.web.model.*;
import moe.orangemc.osu.al1s.chat.driver.web.model.websocket.WebsocketChatData;
import moe.orangemc.osu.al1s.chat.driver.web.model.websocket.WebsocketEvent;
import moe.orangemc.osu.al1s.inject.api.Provides;

public class GsonProvider {
    @Provides
    public Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapter(ServerTokenResponse.class, new ServerTokenResponse.Adapter())
                .registerTypeAdapter(OutboundChannelMessage.class, new OutboundChannelMessage.Adapter())
                .registerTypeAdapter(OutboundChannelJoin.class, new OutboundChannelJoin.Adapter())
                .registerTypeAdapter(OutboundInitiatePrivateMessage.class, new OutboundInitiatePrivateMessage.Adapter())
                .registerTypeAdapter(InboundWebChatChannel.class, new InboundWebChatChannel.Adapter())
                .registerTypeAdapter(InboundChatMessage.class, new InboundChatMessage.Adapter())
                .registerTypeAdapter(InboundPrivateMessageCreation.class, new InboundPrivateMessageCreation.Adapter())
                .registerTypeAdapter(WebsocketChatData.class, new WebsocketChatData.Adapter())
                .registerTypeAdapter(WebsocketEvent.class, new WebsocketEvent.Adapter())
                .create();
    }
}
