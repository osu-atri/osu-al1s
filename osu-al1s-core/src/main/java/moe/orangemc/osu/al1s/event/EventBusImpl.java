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

import moe.orangemc.osu.al1s.api.event.Event;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.EventHandler;
import moe.orangemc.osu.al1s.api.event.HandlerOrder;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.event.asm.HandlerDispatcher;
import moe.orangemc.osu.al1s.event.asm.HandlerDispatcherFactory;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.util.*;

public class EventBusImpl implements EventBus {
    @Inject
    private OsuBotImpl bot;
    private final HandlerDispatcherFactory factory = new HandlerDispatcherFactory();
    private final Map<Class<?>, Set<HandlerDispatcher<?>>> handlers = new HashMap<>();

    @Override
    public void register(Object listener) {
        Class<?> listenerClass = listener.getClass();
        for (Method m : listenerClass.getMethods()) {
            if (m.isSynthetic() || m.getParameterCount() != 1 || !m.accessFlags().contains(AccessFlag.PUBLIC)) {
                continue;
            }

            EventHandler handlerAnnotation = m.getAnnotation(EventHandler.class);
            if (handlerAnnotation == null) {
                continue;
            }

            Class<?> param = m.getParameters()[0].getType();
            if (!(Event.class.isAssignableFrom(param))) {
                continue;
            }

            if (!handlers.containsKey(param)) {
                handlers.put(param, new HashSet<>());
            }

            handlers.get(param).add(factory.createDispatcher(listener, m, param, handlerAnnotation.ignoreCancelled(), handlerAnnotation.order().getId()));
        }
    }

    @Override
    public void unregister(Object listener) {
        handlers.forEach((_, evtHandlers) -> evtHandlers.removeIf(handler -> handler.getOwner() == listener));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fire(Event event) {
        Class<?> evtClass = event.getClass();

        List<Set<HandlerDispatcher<Event>>> layeredHandlers = new ArrayList<>();

        for (int i = 0; i < HandlerOrder.values().length; i++) {
            layeredHandlers.add(new HashSet<>());
        }

        while (Event.class.isAssignableFrom(evtClass)) {
            handlers.getOrDefault(evtClass, Collections.emptySet()).forEach(handler -> layeredHandlers.get(handler.getOrderIndex()).add((HandlerDispatcher<Event>) handler));
            evtClass = evtClass.getSuperclass();
        }

        for (Set<HandlerDispatcher<Event>> handlers : layeredHandlers) {
            bot.execute(() -> handlers.forEach(handler -> handler.dispatchEvent(event)));
        }
    }
}
