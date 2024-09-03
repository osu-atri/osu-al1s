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

import moe.orangemc.osu.al1s.api.event.Event;
import moe.orangemc.osu.al1s.api.user.User;

public class ChatEvent extends Event {
    private final User sender;
    private final String message;

    public ChatEvent(User sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
