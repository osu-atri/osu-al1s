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

import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.util.Objects;

public abstract class OsuChannelImpl implements OsuChannel {
    @Inject
    private ChatDriver driver;

    private String channelName = null;

    @Override
    public void sendMessage(String message) {
        if (channelName == null) {
            channelName = asInternalChannel(message);
            return;
        }

        driver.sendMessage(channelName, message);
    }

    public abstract String asInternalChannel(String initMessage);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OsuChannelImpl && ((OsuChannelImpl) obj).channelName.equals(channelName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channelName);
    }
}
