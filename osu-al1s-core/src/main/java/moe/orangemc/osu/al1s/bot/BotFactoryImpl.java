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

package moe.orangemc.osu.al1s.bot;

import moe.orangemc.osu.al1s.api.bot.BotFactory;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.net.URI;
import java.net.URL;

public class BotFactoryImpl implements BotFactory {
    private URL baseUrl;
    private boolean debug;

    private String serverBotName;

    private String ircHost;
    private int ircPort;

    public BotFactoryImpl() {
        SneakyExceptionHelper.voidCall(() -> withBaseURL(new URI("https://osu.ppy.sh/").toURL())
                .withDebug(false)
                .withServerBotName("BanchoBot")
                .withIrcServer("irc.ppy.sh", 6667)
        );
    }

    @Override
    public BotFactory withBaseURL(URL baseURL) {
        return this;
    }

    @Override
    public BotFactory withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public BotFactory withServerBotName(String serverBotName) {
        this.serverBotName = serverBotName;
        return this;
    }

    @Override
    public BotFactory withIrcServer(String host, int port) {
        this.ircHost = host;
        this.ircPort = port;
        return this;
    }

    @Override
    public BotFactory withIrcServer(String host) {
        this.ircHost = host;
        return this;
    }

    @Override
    public BotFactory withIrcServer(int port) {
        this.ircPort = port;
        return this;
    }
}
