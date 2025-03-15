/*
 * Copyright 2025 Astro angelfish
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

package moe.orangemc.osu.al1s.console.command.builtin;

import moe.orangemc.osu.al1s.api.auth.*;
import moe.orangemc.osu.al1s.api.bot.BotFactory;
import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.console.ArisBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginCommand implements CommandBase {
    @Inject
    private ArisBotImpl arisBot;
    @Inject
    private CredentialProvider credentialProvider;
    @Inject
    private BotFactory botFactory;

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public String getDescription() {
        return "Log the bot onto a user account, and the account credentials will be saved";
    }

    @Override
    public String getUsage() {
        return "[<authenticate type> <client id> <client secret>]|[<username> <password>]";
    }

    @Command
    public void login(String authenticateType, int clientId, String clientSecret) {
        Credential cred = makeCredential(authenticateType, clientId, clientSecret);

        OsuBot bot = botFactory.build();
        bot.authenticateSync(cred);

        arisBot.addBot(bot);
        arisBot.getTokenStorage().getAvailableTokens().add(bot.getToken());
    }

    @Command
    public void logIrc(String username, String password) {
        OsuBot bot = arisBot.findBot(username);

        IrcCredential ircCredential = credentialProvider.newIrcCredential().setIrcPassword(password).setIrcUsername(username);
        bot.authenticateSync(ircCredential);

        arisBot.getTokenStorage().getAvailableIrcCredentials().add(ircCredential);
    }

    private Credential makeCredential(String authenticateType, int clientId, String clientSecret) {
        AuthenticateType type = parseAuthenticateType(authenticateType);
        Credential credential = credentialProvider.newCredential(type);

        credential.setClientId(clientId).setClientSecret(clientSecret);

        if (credential instanceof AuthorizationCodeGrantCredential authCred) {
            AtomicInteger preferredPort = new AtomicInteger(8848);
            SneakyExceptionHelper.voidCallAutoClose(() -> new ServerSocket(0), (ss) -> preferredPort.set(ss.getLocalPort()));

            authCred.setCallbackAddr(new InetSocketAddress("localhost", preferredPort.get()));
            authCred.setRedirectUri("http://localhost:" + preferredPort.get() + "/");
        }

        return credential;
    }

    private AuthenticateType parseAuthenticateType(String authenticateType) {
        return switch (authenticateType) {
            case "CLIENT_CREDENTIALS", "client_credentials" -> AuthenticateType.CLIENT_CREDENTIALS;
            case "AUTHORIZATION_CODE", "authorization_code" -> AuthenticateType.AUTHORIZATION_CODE;
            default -> throw new IllegalArgumentException("Unknown authenticate type: " + authenticateType);
        };
    }
}
