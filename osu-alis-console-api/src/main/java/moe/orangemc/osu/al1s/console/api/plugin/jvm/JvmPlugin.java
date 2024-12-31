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

package moe.orangemc.osu.al1s.console.api.plugin.jvm;

import moe.orangemc.osu.al1s.console.api.plugin.Plugin;
import moe.orangemc.osu.al1s.console.api.plugin.PluginDescriptor;

import java.io.File;

public abstract class JvmPlugin implements Plugin {
    private PluginDescriptor descriptor;
    private File dataFolder;

    private boolean enabled = false;

    public final void load(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
        this.dataFolder = new File("plugins/" + descriptor.name());

        if (!getClass().getName().equals(descriptor.main())) {
            throw new IllegalArgumentException("Main class name does not match the plugin descriptor");
        }

        onLoad();
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public final File getDataFolder() {
        if (!dataFolder.isDirectory()) {
            dataFolder.delete();
            dataFolder.mkdirs();
        }

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        return this.dataFolder;
    }

    public final PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public final boolean isEnabled() {
        return enabled;
    }
}
