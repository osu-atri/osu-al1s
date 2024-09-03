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

package moe.orangemc.osu.al1s.concurrent;

import moe.orangemc.osu.al1s.api.concurrent.Scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SchedulerImpl implements Scheduler {
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() / 2);

    public SchedulerImpl() {
        executor.setKeepAliveTime(5, TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(true);
    }

    @Override
    public <T> Future<T> runTask(Supplier<T> task) {
        return executor.schedule(() -> {
            try {
                return task.get();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }, 1, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<Void> runTask(Runnable task) {
        return (Future<Void>) executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> Future<T> runTaskLater(Supplier<T> task, long delay, TimeUnit unit) {
        return executor.schedule(() -> {
            try {
                return task.get();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }, delay, unit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<Void> runTaskLater(Runnable task, long delay, TimeUnit unit) {
        return (Future<Void>) executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }, delay, unit);
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, delay, period, unit);
    }
}
