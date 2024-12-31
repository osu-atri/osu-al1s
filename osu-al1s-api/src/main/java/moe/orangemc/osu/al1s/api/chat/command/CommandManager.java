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

package moe.orangemc.osu.al1s.api.chat.command;

import moe.orangemc.osu.al1s.api.chat.command.argument.ArgumentTypeAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CommandManager {
    private final Map<Class<?>, ArgumentTypeAdapter<?>> adapterMap = new HashMap<>();
    protected final Map<String, CommandBase> commandMap = new HashMap<>();

    public final <T> void registerAdapter(Class<T> clazz, ArgumentTypeAdapter<T> adapter) {
        adapterMap.put(clazz, adapter);
    }

    public final Set<CommandBase> getCommands() {
        return commandMap.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    public Set<CommandBase> getCommands(List<String> providedArgs) {
        return getCommands();
    }

    @SuppressWarnings("unchecked")
    public final <T> ArgumentTypeAdapter<T> getAdapter(Class<T> clazz) {
        return (ArgumentTypeAdapter<T>) adapterMap.get(clazz);
    }

    public final void registerCommand(CommandBase cmd) {
        this.commandMap.put(cmd.getName().toLowerCase(), cmd);
    }
}
