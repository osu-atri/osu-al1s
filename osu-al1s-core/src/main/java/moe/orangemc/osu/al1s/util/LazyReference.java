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

import java.util.function.Function;
import java.util.function.Supplier;

public class LazyReference<T> {
    private T value;
    private final Supplier<T> initializer;
    private final Function<T, Boolean> validator;

    public LazyReference(Supplier<T> initializer) {
        this(initializer, _ -> true);
    }

    public LazyReference(Supplier<T> initializer, Function<T, Boolean> validator) {
        this.initializer = initializer;
        this.validator = validator;
    }

    public T get() {
        if (value == null || !validator.apply(value)) {
            value = initializer.get();
        }
        return value;
    }
}
