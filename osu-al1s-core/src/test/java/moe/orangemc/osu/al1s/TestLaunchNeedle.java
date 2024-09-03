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

package moe.orangemc.osu.al1s;

import moe.orangemc.osu.al1s.inject.InjectorImpl;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class TestLaunchNeedle implements InvocationInterceptor {
    private static InjectorImpl injector = new InjectorImpl();
    private final Map<Class<?>, Object> testInstances = new HashMap<>();

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        return interceptConstructor(invocation, invocationContext);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        injector = new InjectorImpl();
        testInstances.clear();
        intercept(invocation, invocationContext);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        intercept(invocation, invocationContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        intercept(invocation, invocationContext);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        return intercept(invocation, invocationContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        intercept(invocation, invocationContext);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        intercept(invocation, invocationContext);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        intercept(invocation, invocationContext);
    }

    @SuppressWarnings("unchecked")
    private <T> T interceptConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext) throws Throwable {
        invocation.skip();

        ClassLoader currentThreadPreviousClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader injectorClassLoader = injector.getClassLoader();
        Thread.currentThread().setContextClassLoader(injectorClassLoader);

        try {
            Class<T> testClass = (Class<T>) injector.loadWithInjection(invocationContext.getExecutable().getDeclaringClass().getName());
            Constructor<T> testConstructor = testClass.getDeclaredConstructor();
            testConstructor.setAccessible(true);
            T instance = testConstructor.newInstance();
            testInstances.put(testClass, instance);
            return instance;
        } finally {
            Thread.currentThread().setContextClassLoader(currentThreadPreviousClassLoader);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T intercept(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext) throws Throwable {
        invocation.skip();

        ClassLoader currentThreadPreviousClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader injectorClassLoader = injector.getClassLoader();
        Thread.currentThread().setContextClassLoader(injectorClassLoader);

        try {
            Class<?> testClass = injector.loadWithInjection(invocationContext.getExecutable().getDeclaringClass().getName());
            Method testMethod = testClass.getDeclaredMethod(invocationContext.getExecutable().getName());
            testMethod.setAccessible(true);
            if ((testMethod.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                return (T) testMethod.invoke(null);
            } else {
                return (T) testMethod.invoke(testInstances.get(testClass));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentThreadPreviousClassLoader);
        }
    }
}
