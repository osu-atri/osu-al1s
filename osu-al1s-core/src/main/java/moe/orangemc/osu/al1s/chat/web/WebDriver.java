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
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.ChatDriver;
import moe.orangemc.osu.al1s.chat.web.model.InboundWebChatChannel;
import moe.orangemc.osu.al1s.chat.web.model.OutboundChannelJoin;
import moe.orangemc.osu.al1s.chat.web.model.OutboundChannelMessage;
import moe.orangemc.osu.al1s.chat.web.model.OutboundInitiatePrivateMessage;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.HttpUtil;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WebDriver implements ChatDriver {
    private final Queue<CompletableFuture<String>> commandResponseQueue = new LinkedList<>();

    @Inject
    private OsuBotImpl bot;
    @Inject
    private Gson gson;

    @Inject(name="server-bot")
    private User banchobot;

    public WebDriver() {
        bot.getScheduler().runTaskTimer(this::keepAlive, 30, 30, TimeUnit.SECONDS);
        new WebsocketMessageReceiver(commandResponseQueue);
    }

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
    public String initializePrivateChannel(String user, String initialMessage) {
        if (!bot.getToken().getAllowedScopes().contains(Scope.CHAT.WRITE)) {
            throw new UnsupportedOperationException("Bot does not have permission to write to chat");
        }

        URL channelUrl = URLUtil.concat(bot.getBaseUrl(), "api/v2/chat/new");
        String created = HttpUtil.post(channelUrl, gson.toJson(new OutboundInitiatePrivateMessage(new UserImpl(user), initialMessage, false)), Map.of("Content-Type", "application/json"));
        return String.valueOf(gson.fromJson(created, InboundWebChatChannel.class).id());
    }

    @Override
    public String issueBanchoCommand(String command) {
        if (!bot.getToken().getAllowedScopes().contains(Scope.CHAT.WRITE)) {
            throw new UnsupportedOperationException("Bot does not have permission to write to chat");
        }

        banchobot.sendMessage(command);
        CompletableFuture<String> response = new CompletableFuture<>();
        commandResponseQueue.add(response);
        return SneakyExceptionHelper.call(response::get);
    }
}
