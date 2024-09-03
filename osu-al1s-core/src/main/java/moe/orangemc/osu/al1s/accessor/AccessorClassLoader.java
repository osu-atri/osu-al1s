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

package moe.orangemc.osu.al1s.accessor;

import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.event.accessor.GeneratedHandlerDispatcher;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AccessorClassLoader extends ClassLoader {
    private final Map<String, Class<?>> madeClassCache = new HashMap<>();

    @Inject
    private OsuBotImpl osuBot;

    public AccessorClassLoader() {
        super(GeneratedHandlerDispatcher.class.getClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (madeClassCache.containsKey(name)) {
            return madeClassCache.get(name);
        }

        return Class.forName(name);
    }

    @Override
    protected Class<?> findClass(String moduleName, String name) {
        try {
            return this.findClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private void dumpClass(byte[] data) {
        if (!osuBot.debug) {
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

    public Class<?> makeClass(String name, byte[] classBytes) {
        if (madeClassCache.containsKey(name)) {
            return madeClassCache.get(name);
        }

        dumpClass(classBytes);
        Class<?> cls = this.defineClass(name, classBytes, 0, classBytes.length);
        madeClassCache.put(name, cls);
        return cls;
    }
}
