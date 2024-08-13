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

import moe.orangemc.osu.al1s.api.spi.ArisBootstrapService;
import moe.orangemc.osu.al1s.auth.CredentialProviderModule;
import moe.orangemc.osu.al1s.bot.BotFactoryModule;
import moe.orangemc.osu.al1s.inject.InjectorImpl;
import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.user.UserRequestAPIModule;
import moe.orangemc.osu.al1s.util.GsonProvider;

public class ArisBootstrapServiceImpl implements ArisBootstrapService {
    @Override
    public void boot(String init) {
        Injector injector = new InjectorImpl();

        InjectionContext ctx = injector.getCurrentContext();

        ctx.registerModule(new GsonProvider());
        ctx.registerModule(new BotFactoryModule());
        ctx.registerModule(new CredentialProviderModule());

        injector.bootstrap(init);
    }
}
