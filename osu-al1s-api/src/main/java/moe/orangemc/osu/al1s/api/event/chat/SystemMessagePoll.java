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

package moe.orangemc.osu.al1s.api.event.chat;

import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.api.event.Event;

import java.util.Collections;
import java.util.List;

public class SystemMessagePoll extends Event {
    private final List<String> messages;
    private final OsuChannel channel;

    public SystemMessagePoll(List<String> messages, OsuChannel channel) {
        this.messages = messages;
        this.channel = channel;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

    public OsuChannel getChannel() {
        return channel;
    }
}
