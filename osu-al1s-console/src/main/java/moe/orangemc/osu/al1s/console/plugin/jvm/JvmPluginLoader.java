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

package moe.orangemc.osu.al1s.console.plugin.jvm;

import moe.orangemc.osu.al1s.console.api.plugin.Plugin;
import moe.orangemc.osu.al1s.console.api.plugin.PluginDescriptor;
import moe.orangemc.osu.al1s.console.api.plugin.PluginLoader;
import moe.orangemc.osu.al1s.console.plugin.PluginManagerImpl;
import moe.orangemc.osu.al1s.inject.asm.InjectorClassLoader;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JvmPluginLoader extends PluginLoader {
    private static final Logger logger = LogManager.getLogger(JvmPluginLoader.class);

    private final PluginManagerImpl pm;
    private final Map<Plugin, JvmPluginClassLoader> loaderMap = new HashMap<>();

    public JvmPluginLoader(PluginManagerImpl pm) {
        this.pm = pm;

        if (!(pm.getClass().getClassLoader() instanceof InjectorClassLoader)) {
            throw new IllegalArgumentException("PluginManager must be loaded by InjectorClassLoader");
        }
    }

    @Override
    public String getAcceptableSuffix() {
        return "jar";
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Plugin> T loadPlugin(File target) {
        return SneakyExceptionHelper.callAutoClose(() -> new JarFile(target), jarFile -> {
            PluginDescriptor descriptor = readPluginDescriptor(target, jarFile);
            logger.info("Loading JVM plugin: {} v{}", descriptor.name(), descriptor.version());

            JvmPluginClassLoader loader = makeClassLoader(jarFile);

            Class<T> clazz = (Class<T>) loader.loadClass(descriptor.main());
            Constructor<T> cst = clazz.getConstructor();
            T plugin = cst.newInstance();

            plugin.onLoad();

            return plugin;
        });
    }

    private JvmPluginClassLoader makeClassLoader(JarFile jf) {
        return new JvmPluginClassLoader((InjectorClassLoader) pm.getClass().getClassLoader(), jf);
    }

    private static PluginDescriptor readPluginDescriptor(File target, JarFile jarFile) {
        JarEntry yamlEntry = jarFile.getJarEntry("plugin.yml");
        if (yamlEntry == null) {
            throw new IllegalArgumentException("No plugin.yml found in " + target.getName());
        }

        return SneakyExceptionHelper.callAutoClose(() -> jarFile.getInputStream(yamlEntry), inputStream -> {
            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, PluginDescriptor.class);
        });
    }
}
