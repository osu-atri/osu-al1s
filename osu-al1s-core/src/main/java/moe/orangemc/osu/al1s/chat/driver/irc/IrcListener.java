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

import moe.orangemc.osu.al1s.chat.ChatMessageHandler;
import moe.orangemc.osu.al1s.user.UserImpl;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;

public class IrcListener {
    private final ChatMessageHandler handler;

    public IrcListener(ChatMessageHandler handler) {
        this.handler = handler;
    }

    @Handler
    public void onChannelMessage(ChannelMessageEvent event) {
        String channelName = event.getChannel().getName();
        String message = event.getMessage();
        String sender = event.getActor().getName();
        handler.handle(channelName, new UserImpl(sender), message);
    }

    @Handler
    public void onPrivateMessage(PrivateMessageEvent event) {
        String message = event.getMessage();
        String sender = event.getActor().getName();
        handler.handle(sender, new UserImpl(sender), message);
    }
}
