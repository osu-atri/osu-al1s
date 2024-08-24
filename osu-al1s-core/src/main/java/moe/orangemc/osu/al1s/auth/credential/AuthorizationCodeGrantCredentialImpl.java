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

package moe.orangemc.osu.al1s.auth.credential;

import com.sun.net.httpserver.HttpServer;
import moe.orangemc.osu.al1s.api.auth.AuthenticateType;
import moe.orangemc.osu.al1s.api.auth.AuthorizationCodeGrantCredential;
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.event.EventBus;
import moe.orangemc.osu.al1s.api.event.auth.UserAcceptAuthenticationEvent;
import moe.orangemc.osu.al1s.api.event.auth.UserAuthenticationRequestEvent;
import moe.orangemc.osu.al1s.api.event.auth.UserCsrfFailEvent;
import moe.orangemc.osu.al1s.auth.AuthenticationAPI;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuthorizationCodeGrantCredentialImpl extends CredentialBase implements AuthorizationCodeGrantCredential {
    private String redirectUri;
    private InetSocketAddress callbackAddr;
    private String code;

    @Override
    public AuthorizationCodeGrantCredential setRedirectUri(String uri) {
        this.redirectUri = uri;
        return this;
    }

    @Override
    public AuthorizationCodeGrantCredential setCallbackAddr(InetSocketAddress addr) {
        this.callbackAddr = addr;
        return this;
    }

    @Override
    public AuthenticateType getGrantType() {
        return AuthenticateType.AUTHORIZATION_CODE;
    }

    @Override
    public String toUrlEncodedForm() {
        return super.toUrlEncodedForm() + "&redirect_uri=" + URLUtil.encode(redirectUri) + "&code=" + URLUtil.encode(code);
    }

    @Override
    public Set<Runnable> getPreHook() {
        return Set.of(new CodeReceiver());
    }

    private class CodeReceiver implements Runnable {
        private final Lock codeLock = new ReentrantLock();

        @Inject
        private AuthenticationAPI api;

        private URL makeUserRequestURL(UUID state) {
            return URLUtil.concat(api.getUserRequestURL(), "?" +
                    "client_id=" + getClientId() + "&" +
                    "redirect_uri=" + URLUtil.encode(redirectUri) + "&" +
                    "response_type=code&" +
                    "scope=" + Scope.join(getScopes()) + "&" +
                    "state=" + state.toString());
        }

        @Override
        public void run() {
            UUID state = UUID.randomUUID();
            URL userRequestURL = makeUserRequestURL(state);

            SneakyExceptionHelper.voidCall(() -> {
                try {
                    codeLock.lock();

                    // They might have a handler for customized interface.
                    EventBus eventBus = api.getRequester().getEventBus();
                    eventBus.fire(new UserAuthenticationRequestEvent(userRequestURL));

                    // HttpServer runs on another thread so we need to wait.
                    final Condition codeCondition = codeLock.newCondition();
                    HttpServer server = HttpServer.create(callbackAddr, 1);
                    server.createContext("/", exchange -> {
                        try {
                            Map<String, String> params = URLUtil.extractQueryParams(exchange.getRequestURI());
                            if (!params.get("state").equals(state.toString())) {
                                UserCsrfFailEvent evt = new UserCsrfFailEvent(exchange.getRemoteAddress(), state, params.get("state"));
                                eventBus.fire(evt);

                                if (!evt.isCancelled()) {
                                    exchange.sendResponseHeaders(400, 0);
                                    exchange.close();
                                    return;
                                }
                            }
                            code = params.get("code");

                            UserAcceptAuthenticationEvent event = new UserAcceptAuthenticationEvent(exchange.getRemoteAddress(), code, state);
                            eventBus.fire(event);
                            if (event.isCancelled()) {
                                exchange.sendResponseHeaders(400, 0);
                                exchange.close();
                                return;
                            }

                            String response = event.getResponseHtml();
                            exchange.sendResponseHeaders(200, response.length());
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }

                            try {
                                codeLock.lock();
                                codeCondition.signalAll();
                            } finally {
                                codeLock.unlock();
                            }

                            exchange.close();
                            server.stop(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    server.start();
                    while (code == null) {
                        codeCondition.awaitUninterruptibly();
                    }
                } finally {
                    codeLock.unlock();
                }
            });
        }
    }
}
