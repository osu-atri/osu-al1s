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

package moe.orangemc.osu.al1s.chat.driver.web;

import com.google.gson.Gson;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.ChatMessageHandler;
import moe.orangemc.osu.al1s.chat.driver.web.model.InboundChatMessage;
import moe.orangemc.osu.al1s.chat.driver.web.model.websocket.WebsocketEvent;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebsocketMessageReceiver implements WebSocket.Listener, AutoCloseable {
    private final ChatMessageHandler handler;

    @Inject
    private OsuBotImpl bot;
    @Inject
    private Gson gson;

    private final HttpClient hc = HttpClient.newHttpClient();
    private final WebSocket ws;

    public WebsocketMessageReceiver(ChatMessageHandler handler) {
        this.handler = handler;

        // Just can't programmatically get the notification endpoint
//        Map<String, ?> notifications = gson.fromJson(HttpUtil.get(URLUtil.concat(bot.getBaseUrl(), "api/v2/notifications")), new TypeToken<>() {});
//        URL notificationEndpoint = URLUtil.newURL(notifications.get("notification_endpoint").toString());
        ws = SneakyExceptionHelper.call(() -> hc.newWebSocketBuilder()
                .header("Authorization", ((TokenImpl) bot.getToken()).toHttpToken())
                .buildAsync(new URI("wss://notify.ppy.sh/"), this)
                .join());
        ws.sendText("{\"event\": \"chat.start\"}", true);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        System.out.println(message);
        bot.execute(() -> {
            WebsocketEvent evt = gson.fromJson(message, WebsocketEvent.class);
            if (!"chat.message.new".equals(evt.event())) {
                webSocket.request(1);
                return;
            }

            for (InboundChatMessage msg : evt.data().messages()) {
                handler.handle(String.valueOf(msg.channelId()), new UserImpl(msg.senderUid()), msg.message());
            }

            webSocket.request(1);
        });

        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        error.printStackTrace();
    }

    @Override
    public void close() throws Exception {
        ws.sendClose(WebSocket.NORMAL_CLOSURE, "{\"event\": \"chat.stop\"}");
        hc.close();
    }
}
