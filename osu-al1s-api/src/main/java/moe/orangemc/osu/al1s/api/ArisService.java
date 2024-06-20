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

package moe.orangemc.osu.al1s.api;

import moe.orangemc.osu.al1s.api.bot.BotFactory;
import moe.orangemc.osu.al1s.spi.ArisBootstrapService;
import moe.orangemc.osu.al1s.spi.ArisServiceProviderRegistry;
import moe.orangemc.osu.al1s.spi.BotFactoryProvider;

public class ArisService {
    static {
        ArisServiceProviderRegistry.get(ArisBootstrapService.class).defaultProvider().boot();
    }

    public static BotFactory newBotFactory() {
        return ArisServiceProviderRegistry.get(BotFactoryProvider.class).defaultProvider().newBotFactory();
    }
}
