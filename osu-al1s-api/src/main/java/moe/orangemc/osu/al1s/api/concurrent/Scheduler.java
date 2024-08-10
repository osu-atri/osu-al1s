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

package moe.orangemc.osu.al1s.api.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Scheduler {
    <T> Future<T> runTask(Supplier<T> task);
    Future<Void> runTask(Runnable task);

    <T> Future<T> runTaskLater(Supplier<T> task, long delay, TimeUnit unit);
    Future<Void> runTaskLater(Runnable task, long delay, TimeUnit unit);

    void runTaskTimer(Runnable task, long delay, long period, TimeUnit unit);
}
