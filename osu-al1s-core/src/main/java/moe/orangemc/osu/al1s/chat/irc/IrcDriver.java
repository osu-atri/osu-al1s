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

package moe.orangemc.osu.al1s.chat.irc;

import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.chat.ChatDriver;
import moe.orangemc.osu.al1s.chat.ChatMessageHandler;
import moe.orangemc.osu.al1s.user.UserImpl;
import org.kitteh.irc.client.library.Client;

public class IrcDriver implements ChatDriver {
    private final Client client;

    public IrcDriver(String host, int port, IrcCredentialImpl credential) {
        this.client = Client.builder()
                .nick(credential.getUsername())
                .realName(credential.getUsername())
                .user(credential.getUsername())
                .server()
                .host(host)
                .port(port, Client.Builder.Server.SecurityType.INSECURE)
                .password(credential.getPassword())
                .then()
                .buildAndConnect();
    }

    @Override
    public void sendMessage(String channel, String message) {
        client.sendMessage(channel, message);
    }

    @Override
    public void joinChannel(String channel) {
        client.addChannel(channel);
    }

    @Override
    public void leaveChannel(String channel) {
        client.removeChannel(channel);
    }

    @Override
    public String initializePrivateChannel(UserImpl user, String initialMessage) {
        client.sendMessage(user.getUsername(), initialMessage);
        return user.getUsername();
    }

    @Override
    public void setMessageHandler(ChatMessageHandler handler) {
        this.client.getEventManager().registerEventListener(new IrcListener(handler));
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }
}
