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

package moe.orangemc.osu.al1s.injecttest;

import moe.orangemc.osu.al1s.inject.InjectorImpl;
import moe.orangemc.osu.al1s.inject.api.ContextSession;
import moe.orangemc.osu.al1s.inject.api.InjectionContext;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.inject.api.Provides;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class InjectorTest {
    private Injector injector;

    @BeforeEach
    public void setUp() {
        injector = new InjectorImpl();
    }

    @Provides
    public TestProvider create() {
        return new TestProvider("test");
    }

    @Test
    public void testModuleRegistration() {
        InjectionContext injectionContext = injector.getCurrentContext();

        Assertions.assertDoesNotThrow(() -> injectionContext.registerModule(this));
        Assertions.assertEquals(((TestProvider) injectionContext.mapField(TestProvider.class, "default")).getTest(), "test");
    }

    @Test
    public void testBasicInjection() {
        InjectionContext injectionContext = injector.getCurrentContext();

        Assertions.assertDoesNotThrow(() -> injectionContext.registerModule(this));

        Class<?> injecteeClass = injector.bootstrap("moe.orangemc.osu.al1s.injecttest.TestInjectee");
        Object injectee = Assertions.assertDoesNotThrow(() -> injecteeClass.getConstructor().newInstance());

        Method m = Assertions.assertDoesNotThrow(() -> injecteeClass.getMethod("getTestProvider"));
        Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> (TestProvider) m.invoke(injectee)).getTest(), "test");
    }

    @Test
    public void testLayeredContextInjection() {
        InjectionContext firstLayer = injector.getCurrentContext();
        InjectionContext secondLayer = injector.derivativeContext();

        Assertions.assertDoesNotThrow(() -> firstLayer.registerModule(this));
        Assertions.assertDoesNotThrow(() -> secondLayer.registerModule(new Object() {
            @Provides
            public TestProvider create() {
                return new TestProvider("test2");
            }
        }));

        Class<?> injecteeClass = injector.bootstrap("moe.orangemc.osu.al1s.injecttest.TestInjectee");
        Object injectee = Assertions.assertDoesNotThrow(() -> injecteeClass.getConstructor().newInstance());

        Method m = Assertions.assertDoesNotThrow(() -> injecteeClass.getMethod("getTestProvider"));
        Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> (TestProvider) m.invoke(injectee)).getTest(), "test");

        try (ContextSession _ = injector.setContext(secondLayer)) {
            Object injectee2 = Assertions.assertDoesNotThrow(() -> injecteeClass.getConstructor().newInstance());

            Method m2 = Assertions.assertDoesNotThrow(() -> injecteeClass.getMethod("getTestProvider"));
            Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> (TestProvider) m2.invoke(injectee2)).getTest(), "test2");
        }

        Object injectee3 = Assertions.assertDoesNotThrow(() -> injecteeClass.getConstructor().newInstance());

        Method m3 = Assertions.assertDoesNotThrow(() -> injecteeClass.getMethod("getTestProvider"));
        Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> (TestProvider) m3.invoke(injectee3)).getTest(), "test");
    }

    @Test
    public void testLayeredContextFallback() {
        InjectionContext firstLayer = injector.getCurrentContext();
        InjectionContext secondLayer = injector.derivativeContext();

        Assertions.assertDoesNotThrow(() -> firstLayer.registerModule(this));

        Class<?> injecteeClass = injector.bootstrap("moe.orangemc.osu.al1s.injecttest.TestInjectee");
        Object injectee = Assertions.assertDoesNotThrow(() -> injecteeClass.getConstructor().newInstance());

        Method m = Assertions.assertDoesNotThrow(() -> injecteeClass.getMethod("getTestProvider"));
        Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> (TestProvider) m.invoke(injectee)).getTest(), "test");

        try (ContextSession _ = injector.setContext(secondLayer)) {
            Object injectee2 = Assertions.assertDoesNotThrow(() -> injecteeClass.getConstructor().newInstance());

            Method m2 = Assertions.assertDoesNotThrow(() -> injecteeClass.getMethod("getTestProvider"));
            Assertions.assertEquals(Assertions.assertDoesNotThrow(() -> (TestProvider) m2.invoke(injectee2)).getTest(), "test");
        }
    }
}
