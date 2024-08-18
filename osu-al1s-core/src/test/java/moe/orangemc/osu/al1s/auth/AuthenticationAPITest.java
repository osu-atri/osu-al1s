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

package moe.orangemc.osu.al1s.auth;

import moe.orangemc.osu.al1s.TestLaunchNeedle;
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.event.EventHandler;
import moe.orangemc.osu.al1s.api.event.auth.UserAuthenticationRequestEvent;
import moe.orangemc.osu.al1s.auth.credential.AuthorizationCodeGrantCredentialImpl;
import moe.orangemc.osu.al1s.auth.credential.CredentialBase;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.util.GsonProvider;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import moe.orangemc.osu.al1s.util.URLUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Scanner;

@ExtendWith(TestLaunchNeedle.class)
public class AuthenticationAPITest {
    private OsuBotImpl osuBot;

    @Inject
    private static Injector injector;
    private static InjectionContext initialContext;

    @BeforeAll
    public static void setUp() {
        injector.getCurrentContext().registerModule(new GsonProvider());
        initialContext = injector.getCurrentContext();
    }

    public void initTest() {
        injector.setContext(initialContext);
        injector.setContext(injector.derivativeContext());

        osuBot = new OsuBotImpl(true, URLUtil.newURL("http://osu.ppy.sh/"), "BanchoBot", "irc.ppy.sh", 6667);
        osuBot.getEventBus().register(new Authenticator());
    }

    @Test
    public void testAuthorizationCodeGrant() {
        initTest();
        AuthorizationCodeGrantCredentialImpl credential = new AuthorizationCodeGrantCredentialImpl();

        File tmpCredentialFile = new File("tmpCredentialFile");
        Assertions.assertTrue(tmpCredentialFile.exists(), "Credential file not found");

        SneakyExceptionHelper.voidCallAutoClose(() -> new Scanner(tmpCredentialFile), scanner -> {
            credential.setClientId(scanner.nextInt());
            scanner.nextLine();
            credential.setClientSecret(scanner.nextLine());
            credential.setRedirectUri(scanner.nextLine());
        });

        credential.setCallbackAddr(new InetSocketAddress("localhost", 4000));
        credential.setScopes(Scope.PUBLIC, Scope.IDENTIFY, Scope.CHAT.READ);

        Assertions.assertDoesNotThrow(() -> osuBot.authenticateSync(credential));
    }

    @Test
    public void testClientCredentialsGrant() {
        initTest();
        CredentialBase credentialBase = new CredentialBase();

        File tmpCredentialFile = new File("tmpCredentialFile");
        Assertions.assertTrue(tmpCredentialFile.exists(), "Credential file not found");

        SneakyExceptionHelper.voidCallAutoClose(() -> new Scanner(tmpCredentialFile), scanner -> {
            credentialBase.setClientId(scanner.nextInt());
            scanner.nextLine();
            credentialBase.setClientSecret(scanner.nextLine());
        });
        credentialBase.setScopes(Scope.PUBLIC);

        Assertions.assertDoesNotThrow(() -> osuBot.authenticateSync(credentialBase));
    }

    @Test
    public void testTokenRenewal() {
        initTest();
        AuthorizationCodeGrantCredentialImpl credential = new AuthorizationCodeGrantCredentialImpl();

        File tmpCredentialFile = new File("tmpCredentialFile");
        Assertions.assertTrue(tmpCredentialFile.exists(), "Credential file not found");

        SneakyExceptionHelper.voidCallAutoClose(() -> new Scanner(tmpCredentialFile), scanner -> {
            credential.setClientId(scanner.nextInt());
            scanner.nextLine();
            credential.setClientSecret(scanner.nextLine());
            credential.setRedirectUri(scanner.nextLine());
        });

        credential.setCallbackAddr(new InetSocketAddress("localhost", 4000));
        credential.setScopes(Scope.PUBLIC, Scope.IDENTIFY, Scope.CHAT.READ);

        osuBot.authenticateSync(credential);

        TokenImpl token = (TokenImpl) osuBot.getToken();

        Assertions.assertDoesNotThrow(token::refresh);
    }

    public static class Authenticator {
        @EventHandler
        public void authenticate(UserAuthenticationRequestEvent event) {
            System.out.println("Please go to see " + event.getRequestURL());
        }
    }
}
