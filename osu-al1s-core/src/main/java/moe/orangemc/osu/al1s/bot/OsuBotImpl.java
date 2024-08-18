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

import moe.orangemc.osu.al1s.accessor.AccessorModule;
import moe.orangemc.osu.al1s.api.auth.Credential;
import moe.orangemc.osu.al1s.api.auth.IrcCredential;
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.auth.Token;
import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.chat.ChatManager;
import moe.orangemc.osu.al1s.api.concurrent.Scheduler;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.mutltiplayer.RoomManager;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.auth.AuthenticationAPI;
import moe.orangemc.osu.al1s.auth.AuthenticationAPIModule;
import moe.orangemc.osu.al1s.auth.credential.CredentialBase;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.beatmap.BeatmapRequestAPIModule;
import moe.orangemc.osu.al1s.chat.ChatManagerImpl;
import moe.orangemc.osu.al1s.concurrent.SchedulerImpl;
import moe.orangemc.osu.al1s.event.EventBusImpl;
import moe.orangemc.osu.al1s.inject.api.*;
import moe.orangemc.osu.al1s.multiplayer.RoomManagerImpl;
import moe.orangemc.osu.al1s.user.UserImpl;
import moe.orangemc.osu.al1s.user.UserRequestAPIModule;
import moe.orangemc.osu.al1s.util.LazyReference;
import org.apache.commons.lang3.Validate;

import java.net.URL;
import java.util.concurrent.Future;

public class OsuBotImpl implements OsuBot {
    public final boolean debug;
    private final URL baseURL;

    private final AuthenticationAPI authenticationAPI;

    private final SchedulerImpl scheduler = new SchedulerImpl();
    private final EventBus eventBus;
    private final RoomManagerImpl roomManager;

    private TokenImpl token = null;
    private final LazyReference<User> botUser = new LazyReference<>(() -> {
        if (!token.getAllowedScopes().contains(Scope.IDENTIFY)) {
            throw new UnsupportedOperationException("No permission to identify self");
        }
        this.execute(UserImpl::me);
        return UserImpl.me();
    });

    @Inject
    private Injector injector;
    private final InjectionContext ctx;

    private final ChatManagerImpl chatManager;

    public OsuBotImpl(boolean debug, URL baseURL, String serverBotName, String ircServer, int ircPort) {
        this.debug = debug;
        this.baseURL = baseURL;

        ctx = injector.derivativeContext();
        ctx.registerModule(this);

        try (var _ = injector.setContext(ctx)) {
            ctx.registerModule(new BeatmapRequestAPIModule());
            ctx.registerModule(new UserRequestAPIModule());
            ctx.registerModule(new AuthenticationAPIModule());
            ctx.registerModule(new AccessorModule());
            this.authenticationAPI = (AuthenticationAPI) ctx.mapField(AuthenticationAPI.class, "default");
            this.eventBus = new EventBusImpl();
            ctx.registerModule(this, true);
            this.chatManager = new ChatManagerImpl(serverBotName);
            this.chatManager.setIrcServer(ircServer, ircPort);
            ctx.registerModule(this, true);
            this.roomManager = new RoomManagerImpl();
            ctx.registerModule(this, true);
        }
    }

    @Provides
    public OsuBotImpl getBotImpl() {
        return this;
    }

    @Provides
    public OsuBot getBot() {
        return this;
    }

    @Provides
    public RoomManager getRoomManager() {
        return this.roomManager;
    }

    @Override
    public Future<Void> authenticate(Credential credential) {
        return scheduler.runTask(() -> authenticateSync(credential));
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
    public Future<Void> authenticate(IrcCredential credential) {
        return scheduler.runTask(() -> authenticateSync(credential));
    }

    @Override
    public void authenticateSync(IrcCredential credential) {
        try (var _ = injector.setContext(ctx)) {
            chatManager.authenticateIrc((IrcCredentialImpl) credential);
        }
    }

    @Provides
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Provides
    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Provides
    @Override
    public ChatManager getChatManager() {
        return this.chatManager;
    }

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public void useToken(Token token) {
        this.token = (TokenImpl) token;
        if (System.currentTimeMillis() / 1000 > token.getTimeToLive()) {
            try {
                token.refresh();
            } catch (UnsupportedOperationException e) {
                this.token = null;
                throw e;
            }
        }
    }

    @Override
    public int getId() {
        return botUser.get().getId();
    }

    @Override
    public <T> T getMetadata(String key) {
        return botUser.get().getMetadata(key);
    }

    public URL getBaseUrl() {
        return this.baseURL;
    }

    @Override
    public String getUsername() {
        return botUser.get().getUsername();
    }

    @Override
    public void execute(Runnable runnable) {
        try (var _ = injector.setContext(ctx)) {
            ctx.registerModule(this, true);
            runnable.run();
        }
    }

    public void checkPermission(Scope scope) {
        if (token == null || !token.getAllowedScopes().contains(scope)) {
            throw new UnsupportedOperationException("No permission to " + scope);
        }
    }
}
