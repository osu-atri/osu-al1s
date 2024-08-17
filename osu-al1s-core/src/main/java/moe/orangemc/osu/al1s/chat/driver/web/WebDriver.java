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
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.driver.ChatDriver;
import moe.orangemc.osu.al1s.chat.ChatMessageHandler;
import moe.orangemc.osu.al1s.chat.driver.web.model.*;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.HttpUtil;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebDriver implements ChatDriver {
    @Inject
    private OsuBotImpl bot;
    @Inject
    private Gson gson;

    private final Set<WebsocketMessageReceiver> receivers = new HashSet<>();

    private void keepAlive() {
        URL keepAliveUrl = URLUtil.concat(bot.getBaseUrl(), "api/v2/chat/ack");
        HttpUtil.post(keepAliveUrl, "{}", Map.of("Content-Type", "application/json"));
    }

    @Override
    public void sendMessage(String channel, String message) {
        if (!bot.getToken().getAllowedScopes().contains(Scope.CHAT.WRITE)) {
            throw new UnsupportedOperationException("Bot does not have permission to write to chat");
        }

        URL channelUrl = URLUtil.concat(bot.getBaseUrl(), "api/v2/chat/channels/" + channel + "/messages");
        HttpUtil.post(channelUrl, gson.toJson(new OutboundChannelMessage(message, false)), Map.of("Content-Type", "application/json"));
    }

    @Override
    public void joinChannel(String channel) {
        if (!bot.getToken().getAllowedScopes().contains(Scope.CHAT.WRITE_MANAGE)) {
            throw new UnsupportedOperationException("Bot does not have permission to manage chat");
        }

        String username = bot.getMetadata("username");
        URL channelUrl = URLUtil.concat(bot.getBaseUrl(), "api/v2/chat/channels/" + channel + "/users/" + username);
        HttpUtil.put(channelUrl, gson.toJson(new OutboundChannelJoin(username, channel)), Map.of("Content-Type", "application/json"));
    }

    @Override
    public void leaveChannel(String channel) {
        if (!bot.getToken().getAllowedScopes().contains(Scope.CHAT.WRITE_MANAGE)) {
            throw new UnsupportedOperationException("Bot does not have permission to manage chat");
        }

        String username = bot.getMetadata("username");
        URL channelUrl = URLUtil.concat(bot.getBaseUrl(), "api/v2/chat/channels/" + channel + "/users/" + username);
        HttpUtil.delete(channelUrl, gson.toJson(new OutboundChannelJoin(username, channel)), Map.of("Content-Type", "application/json"));
    }

    @Override
    public String initializePrivateChannel(UserImpl user, String initialMessage) {
        if (!bot.getToken().getAllowedScopes().contains(Scope.CHAT.WRITE)) {
            throw new UnsupportedOperationException("Bot does not have permission to write to chat");
        }

        URL channelUrl = URLUtil.concat(bot.getBaseUrl(), "api/v2/chat/new");
        String created = HttpUtil.post(channelUrl, gson.toJson(new OutboundInitiatePrivateMessage(user, initialMessage, false)), Map.of("Content-Type", "application/json"));
        return String.valueOf(gson.fromJson(created, InboundPrivateMessageCreation.class).newChannelId());
    }

    @Override
    public void setMessageHandler(ChatMessageHandler handler) {
        receivers.add(new WebsocketMessageReceiver(handler));
    }

    @Override
    public void shutdown() {
        for (WebsocketMessageReceiver receiver : receivers) {
            try {
                receiver.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
