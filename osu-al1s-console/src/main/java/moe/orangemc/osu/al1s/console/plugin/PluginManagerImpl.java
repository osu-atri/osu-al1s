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

package moe.orangemc.osu.al1s.console.plugin;

import moe.orangemc.osu.al1s.console.api.plugin.Plugin;
import moe.orangemc.osu.al1s.console.api.plugin.PluginLoader;
import moe.orangemc.osu.al1s.console.api.plugin.PluginManager;
import moe.orangemc.osu.al1s.inject.api.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class PluginManagerImpl implements PluginManager {
    private static final Logger logger = LogManager.getLogger(PluginManagerImpl.class);

    @Inject(name = "cwd")
    private File cwd;

    private final File pluginDir;

    private final Set<Plugin> loadedPlugins = new HashSet<>();
    private final Set<PluginLoader> loaders = new HashSet<>();

    private final Set<Plugin> enabledPlugins = new HashSet<>();

    private final Map<String, Plugin> pluginNameMap = new HashMap<>();

    public PluginManagerImpl() {
        this.pluginDir = new File(cwd, "plugins");
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Plugin> T getPlugin(String id) {
        if (!pluginNameMap.containsKey(id)) {
            throw new NoSuchElementException(id);
        }

        return (T) pluginNameMap.get(id);
    }

    @Override
    public <T extends Plugin> T loadPlugin(File file) {
        for (PluginLoader loader : loaders) {
            if (file.getName().endsWith("." + loader.getAcceptableSuffix())) {
                T plugin = loader.loadPlugin(file);

                if (pluginNameMap.containsKey(plugin.getDescriptor().name())) {
                    throw new IllegalArgumentException("Duplicated plugin id: " + plugin.getDescriptor().name());
                }
                pluginNameMap.put(plugin.getDescriptor().name(), plugin);
            }
        }

        throw new IllegalArgumentException("No suitable loader found for " + file.getName());
    }

    @Override
    public void disablePlugin(String id) {
        this.disablePlugin(getPlugin(id));
    }

    @Override
    public <T extends Plugin> void disablePlugin(T plugin) {
        if (!enabledPlugins.contains(plugin)) {
            return;
        }

        logger.info("Disabling plugin " + plugin.getDescriptor().name());

        plugin.onDisable();
        enabledPlugins.remove(plugin);
    }

    @Override
    public void enablePlugin(String id) {
        this.enablePlugin(getPlugin(id));
    }

    @Override
    public <T extends Plugin> void enablePlugin(T plugin) {
        if (enabledPlugins.contains(plugin)) {
            return;
        }

        logger.info("Enabling plugin: " + plugin.getDescriptor().name());

        plugin.onEnable();
        enabledPlugins.add(plugin);
    }

    @Override
    public void registerPluginLoader(PluginLoader loader) {
        this.loaders.add(loader);
    }

    @Override
    public boolean isPluginEnabled(String id) {
        if (!pluginNameMap.containsKey(id)) {
            throw new IllegalArgumentException("No such plugin: " + id);
        }

        return isPluginEnabled(pluginNameMap.get(id));
    }

    @Override
    public <T extends Plugin> boolean isPluginEnabled(T plugin) { // ok why don't use Plugin#isEnabled.
        return enabledPlugins.contains(plugin);
    }

    public void loadPlugins() {
        File[] files = pluginDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                try {
                    loadedPlugins.add(loadPlugin(file));
                } catch (IllegalArgumentException e) {
                    logger.error("Error while loading plugin from file: {}", file.getName(), e);
                }
            }
        }
    }

    public Map<String, Plugin> getPluginNameMap() {
        return Collections.unmodifiableMap(pluginNameMap);
    }

    public void enableAllPlugins() {
        for (Plugin plugin : loadedPlugins) {
            enablePlugin(plugin);
        }
    }
}
