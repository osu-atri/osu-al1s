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

package moe.orangemc.osu.al1s.chat.command.accessor;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;

public class CommandArgumentNode {
    private Method method;
    private final Map<Class<?>, CommandArgumentNode> children = new HashMap<>();
    private final Class<?> parameter;

    private CommandArgumentNode(Class<?> parameter) {
        this.parameter = parameter;
    }

    public void addChild(Class<?> parameterType, CommandArgumentNode node) {
        children.put(parameterType, node);
    }

    public List<CommandArgumentNode> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(children.values()));
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getParameter() {
        return parameter;
    }

    public static CommandArgumentNode build(List<Method> methods) {
        CommandArgumentNode root = new CommandArgumentNode(null);
        for (Method method : methods) {
            Class<?>[] parameters = method.getParameterTypes();
            CommandArgumentNode current = root;
            for (int i = 2; i < parameters.length; i++) { // Skip 1st sender argument
                Class<?> parameter = parameters[i];
                if (parameter.isPrimitive()) {
                    parameter = MethodType.methodType(parameter).wrap().returnType();
                }
                CommandArgumentNode next = current.children.get(parameter);
                if (next == null) {
                    next = new CommandArgumentNode(parameter);
                    current.addChild(parameter, next);
                }
                current = next;
            }
            current.method = method;
        }
        return root;
    }

    @Override
    public String toString() {
        return "CommandArgumentNode{" +
                "method=" + method +
                ", children=" + children +
                ", parameter=" + parameter +
                '}';
    }
}
