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

package moe.orangemc.osu.al1s.bot;

import moe.orangemc.osu.al1s.api.auth.Credential;
import moe.orangemc.osu.al1s.api.auth.Token;
import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.auth.AuthenticationAPI;
import moe.orangemc.osu.al1s.auth.AuthenticationAPIModule;
import moe.orangemc.osu.al1s.auth.credential.CredentialBase;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.event.EventBusImpl;
import moe.orangemc.osu.al1s.inject.api.*;
import moe.orangemc.osu.al1s.user.UserImpl;
import org.apache.commons.lang3.Validate;

import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OsuBotImpl implements OsuBot {
    private final boolean debug;
    private final URL baseURL;

    @Inject(when = InjectTiming.POST)
    private AuthenticationAPI authenticationAPI;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() / 4);
    private final EventBus eventBus = new EventBusImpl();

    private TokenImpl token = null;
    private User botUser = new UserImpl();

    @Inject
    private Injector injector;
    private InjectionContext ctx;

    public OsuBotImpl(boolean debug, URL baseURL) {
        this.debug = debug;
        this.baseURL = baseURL;

        ctx = injector.derivativeContext();
        ctx.registerModule(this);

        try (var _ = injector.setContext(ctx)) {
            ctx.registerModule(new AuthenticationAPIModule());
        }
    }

    @Provides
    public OsuBotImpl getBot() {
        return this;
    }

    @Override
    public Future<Void> authenticate(Credential credential) {
        FutureTask<Void> future = new FutureTask<>(() -> {
            authenticateSync(credential);
            return null;
        });
        executor.schedule(future, 0, TimeUnit.MILLISECONDS);
        return future;
    }

    @Override
    public void authenticateSync(Credential credential) {
        Validate.isTrue(token == null, "Already authenticated");
        Validate.isTrue(credential instanceof CredentialBase, "Invalid credential type");

        try (var _ = injector.setContext(ctx)) {
            this.token = authenticationAPI.authorize((CredentialBase) credential);
        }
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public <T> T getMetadata(String key) {
        return botUser.getMetadata(key);
    }

    public URL getBaseUrl() {
        return this.baseURL;
    }
}
