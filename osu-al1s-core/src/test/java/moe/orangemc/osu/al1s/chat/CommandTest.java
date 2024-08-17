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
import moe.orangemc.osu.al1s.accessor.AccessorModule;
import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.command.CommandManagerImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.util.GsonProvider;
import moe.orangemc.osu.al1s.util.URLUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLaunchNeedle.class)
public class CommandTest {
    private static CommandManagerImpl commandManager;

    @Inject
    private static Injector injector;

    @BeforeAll
    static void setup() {
        injector.getCurrentContext().registerModule(new GsonProvider());
        injector.getCurrentContext().registerModule(new OsuBotImpl(true, URLUtil.newURL("https://osu.ppy.sh/"), "BanchoBot", "irc.ppy.sh", 6667), true);
        injector.getCurrentContext().registerModule(new AccessorModule());

        commandManager = new CommandManagerImpl();
    }

    @Test
    void testCommand() {
        MyCommand myCommand = new MyCommand();
        commandManager.registerCommand(myCommand);
        commandManager.executeCommand(null, null, "!test");
        Assertions.assertTrue(myCommand.test);
    }

    @Test
    void testCommand2() {
        MyCommand myCommand = new MyCommand();
        commandManager.registerCommand(myCommand);
        commandManager.executeCommand(null, null, "!test 5");
        Assertions.assertEquals(5, myCommand.test2);
        Assertions.assertFalse(myCommand.test);
    }

    public static class MyCommand implements CommandBase {
        private boolean test = false;
        private int test2 = 0;

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public String getUsage() {
            return "";
        }

        @Command
        public void test(User user, OsuChannel channel) {
            test = true;
        }

        @Command
        public void test2(User user, OsuChannel channel, int a) {
            test2 = a;
        }
    }
}
