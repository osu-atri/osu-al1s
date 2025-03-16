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

package moe.orangemc.osu.al1s.console;

import moe.orangemc.osu.al1s.console.command.ConsoleCommandManager;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArisConsole extends SimpleTerminalConsole {
    private static final Logger logger = LogManager.getLogger(ArisConsole.class);

    private final ArisBotImpl bot;

    public ArisConsole(ArisBotImpl bot) {
        this.bot = bot;
    }

    @Override
    protected boolean isRunning() {
        return this.bot.isRunning();
    }

    @Override
    protected void runCommand(String s) {
        try {
            // the method is also opened as an API. im lazy to open another method.
            if (!((ConsoleCommandManager) bot.getConsoleCommandManager()).executeCommand(s)) {
                logger.warn("Unknown command: {}", s);
            }
        } catch (Exception e) {
            logger.warn("Error while executing command: {}", s, e);
        }
    }

    @Override
    protected void shutdown() {
        this.bot.stop();
    }
}
