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

import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.chat.command.accessor.CommandExecutorFactory;

public class ConsoleSpaceCommandExecutorFactory extends CommandExecutorFactory<ConsoleSpaceCommandGeneratedExecutor> {
    @Override
    protected Class<ConsoleSpaceCommandGeneratedExecutor> getSuperClass() {
        return ConsoleSpaceCommandGeneratedExecutor.class;
    }

    @Override
    protected int getParameterStart() {
        return 0;
    }

    @Override
    protected Class<? extends CommandManager> getCommandManagerClass() {
        return ConsoleCommandManager.class;
    }
}
