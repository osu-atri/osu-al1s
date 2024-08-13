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

package moe.orangemc.osu.al1s;

import moe.orangemc.osu.al1s.api.bot.InitEntry;
import moe.orangemc.osu.al1s.api.spi.ArisBootstrapService;
import moe.orangemc.osu.al1s.auth.CredentialProviderModule;
import moe.orangemc.osu.al1s.bot.BotFactoryModule;
import moe.orangemc.osu.al1s.inject.InjectorImpl;
import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.util.GsonProvider;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.lang.reflect.Constructor;

public class ArisBootstrapServiceImpl implements ArisBootstrapService {
    @Override
    public void boot(String init) {
        Injector injector = new InjectorImpl();

        InjectionContext ctx = injector.getCurrentContext();

        ctx.registerModule(new GsonProvider());
        ctx.registerModule(new BotFactoryModule());
        ctx.registerModule(new CredentialProviderModule());

        Class<?> initClass = injector.bootstrap(init);
        if (!InitEntry.class.isAssignableFrom(initClass)) {
            throw new IllegalArgumentException("Init class must implement InitEntry");
        }

        Constructor<?> entryConstructor;
        try {
            entryConstructor = initClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Init class must have a public no-args constructor", e);
        }

        InitEntry entry = (InitEntry) SneakyExceptionHelper.call(entryConstructor::newInstance);
        entry.main();
    }
}
