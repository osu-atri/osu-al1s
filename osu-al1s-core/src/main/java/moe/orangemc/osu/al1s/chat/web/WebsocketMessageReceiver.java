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

package moe.orangemc.osu.al1s.chat.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.web.model.InboundChatMessage;
import moe.orangemc.osu.al1s.chat.web.model.websocket.WebsocketEvent;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.HttpUtil;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebsocketMessageReceiver implements WebSocket.Listener {
    private final Queue<CompletableFuture<String>> commandResponseQueue = new LinkedList<>();

    @Inject
    private OsuBotImpl bot;
    @Inject
    private Gson gson;
    @Inject(name="server-bot")
    private User banchobot;

    public WebsocketMessageReceiver(Queue<CompletableFuture<String>> commandResponseQueue) {
        Map<String, ?> notifications = gson.fromJson(HttpUtil.get(URLUtil.concat(bot.getBaseUrl(), "/api/v2/notifications")), new TypeToken<>() {});
        URL notificationEndpoint = URLUtil.newURL(notifications.get("notification_endpoint").toString());
        WebSocket receiver = SneakyExceptionHelper.callAutoClose(HttpClient::newHttpClient, client -> client.newWebSocketBuilder()
                .header("Authorization", ((TokenImpl) bot.getToken()).toHttpToken())
                .buildAsync(notificationEndpoint.toURI(), this)
                .join());
        receiver.sendText("{\"event\": \"chat.start\"}", true);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        WebsocketEvent evt = gson.fromJson(message, WebsocketEvent.class);
        if (!"chat.message.new".equals(evt.event())) {
            return null;
        }

        if (!evt.data().who().contains(banchobot)) {
            return null;
        }

        for (InboundChatMessage msg : evt.data().messages()) {
            if (msg.senderUid() == banchobot.getId() && !commandResponseQueue.isEmpty()) {
                commandResponseQueue.poll().complete(msg.message());
            }
        }

        return null;
    }
}
