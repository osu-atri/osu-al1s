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

package moe.orangemc.osu.al1s.event;

import moe.orangemc.osu.al1s.TestLaunchNeedle;
import moe.orangemc.osu.al1s.accessor.AccessorModule;
import moe.orangemc.osu.al1s.api.event.CancellableEvent;
import moe.orangemc.osu.al1s.api.event.Event;
import moe.orangemc.osu.al1s.api.event.EventHandler;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.event.accessor.LineNumberedMethodVisitor;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.Injector;
import moe.orangemc.osu.al1s.util.GsonProvider;
import moe.orangemc.osu.al1s.util.URLUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestLaunchNeedle.class)
class EventBusImplTest {
    private static EventBusImpl eventBus;
    private static MyObject handler;

    @Inject
    private static Injector injector;

    @BeforeAll
    static void setup() {
        injector.getCurrentContext().registerModule(new GsonProvider());
        injector.getCurrentContext().registerModule(new OsuBotImpl(true, URLUtil.newURL("https://osu.ppy.sh/"), "BanchoBot", "irc.ppy.sh", 6667), true);
        injector.getCurrentContext().registerModule(new AccessorModule());

        eventBus = new EventBusImpl();
        handler = new MyObject();

        LineNumberedMethodVisitor.debug = true;
    }

    @Test
    void register() {
        Assertions.assertDoesNotThrow(() -> {
            eventBus.register(handler);
        });
    }

    @Test
    void unregister() {
        Assertions.assertDoesNotThrow(() -> {
            eventBus.unregister(handler);
        });
    }

    @Test
    void fire() {
        Assertions.assertDoesNotThrow(() -> {
            eventBus.unregister(handler);
            eventBus.register(handler);
        });
        Assertions.assertDoesNotThrow(() -> {
            eventBus.fire(new Event() {});
        });
        Assertions.assertEquals(5, handler.val);
        Assertions.assertDoesNotThrow(() -> {
            eventBus.fire(new CancellableEvt());
        });
        Assertions.assertEquals(15, handler.val);
    }

    public static class MyObject {
        private int val = 0;

        @EventHandler
        public void onEvent(Event evt) {
            val |= 1;
        }

        @EventHandler(ignoreCancelled = true)
        public void onEventCancellable(CancellableEvt evt) {
            val |= 2;
        }

        @EventHandler(ignoreCancelled = true)
        public void onEvent2(Event evt) {
            val |= 4;
        }

        @EventHandler
        public void onEventCancellable2(CancellableEvt evt) {
            val |= 8;
        }
    }

    public static class CancellableEvt extends Event implements CancellableEvent {
        private boolean cancelled = false;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
