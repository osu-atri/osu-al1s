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

package moe.orangemc.osu.al1s.chat.command;

import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.api.chat.command.StringReader;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.chat.OsuChannelImpl;
import moe.orangemc.osu.al1s.chat.command.accessor.UserspaceCommandExecutorFactory;
import moe.orangemc.osu.al1s.chat.command.argument.*;
import moe.orangemc.osu.al1s.user.UserImpl;

public class UserspaceCommandManager extends CommandManager {
    private final UserspaceCommandExecutorFactory executorFactory = new UserspaceCommandExecutorFactory();

    public UserspaceCommandManager() {
        registerBuiltinAdapters();
    }

    private void registerBuiltinAdapters() {
        registerAdapter(int.class, new IntegerTypeAdapter());
        registerAdapter(Integer.class, new IntegerTypeAdapter());

        registerAdapter(long.class, new LongTypeAdapter());
        registerAdapter(Long.class, new LongTypeAdapter());

        registerAdapter(User.class, new UserTypeAdapter());

        registerAdapter(double.class, new DoubleTypeAdapter());
        registerAdapter(Double.class, new DoubleTypeAdapter());

        registerAdapter(float.class, new FloatTypeAdapter());
        registerAdapter(Float.class, new FloatTypeAdapter());

        registerAdapter(boolean.class, new BooleanTypeAdapter());
        registerAdapter(Boolean.class, new BooleanTypeAdapter());

        registerAdapter(String.class, new StringTypeAdapter());
    }

    public boolean executeCommand(UserImpl sender, OsuChannelImpl where, String command) {
        StringReader reader = new StringReader(command);
        String cmdName = reader.getRootCommand().toLowerCase();
        CommandBase cmd = commandMap.get(cmdName);
        if (cmd == null) {
            return false;
        }
        try {
            executorFactory.fetchExecutor(cmd).execute(sender, where, this, reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
