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

import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.chat.ChatMessageHandler;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelTargetedMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;

public class IrcListener {
    @Inject
    private OsuBot bot;
    private final ChatMessageHandler handler;
    @Inject
    private Scheduler scheduler;

    public IrcListener(ChatMessageHandler handler) {
        this.handler = handler;
    }

    @Handler
    public void onChannelMessage(ChannelMessageEvent event) {
        String channelName = event.getChannel().getName();
        String message = event.getMessage();
        String sender = event.getActor().getNick();
        scheduler.runTask(() -> bot.execute(() -> handler.handle(channelName, UserImpl.get(sender), message)));
    }

    @Handler
    public void onPrivateMessage(PrivateMessageEvent event) {
        String message = event.getMessage();
        String sender = event.getActor().getNick();
        scheduler.runTask(() -> bot.execute(() -> handler.handle(sender, UserImpl.get(sender), message)));
    }

    @Handler
    public void onTargetedMessage(ChannelTargetedMessageEvent event) {
        String channelName = event.getChannel().getName();
        String message = event.getMessage();
        String sender = event.getActor().getNick();
        scheduler.runTask(() -> bot.execute(() -> handler.handle(channelName, UserImpl.get(sender), message)));
    }
}
