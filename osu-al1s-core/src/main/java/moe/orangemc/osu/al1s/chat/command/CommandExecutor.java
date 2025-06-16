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

package moe.orangemc.osu.al1s.chat.command;

import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.api.chat.command.CommandManager;
import moe.orangemc.osu.al1s.api.chat.command.StringReader;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandExecutor {
    public static void callCommand(CommandManager manager, CommandBase cmd, StringReader reader, Object... initialObjects) {
        List<Object> parameterList = new ArrayList<>(List.of(initialObjects));

        Class<?>[] parameterTypes;
        Method chosenMethod = null;

        List<Method> methodsToVisit = Arrays.stream(cmd.getClass().getMethods()).filter(m -> m.getAnnotation(Command.class) != null && (m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC).toList();
        reader.mark();
        for (Method m : methodsToVisit) {
            parameterTypes = m.getParameterTypes();
            try {
                for (int i = initialObjects.length; i < parameterTypes.length; i++) {
                    reader.skip();
                    parameterList.add(manager.getAdapter(parameterTypes[i]).parse(reader));
                }
                chosenMethod = m;
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                parameterList = new ArrayList<>(List.of(initialObjects));
                reader.reset();
            }
        }
        if (chosenMethod == null) {
            throw new IllegalArgumentException("No command matching found for input: " + reader);
        }

        Method finalChosenMethod = chosenMethod;
        List<Object> finalParameterList = parameterList;
        SneakyExceptionHelper.call(() -> finalChosenMethod.invoke(cmd, finalParameterList.toArray()));
    }
}
