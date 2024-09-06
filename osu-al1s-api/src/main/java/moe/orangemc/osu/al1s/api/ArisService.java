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

import moe.orangemc.osu.al1s.api.spi.ArisBootstrapService;
import moe.orangemc.osu.al1s.api.spi.ArisServiceProviderRegistry;

public class ArisService {
    /**
     * Boots the main service up. External projects should refer to this class.
     * @param init the target class to be injected
     * @param args other arguments (optional)
     */
    public static void bootstrap(String init, String[] args) {
        ArisServiceProviderRegistry.get(ArisBootstrapService.class).defaultProvider().bootstrap(init, args);
    }
}
