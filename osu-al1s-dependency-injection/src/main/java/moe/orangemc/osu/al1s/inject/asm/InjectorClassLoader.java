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

package moe.orangemc.osu.al1s.inject.asm;

import moe.orangemc.osu.al1s.inject.InjectorImpl;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.inject.util.ClassNameMatcher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InjectorClassLoader extends ClassLoader {
    private static final boolean DEBUG = false;

    private static final List<File> classPath = Stream.of(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new).toList();
    private final Map<String, Class<?>> cache = new HashMap<>();
    private final InjectorImpl injector;

    public InjectorClassLoader(ClassLoader parent, InjectorImpl injector) {
        super(parent);
        this.injector = injector;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return loadClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("moe.orangemc.osu.al1s.inject.") || injector.getCurrentContext().getMappedClass(name) != null) {
            return Class.forName(name);
        }
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        try {
            Class<?> found = null;
            for (File file : classPath) {
                if (file.isDirectory()) {
                    File classFile = new File(file, name.replace('.', File.separatorChar) + ".class");

                    if (classFile.exists()) {
                        byte[] bytes = readClassFromFile(classFile);
                        if (bytes == null) {
                            continue;
                        }

                        byte[] finalData = transform(bytes);
                        found = defineClass(name, finalData, 0, finalData.length);
                    }
                }
                if (file.getName().endsWith(".jar") || file.getName().endsWith(".war")) {
                    byte[] bytes = readClassFromJar(file, name);
                    if (bytes == null) {
                        continue;
                    }
                    byte[] finalData = transform(bytes);
                    found = defineClass(name, finalData, 0, finalData.length);
                }
            }

            if (found == null) {
                found = getParent().loadClass(name);
            }

            cache.put(name, found);
            return found;
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] transform(byte[] from) {
        ClassReader cr = new ClassReader(from);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        cr.accept(new InjectClassTransformer(new CheckClassAdapter(cw)), 0);

        byte[] clsBytes = cw.toByteArray();
        dumpClass(clsBytes);
        return clsBytes;
    }

    private byte[] readClassFromFile(File classFile) {
        try (InputStream is = new FileInputStream(classFile)) {
            return is.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] readClassFromJar(File jarFile, String name) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry(name.replace('.', '/') + ".class");
            if (entry == null) {
                return null;
            }
            try (InputStream is = jar.getInputStream(entry)) {
                return is.readAllBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Injector getInjector() {
        return injector;
    }

    private void dumpClass(byte[] data) {
        if (!DEBUG) {
            return;
        }

        try {
            File tmp = File.createTempFile("dump", ".class");
            System.out.println("Dumping class to " + tmp.getAbsolutePath());

            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
