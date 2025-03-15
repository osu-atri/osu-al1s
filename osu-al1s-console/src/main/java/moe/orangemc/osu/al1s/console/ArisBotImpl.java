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
import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.auth.CredentialProviderModule;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.bot.BotFactoryImpl;
import moe.orangemc.osu.al1s.console.api.ArisBot;
import moe.orangemc.osu.al1s.console.api.plugin.PluginManager;
import moe.orangemc.osu.al1s.console.command.ConsoleCommandManager;
import moe.orangemc.osu.al1s.console.command.builtin.LoginCommand;
import moe.orangemc.osu.al1s.console.command.builtin.LogoutCommand;
import moe.orangemc.osu.al1s.console.plugin.PluginManagerImpl;
import moe.orangemc.osu.al1s.console.storage.Settings;
import moe.orangemc.osu.al1s.console.storage.TokenStorage;
import moe.orangemc.osu.al1s.console.util.StringUtil;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.inject.api.Provides;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Stub class (launcher base) used by {@link KeiBootstrap}.
 */
public class ArisBotImpl implements InitEntry, ArisBot {
    private boolean running = true;
    private boolean debug = false;

    @Inject
    private Injector injector;
    private ArisConsole console;
    private TokenStorage tokenStorage;
    private BotFactory botFactory;

    private final Settings settings = new Settings(new File("config.yml"));
    private final PluginManagerImpl pluginManager = new PluginManagerImpl();
    private final Set<OsuBot> bots = new HashSet<>();

    private final CommandManager consoleCommandManager = new ConsoleCommandManager();

    @Override
    public void main(String[] args) {
        if (Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--debug"))) {
            this.debug = true;
        }

        initiateBotFactory();
        initiateInjectionContext();

        initiateConsole();

        pluginManager.loadPlugins();
        tokenStorage = new TokenStorage();

        registerBuiltinConsoleCommands();

        pluginManager.enableAllPlugins();

        authenticateBots();
    }

    private void initiateConsole() {
        console = new ArisConsole(this);

        new Thread(this::runConsole).start();
    }

    private void registerBuiltinConsoleCommands() {
        consoleCommandManager.registerCommand(new LoginCommand());
        consoleCommandManager.registerCommand(new LogoutCommand());
    }

    private void initiateBotFactory() {
        botFactory = new BotFactoryImpl()
                .withBaseURL(settings.getServerUrl())
                .withDebug(this.debug)
                .withServerBotName(settings.getServerBotName())
                .withIrcServer(settings.getIrcHost(), settings.getIrcPort());
    }

    private void initiateInjectionContext() {
        injector.getCurrentContext().registerModule(this.new Provider());
        injector.getCurrentContext().registerModule(new CredentialProviderModule());
        injector.getCurrentContext().registerModule(this.new BotFactoryProvider());
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

        this.tokenStorage.save();
    }

    public TokenStorage getTokenStorage() {
        return tokenStorage;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void addBot(OsuBot bot) {
        this.bots.add(bot);
    }

    @Override
    public OsuBot findBot(String username) {
        return this.bots.stream()
                .filter(bot -> StringUtil.osuStyleLike(bot.getUsername(), username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No bot found for username: " + username));
    }

    @Override
    public void removeBot(OsuBot bot) {
        this.bots.remove(bot);
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public class Provider {
        @Provides
        public ArisBotImpl provideAris() {
            return ArisBotImpl.this;
        }
    }

    public class BotFactoryProvider {
        @Provides
        public BotFactory provideBotFactory() {
            return ArisBotImpl.this.botFactory;
        }
    }

    public class InterfaceProvider {
        @Provides
        public ArisBot provideArisBot() {
            return ArisBotImpl.this;
        }
    }
}
