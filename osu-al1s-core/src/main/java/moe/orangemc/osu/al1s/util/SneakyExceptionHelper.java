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

package moe.orangemc.osu.al1s.util;

public class SneakyExceptionHelper {
    @SuppressWarnings("unchecked")
    public static <T extends Throwable, U> U raise(Throwable t) throws T {
        throw (T) t;
    }

    public static void voidCall(ThrowingRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            raise(t);
        }
    }

    public static <T> T call(ThrowingSupplier<T, Throwable> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            return raise(t);
        }
    }

    public static <T> void accept(ThrowingConsumer<T, Throwable> consumer, T t) {
        try {
            consumer.accept(t);
        } catch (Throwable e) {
            raise(e);
        }
    }

    public static <T, R> R apply(ThrowingFunction<T, R, Throwable> function, T t) {
        try {
            return function.apply(t);
        } catch (Throwable e) {
            return raise(e);
        }
    }

    public static <T, U extends AutoCloseable> T callAutoClose(ThrowingSupplier<U, Throwable> supplier, ThrowingFunction<U, T, Throwable> function) {
        U u = null;
        try {
            u = supplier.get();
            return function.apply(u);
        } catch (Throwable e) {
            return raise(e);
        } finally {
            if (u != null) {
                voidCall(u::close);
            }
        }
    }

    public static <T extends AutoCloseable> void voidCallAutoClose(ThrowingSupplier<T, Throwable> supplier, ThrowingConsumer<T, Throwable> consumer) {
        T t = null;
        try {
            t = supplier.get();
            consumer.accept(t);
        } catch (Throwable e) {
            raise(e);
        } finally {
            if (t != null) {
                voidCall(t::close);
            }
        }
    }

    public interface ThrowingRunnable<E extends Throwable> {
        void run() throws E;
    }

    public interface ThrowingSupplier<T, E extends Throwable> {
        T get() throws E;
    }

    public interface ThrowingConsumer<T, E extends Throwable> {
        void accept(T t) throws E;
    }

    public interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }
}
