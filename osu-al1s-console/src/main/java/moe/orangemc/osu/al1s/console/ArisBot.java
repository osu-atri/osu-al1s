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

package moe.orangemc.osu.al1s.console;

import moe.orangemc.osu.al1s.api.auth.IrcCredential;
import moe.orangemc.osu.al1s.api.auth.Token;
import moe.orangemc.osu.al1s.api.bot.BotFactory;
import moe.orangemc.osu.al1s.api.bot.InitEntry;
import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.bot.BotFactoryImpl;
import moe.orangemc.osu.al1s.console.plugin.PluginManagerImpl;
import moe.orangemc.osu.al1s.console.storage.Settings;
import moe.orangemc.osu.al1s.console.storage.TokenStorage;
import moe.orangemc.osu.al1s.console.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Stub class (launcher base) used by {@link KeiBootstrap}.
 */
public class ArisBot implements InitEntry {
    private boolean running = true;
    private boolean debug = false;

    private ArisConsole console;
    private TokenStorage tokenStorage;

    private final Settings settings = new Settings(new File("config.yml"));
    private final PluginManagerImpl pluginManager = new PluginManagerImpl();
    private final Set<OsuBot> bots = new HashSet<>();

    @Override
    public void main(String[] args) {
        if (Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--debug"))) {
            this.debug = true;
        }

        console = new ArisConsole(this);

        new Thread(this::runConsole).start();

        pluginManager.loadPlugins();
        tokenStorage = new TokenStorage();

        pluginManager.enableAllPlugins();

        authenticateBots();
    }

    private void runConsole() {
        Thread.currentThread().setName("ArisConsole");
        Thread.currentThread().setDaemon(true);
        console.start();
    }

    private void authenticateBots() {
        Set<OsuBot> noChatBot = authenticateFromTokenStorage();
        findMatchingIrcAndAuth(noChatBot);
    }

    private void findMatchingIrcAndAuth(Set<OsuBot> noChatBot) {
        for (IrcCredential irc : tokenStorage.getAvailableIrcCredentials()) {
            noChatBot.removeIf((bot) -> {
                if (StringUtil.osuStyleLike(bot.getUsername(), ((IrcCredentialImpl) irc).getUsername())) {
                    bot.authenticateSync(irc);
                    return true;
                }
                return false;
            });
        }
    }

    private @NotNull Set<OsuBot> authenticateFromTokenStorage() {
        BotFactory botFactory = new BotFactoryImpl();

        botFactory.withBaseURL(settings.getServerUrl())
                .withDebug(this.debug)
                .withServerBotName(settings.getServerBotName())
                .withIrcServer(settings.getIrcHost(), settings.getIrcPort());
        Set<OsuBot> noChatBot = new HashSet<>();

        for (Token tk : tokenStorage.getAvailableTokens()) {
            OsuBot bot = botFactory.build();
            bot.useToken(tk);

            bots.add(bot);
            noChatBot.add(bot);
        }
        return noChatBot;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
