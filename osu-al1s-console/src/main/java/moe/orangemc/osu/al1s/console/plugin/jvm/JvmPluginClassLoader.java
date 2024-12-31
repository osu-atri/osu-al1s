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

import moe.orangemc.osu.al1s.inject.asm.InjectorClassLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JvmPluginClassLoader extends ClassLoader {
    private final InjectorClassLoader parent;
    private final JarFile target;

    private final Map<String, Class<?>> cache = new HashMap<>();

    public JvmPluginClassLoader(InjectorClassLoader parent, JarFile target) {
        super(parent);
        this.parent = parent;

        this.target = target;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        return loadClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            byte[] classData = readClass(name.replace('.', '/') + ".class");
            Class<?> loaded = defineClass(name, classData, 0, classData.length);
            this.cache.put(name, loaded);
            return loaded;
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] readClass(String entryName) throws IOException {
        JarEntry entry = target.getJarEntry(entryName);
        if (entry == null) {
            throw new FileNotFoundException(entryName);
        }

        try (InputStream is = target.getInputStream(entry)) {
            return parent.transform(is.readAllBytes());
        }
    }
}
