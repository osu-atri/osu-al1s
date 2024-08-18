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

package moe.orangemc.osu.al1s.api.bot;

import moe.orangemc.osu.al1s.api.auth.Credential;
import moe.orangemc.osu.al1s.api.auth.IrcCredential;
import moe.orangemc.osu.al1s.api.auth.Token;
import moe.orangemc.osu.al1s.api.chat.ChatManager;
import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.user.User;

import java.util.concurrent.Future;

public interface OsuBot extends User {
    Future<Void> authenticate(Credential credential);
    void authenticateSync(Credential credential);

    Future<Void> authenticate(IrcCredential credential);
    void authenticateSync(IrcCredential credential);

    EventBus getEventBus();
    Scheduler getScheduler();
    ChatManager getChatManager();

    Token getToken();
    void useToken(Token token);

    void execute(Runnable runnable);

    @Override
    default String getChannelName() {
        throw new UnsupportedOperationException("I'm a osu!bot");
    }

    default void sendMessage(String message) {
        throw new UnsupportedOperationException("I cannot chat to myself");
    }
}
