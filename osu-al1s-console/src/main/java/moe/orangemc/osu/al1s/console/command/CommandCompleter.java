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

package moe.orangemc.osu.al1s.console.command;

import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Set;

public class CommandCompleter implements Completer {
    private final ConsoleCommandManager consoleCommandManager;

    public CommandCompleter(ConsoleCommandManager consoleCommandManager) {
        this.consoleCommandManager = consoleCommandManager;
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        Set<CommandBase> commands = consoleCommandManager.getCommands(parsedLine.words());
        for (CommandBase command : commands) {
            list.add(new Candidate(command.getName()));
        }
    }
}
