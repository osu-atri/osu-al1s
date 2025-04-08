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
import moe.orangemc.osu.al1s.console.api.plugin.PluginManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class JvmPlugin extends Plugin {
    private File dataFolder;

    protected JvmPlugin(PluginDescriptor descriptor, PluginManager pluginManager) {
        super(descriptor, pluginManager);
    }

    public final void load() {
        this.dataFolder = new File("plugins/" + getDescriptor().name());

        if (!getClass().getName().equals(getDescriptor().main())) {
            throw new IllegalArgumentException("Main class name does not match the plugin descriptor");
        }

        onLoad();
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

    public final void saveResource(String res) {
        File file = new File(getDataFolder(), res);
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/" + res)) {
                if (in == null) {
                    throw new IllegalArgumentException("Resource not found: " + res);
                }
                file.getParentFile().mkdirs();
                try (FileOutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to save resource: " + res, e);
            }
        }
    }
}
