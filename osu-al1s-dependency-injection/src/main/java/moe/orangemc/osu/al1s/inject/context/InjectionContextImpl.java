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

package moe.orangemc.osu.al1s.inject.context;

import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.InvalidInjectModuleException;
import moe.orangemc.osu.al1s.inject.api.Provides;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class InjectionContextImpl implements InjectionContext {
    private final InjectionContextImpl parent;
    private final Set<InjectionContextImpl> directChildren = new HashSet<>();
    private final Set<InjectionContextImpl> children = new HashSet<>();

    private final Map<String, Class<?>> providedClass = new HashMap<>();
    private final Map<Class<?>, Map<String, Object>> fieldMap = new HashMap<>();

    public InjectionContextImpl() {
        this.parent = null;
    }

    public InjectionContextImpl(InjectionContextImpl parent) {
        this.parent = parent;
        parent.addDirectChildren(this);
    }

    @Override
    public void registerModule(Object module) {
        for (Method method : module.getClass().getMethods()) {
            Provides providesAnnotation = method.getAnnotation(Provides.class);
            if (providesAnnotation == null) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                throw new InvalidInjectModuleException(module.getClass(), "Method " + method + " annotated with @Provides must have no parameters");
            }

            Class<?> clazz = method.getReturnType();
            providedClass.put(clazz.getName(), clazz);
            String name = providesAnnotation.name();
            try {
                method.setAccessible(true);
                Object value = method.invoke(module);
                Map<String, Object> map = fieldMap.computeIfAbsent(clazz, _ -> new HashMap<>());
                if (map.containsKey(name)) {
                    throw new InvalidInjectModuleException(module.getClass(), "Duplicate @Provides name: " + clazz.getName() + ":" + name);
                }

                map.put(name, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new InvalidInjectModuleException(module.getClass(), e);
            }
        }
    }

    @Override
    public Object mapField(Class<?> clazz, String name) {
        if (!fieldMap.containsKey(clazz) || !fieldMap.get(clazz).containsKey(name)) {
            if (parent != null) {
                return parent.mapField(clazz, name);
            }
            throw new NoSuchElementException("No such value in: " + clazz.getName() + ":" + name);
        }

        return fieldMap.get(clazz).get(name);
    }

    @Override
    public Class<?> getMappedClass(String name) {
        return providedClass.get(name);
    }

    @Override
    public void addExternalClass(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            providedClass.put(clazz.getName(), clazz);
        }
    }

    @Override
    public void addExternalClass(java.util.Collection<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            providedClass.put(clazz.getName(), clazz);
        }
    }

    private void addDirectChildren(InjectionContextImpl child) {
        this.directChildren.add(child);
        this.addChildren(child);

        if (parent != null) {
            parent.addChildren(child);
        }
    }

    private void addChildren(InjectionContextImpl child) {
        this.children.add(child);
    }

    public boolean isChild(InjectionContextImpl context) {
        return this.children.contains(context);
    }

    @Override
    public InjectionContextImpl getParent() {
        return parent;
    }
}
