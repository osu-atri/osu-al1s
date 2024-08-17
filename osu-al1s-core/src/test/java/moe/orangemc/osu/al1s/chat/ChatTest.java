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

import moe.orangemc.osu.al1s.TestLaunchNeedle;
import moe.orangemc.osu.al1s.api.auth.IrcCredential;
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.event.EventHandler;
import moe.orangemc.osu.al1s.api.event.chat.ChannelChatEvent;
import moe.orangemc.osu.al1s.api.event.chat.ChatEvent;
import moe.orangemc.osu.al1s.api.event.chat.MultiplayerRoomChatEvent;
import moe.orangemc.osu.al1s.auth.AuthenticationAPITest;
import moe.orangemc.osu.al1s.auth.credential.AuthorizationCodeGrantCredentialImpl;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.util.GsonProvider;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import moe.orangemc.osu.al1s.util.URLUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.TestSkippedException;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Scanner;

@ExtendWith(TestLaunchNeedle.class)
public class ChatTest {
    private static OsuBotImpl osuBot;
    private static final boolean enabled = true;
    private static String targetUser;
    private static Thread testThread;
    private static IrcCredential ircCredential;

    @Inject
    private static Injector injector;

    @BeforeAll
    public static void setUp() {
        injector.getCurrentContext().registerModule(new GsonProvider());

        osuBot = new OsuBotImpl(true, URLUtil.newURL("https://osu.ppy.sh/"), "BanchoBot", "irc.ppy.sh", 6667);
        osuBot.getEventBus().register(new AuthenticationAPITest.Authenticator());

        AuthorizationCodeGrantCredentialImpl credential = new AuthorizationCodeGrantCredentialImpl();
        ircCredential = new IrcCredentialImpl();
        File tmpCredentialFile = new File("tmpCredentialFile");
        Assertions.assertTrue(tmpCredentialFile.exists(), "Credential file not found");

        SneakyExceptionHelper.voidCallAutoClose(() -> new Scanner(tmpCredentialFile), scanner -> {
            credential.setClientId(scanner.nextInt());
            scanner.nextLine();
            credential.setClientSecret(scanner.nextLine());
            credential.setRedirectUri(scanner.nextLine());
            ircCredential.setIrcPassword(scanner.nextLine());
            targetUser = scanner.nextLine();
        });

        credential.setCallbackAddr(new InetSocketAddress("localhost", 4000));
        credential.setScopes(Scope.PUBLIC, Scope.IDENTIFY, Scope.CHAT.READ, Scope.CHAT.WRITE, Scope.CHAT.WRITE_MANAGE);

        Assertions.assertDoesNotThrow(() -> osuBot.authenticateSync(credential));
        ircCredential.setIrcUsername(osuBot.getUsername());
        osuBot.execute(() -> osuBot.getEventBus().register(new MessageListener()));
    }

    private void checkEnabled() {
        if (!enabled) {
            throw new TestSkippedException("Test disabled");
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {

        }
        testThread = Thread.currentThread();
    }

    @Test
    public void testNewPrivateMsg() {
        checkEnabled();

        osuBot.execute(() -> {
            UserImpl target = new UserImpl(targetUser);
            target.sendMessage("Test message via web API. Please respond me.");
        });
        try {
            Thread.sleep(2147483647);
        } catch (InterruptedException e) {

        }
    }

    @Test
    public void testIrcPrivateMsg() {
        checkEnabled();

        osuBot.authenticateSync(ircCredential);

        osuBot.execute(() -> {
            UserImpl target = new UserImpl(targetUser);
            target.sendMessage("Test message via IRC. Please respond me.");
        });
        try {
            Thread.sleep(2147483647);
        } catch (InterruptedException e) {

        }
    }

    public static class MessageListener {
        @EventHandler
        public void onChat(ChatEvent event) {
            System.out.println("Received message: " + event.getMessage() + " from " + event.getSender().getUsername());
            if (testThread != null && event.getSender().getUsername().equals(targetUser)) {
                testThread.interrupt();
            }
        }

        @EventHandler
        public void onChat(MultiplayerRoomChatEvent event) {
            System.out.println("Received message: " + event.getMessage() + " from " + event.getSender().getUsername() + " in room " + event.getRoom().getName());
        }

        @EventHandler
        public void onChat(ChannelChatEvent event) {
            System.out.println("Received message: " + event.getMessage() + " from " + event.getSender().getUsername() + " in channel " + event.getChannel().getChannelName());
        }
    }
}
