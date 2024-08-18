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

package moe.orangemc.osu.al1s.chat;

import moe.orangemc.osu.al1s.api.chat.ChatManager;
import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.chat.ChannelChatEvent;
import moe.orangemc.osu.al1s.api.event.chat.ChatEvent;
import moe.orangemc.osu.al1s.api.event.chat.MultiplayerRoomChatEvent;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.chat.command.CommandManagerImpl;
import moe.orangemc.osu.al1s.chat.driver.ChatDriver;
import moe.orangemc.osu.al1s.chat.driver.irc.IrcDriver;
import moe.orangemc.osu.al1s.chat.driver.web.WebDriver;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.multiplayer.RoomImpl;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.LazyReference;

import java.util.HashMap;
import java.util.Map;

public class ChatManagerImpl implements ChatMessageHandler, ChatManager {
    private final Map<String, OsuChannelImpl> channelsNameMap = new HashMap<>();
    private final Map<OsuChannelImpl, String> reverseChannelsNameMap = new HashMap<>();

    private final String serverBotName;

    @Inject
    private EventBus eventBus;

    private LazyReference<ChatDriver> driver = new LazyReference<>(() -> {
        ChatDriver result = new WebDriver();
        result.setMessageHandler(this);
        return result;
    });
    private final CommandManagerImpl commandManager = new CommandManagerImpl();

    private String ircHost;
    private int ircPort;

    public ChatManagerImpl(String serverBotName) {
        this.serverBotName = serverBotName;
    }

    public void setIrcServer(String host, int port) {
        this.ircHost = host;
        this.ircPort = port;
    }

    public void authenticateIrc(IrcCredentialImpl credential) {
        driver = new LazyReference<>(() -> {
            ChatDriver result = new IrcDriver(ircHost, ircPort, credential);
            result.setMessageHandler(this);
            return result;
        });
    }

    @Override
    public void handle(String channel, UserImpl user, String message) {
        OsuChannelImpl osuChannel = channelsNameMap.get(channel);
        if (osuChannel == null) {
            return;
        }

        if (user.getUsername().equals(serverBotName)) {
            osuChannel.pushServerMessage(message);
            return;
        }

        if (message.startsWith("!") && commandManager.executeCommand(user, osuChannel, message)) {
            return;
        }

        if (osuChannel instanceof RoomImpl room) {
            eventBus.fire(new MultiplayerRoomChatEvent(user, message, room));
        } else if (osuChannel instanceof UserImpl) {
            eventBus.fire(new ChatEvent(user, message));
        } else {
            eventBus.fire(new ChannelChatEvent(user, message, osuChannel));
        }
    }

    private String findChannelName(OsuChannel channel) {
        if (reverseChannelsNameMap.containsKey(channel)) {
            return reverseChannelsNameMap.get(channel);
        }

        if (channel instanceof UserImpl user) {
            ChatDriver driver = this.driver.get();
            if (driver instanceof IrcDriver) {
                return user.getUsername().replaceAll(" ", "_");
            }
            throw new IllegalStateException("Send a message to them first!");
        }

        String channelName = channel.getChannelName();
        channelsNameMap.put(channelName, (OsuChannelImpl) channel);
        reverseChannelsNameMap.put((OsuChannelImpl) channel, channelName);
        this.driver.get().joinChannel(channelName);
        return channelName;
    }

    public void sendMessage(OsuChannel channel, String message) {
        try {
            String channelName = findChannelName(channel);
            driver.get().sendMessage(channelName, message);
        } catch (IllegalStateException e) {
            if (channel instanceof UserImpl user) {
                String channelName = driver.get().initializePrivateChannel(user, message);
                channelsNameMap.put(channelName, user);
                reverseChannelsNameMap.put(user, channelName);
            }
        }
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }
}
