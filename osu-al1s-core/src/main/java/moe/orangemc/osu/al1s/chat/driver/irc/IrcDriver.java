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

package moe.orangemc.osu.al1s.chat.driver.irc;

import io.netty.channel.Channel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.driver.ChatDriver;
import moe.orangemc.osu.al1s.chat.ChatMessageHandler;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.DefaultClient;
import org.kitteh.irc.client.library.defaults.feature.network.NettyConnection;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEstablishedEvent;

import java.lang.reflect.Field;

public class IrcDriver implements ChatDriver {
    private final Client client;

    @Inject
    private OsuBotImpl bot;

    public IrcDriver(String host, int port, IrcCredentialImpl credential) {
        var builder = Client.builder()
                .nick(credential.getUsername().replaceAll(" ", "_"))
                .server()
                .host(host)
                .port(port, Client.Builder.Server.SecurityType.INSECURE)
                .password(credential.getPassword())
                .then();
        if (bot.debug) {
            builder = builder.listeners().input(System.out::println).output(System.out::println).exception(Throwable::printStackTrace).then();
        }
        client = builder.build();
        client.getEventManager().registerEventListener(this);
        client.connect();
    }

    @Override
    public void sendMessage(String channel, String message) {
        client.sendMessage(channel, message);
        ((Client.WithManagement) client).startSending();
    }

    @Override
    public void joinChannel(String channel) {
        client.addChannel(channel);
        ((Client.WithManagement) client).startSending();
    }

    @Override
    public void leaveChannel(String channel) {
        client.removeChannel(channel);
        ((Client.WithManagement) client).startSending();
    }

    @Override
    public String initializePrivateChannel(UserImpl user, String initialMessage) {
        client.sendMessage(user.getUsername().replaceAll(" ", "_"), initialMessage);
        ((Client.WithManagement) client).startSending();
        return user.getUsername().replaceAll(" ", "_");
    }

    @Override
    public void setMessageHandler(ChatMessageHandler handler) {
        this.client.getEventManager().registerEventListener(new IrcListener(handler));
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

    @Handler
    public void onConnect(ClientConnectionEstablishedEvent event) {
        Client client = event.getClient();

        // Use bancho delimiter '\n'
        DefaultClient dc = (DefaultClient) client;
        Class<DefaultClient> clientClass = DefaultClient.class;
        Field connectionField = SneakyExceptionHelper.call(() -> {
            Field f = clientClass.getDeclaredField("connection");
            f.setAccessible(true);
            return f;
        });
        NettyConnection connection = SneakyExceptionHelper.call(() -> (NettyConnection) connectionField.get(dc));

        Field channelField = SneakyExceptionHelper.call(() -> {
            Field f = connection.getClass().getDeclaredField("channel");
            f.setAccessible(true);
            return f;
        });
        Channel channel = SneakyExceptionHelper.call(() -> (Channel) channelField.get(connection));
        channel.pipeline().replace("[INPUT] Line splitter", "[INPUT] Line splitter", new LineBasedFrameDecoder(9003));
    }
}
