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

package moe.orangemc.osu.al1s.chat.driver;

import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.chat.OsuChannelImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BanchoBotWatchdog implements Runnable {
    @Inject
    private Scheduler scheduler;

    private final AtomicInteger timer = new AtomicInteger();

    private final OsuChannelImpl channel;

    public BanchoBotWatchdog(OsuChannelImpl channel) {
        this.channel = channel;
        scheduler.runTaskTimer(this, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void feed() {
        timer.set(0);
    }

    @Override
    public void run() {
        if (timer.getAndIncrement() > 1000) {
            new Thread(channel::schedulePollEvent).start();
            timer.set(0);
        }
    }
}
