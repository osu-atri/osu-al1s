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
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.util.*;

public abstract class OsuChannelImpl implements OsuChannel {
    @Inject
    private ChatManager chatManager;

    private final Map<Long, List<String>> serverMessages = new HashMap<>();

    @Override
    public void sendMessage(String message) {
        chatManager.sendMessage(this, message);
    }

    public void pushServerMessage(String message) {
        long time = System.currentTimeMillis() / 1000;
        serverMessages.computeIfAbsent(time, _ -> new ArrayList<>()).add(message);
    }

    @Override
    public List<String> getServerMessages(long time) {
        return Collections.unmodifiableList(serverMessages.getOrDefault(time, Collections.emptyList()));
    }

    @Override
    public List<String> getLatestServerMessages() {
        long latestTime = serverMessages.keySet().stream().max(Long::compareTo).orElse(0L);
        return getServerMessages(latestTime);
    }

    @Override
    public void clearServerMessages() {
        serverMessages.clear();
    }
}
