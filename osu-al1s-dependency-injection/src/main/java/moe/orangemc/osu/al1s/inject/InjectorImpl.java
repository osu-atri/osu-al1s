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

package moe.orangemc.osu.al1s.inject;

import moe.orangemc.osu.al1s.inject.api.ContextSession;
import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.inject.asm.InjectorClassLoader;
import moe.orangemc.osu.al1s.inject.context.ContextSessionImpl;
import moe.orangemc.osu.al1s.inject.context.InjectionContextImpl;

import java.util.*;

public class InjectorImpl implements Injector {
    private final InjectionContextImpl root = new InjectionContextImpl();
    private final InjectorClassLoader classLoader = new InjectorClassLoader(getClass().getClassLoader(), this);

    private InjectionContextImpl context = root;
    private boolean selfProvided = false;

    private final Stack<InjectionContextImpl> contextStack = new Stack<>();

    @Override
    public Class<?> loadWithInjection(String rootClass) {
        if (!selfProvided) {
            selfProvided = true;
            root.registerModule(new InjectorProvider(this));
        }
        try {
            return classLoader.loadClass(rootClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not valid", e);
        }
    }

    @Override
    public InjectionContext derivativeContext() {
        return new InjectionContextImpl(context);
    }

    @Override
    public ContextSession setContext(InjectionContext context) {
        if (!(context instanceof InjectionContextImpl ctx)) {
            throw new IllegalArgumentException("Invalid context class: " + context.getClass());
        }

        if (!(root.isChild(ctx)) && root != ctx) {
            throw new IllegalArgumentException("Unclaimed context");
        }

        this.pushContext(ctx);

        return new ContextSessionImpl(this);
    }

    private void pushContext(InjectionContextImpl ctx) {
        contextStack.push(this.context);
        this.context = ctx;
    }

    public void popContext() {
        this.context = contextStack.pop();
    }

    @Override
    public InjectionContext getCurrentContext() {
        return context;
    }

    public InjectorClassLoader getClassLoader() {
        return classLoader;
    }
}
