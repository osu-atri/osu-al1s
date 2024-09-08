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

import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.chat.ChatManager;
import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.chat.SystemMessagePoll;
import moe.orangemc.osu.al1s.chat.driver.BanchoBotWatchdog;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class OsuChannelImpl implements OsuChannel {
    @Inject
    private ChatManager chatManager;
    @Inject
    private EventBus eventBus;
    @Inject
    private OsuBot bot;

    private final BanchoBotWatchdog watchdog = new BanchoBotWatchdog(this);

    private final List<String> polledServerMessages = new CopyOnWriteArrayList<>();
    private final List<String> polledQueue = Collections.synchronizedList(new LinkedList<>());

    private final Lock pollLock = new ReentrantLock();
    private final Condition notEmpty = pollLock.newCondition();
    private final Condition newArrival = pollLock.newCondition();

    @Override
    public final void sendMessage(String message) {
        chatManager.sendMessage(this, message);
    }

    public final void pushServerMessage(String message) {
        this.polledServerMessages.addLast(message.replaceAll("\\s+", " ")); // remove extra spaces and tabs
        watchdog.feed();
    }

    public final void schedulePollEvent() {
        if (this.polledServerMessages.size() <= 0) {
            return;
        }

        List<String> messages = List.copyOf(this.polledServerMessages);

        bot.execute(() -> {
            polledQueue.addAll(messages);
            try {
                pollLock.lock();
                notEmpty.signalAll();
                newArrival.signalAll();
            } finally {
                pollLock.unlock();
            }
            this.processServerMessages(messages);
            eventBus.fire(new SystemMessagePoll(messages, this));
            this.polledServerMessages.clear();
        });
    }

    public void pollServerMessages(Consumer<List<String>> consumer) {
        SneakyExceptionHelper.voidCall(() -> Thread.sleep(500)); // Sleep on purpose to prevent race condition
        pollLock.lock();

        try {
            while (this.polledQueue.isEmpty()) {
                SneakyExceptionHelper.voidCall(notEmpty::await);
            }

            consumer.accept(this.polledQueue);
        } finally {
            pollLock.unlock();
        }
    }

    public List<String> waitForNewServerMessages() {
        pollLock.lock();

        try {
            while (this.polledQueue.isEmpty()) {
                SneakyExceptionHelper.voidCall(newArrival::await);
            }
        } finally {
            pollLock.unlock();
        }

        return this.polledQueue;
    }

    public void clearUnprocessedMessages() {
        this.polledServerMessages.clear();
        this.polledQueue.clear();
    }

    protected void processServerMessages(List<String> messages) {

    }
}
