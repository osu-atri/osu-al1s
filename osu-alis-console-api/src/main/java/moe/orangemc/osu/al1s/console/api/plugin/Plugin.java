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

package moe.orangemc.osu.al1s.console.api.plugin;

public abstract class Plugin {
    private final PluginDescriptor descriptor;
    private final PluginManager pluginManager;

    protected Plugin(PluginDescriptor descriptor, PluginManager pluginManager) {
        this.descriptor = descriptor;
        this.pluginManager = pluginManager;
    }

    public void onLoad() {}
    public void onEnable() {}
    public void onDisable() {}

    public final PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public final PluginManager getPluginManager() {
        return pluginManager;
    }

    public final boolean isEnabled() {
        return pluginManager.isPluginEnabled(this);
    }
}
