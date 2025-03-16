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

import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.console.ArisBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class HelpCommand implements CommandBase {
    private static final Logger logger = LogManager.getLogger(HelpCommand.class);

    @Inject
    private ArisBotImpl arisBot;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Get help, from this command";
    }

    @Override
    public String getUsage() {
        return "[command]";
    }

    @Command
    public void helpAllCommand() {
        CommandManager cm = arisBot.getConsoleCommandManager();

        for (CommandBase command : cm.getCommands()) {
            printCommandHelp(command);
        }
    }

    @Command
    public void helpFromCommand(String command) {
        for (CommandBase cb : arisBot.getConsoleCommandManager().getCommands(List.of(command))) {
            if (!cb.getName().equalsIgnoreCase(command)) {
                logger.info("Did you mean: {}?", cb.getName());
            }
            printCommandHelp(cb);
        }
    }

    private void printCommandHelp(CommandBase command) {
        logger.info("{}: {} -- {}", command.getName(), command.getDescription(), command.getUsage());
    }
}
