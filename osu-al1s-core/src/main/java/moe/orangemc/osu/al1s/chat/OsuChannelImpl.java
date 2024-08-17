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
import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.chat.SystemMessagePoll;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class OsuChannelImpl implements OsuChannel {
    @Inject
    private ChatManager chatManager;

    @Inject
    private EventBus eventBus;

    @Inject
    private Scheduler scheduler;

    private final List<String> polledServerMessages = new ArrayList<>();

    public OsuChannelImpl() {
        scheduler.runTaskTimer(this::schedulePollEvent, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void sendMessage(String message) {
        chatManager.sendMessage(this, message);
    }

    public void pushServerMessage(String message) {
        this.polledServerMessages.addLast(message);
    }

    public void schedulePollEvent() {
        if (this.polledServerMessages.size() <= 0) {
            return;
        }

        eventBus.fire(new SystemMessagePoll(Collections.unmodifiableList(new ArrayList<>(this.polledServerMessages)), this));
        this.polledServerMessages.clear();
    }
}
