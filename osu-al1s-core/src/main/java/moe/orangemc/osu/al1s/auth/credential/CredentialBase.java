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

import moe.orangemc.osu.al1s.api.auth.AuthenticateType;
import moe.orangemc.osu.al1s.api.auth.Credential;
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.auth.AuthenticationAPI;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CredentialBase implements Credential {
    private int clientId;
    private String clientSecret;
    private List<Scope> scopes;

    @Override
    public Credential setClientId(int id) {
        this.clientId = id;
        return this;
    }

    @Override
    public Credential setClientSecret(String secret) {
        this.clientSecret = secret;
        return this;
    }

    @Override
    public Credential setScopes(Scope... scopes) {
        this.scopes = List.of(scopes);
        return this;
    }

    @Override
    public Credential setScopes(List<Scope> scopes) {
        this.scopes = new ArrayList<>(scopes);
        return this;
    }

    public AuthenticateType getGrantType() {
        return AuthenticateType.CLIENT_CREDENTIALS;
    }

    public String toUrlEncodedForm() {
        return "client_id=" + clientId + "&" +
                "client_secret=" + URLUtil.encode(clientSecret) + "&" +
                "scope=" + URLUtil.encode(scopes.stream().map(Scope::name).reduce((a, b) -> a + " " + b).orElseThrow(() -> new IllegalStateException("Unknown scope"))) + "&" +
                "grant_type=" + URLUtil.encode(getGrantType().toString());
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Set<Runnable> getPreHook(AuthenticationAPI api) {
        return Collections.emptySet();
    }
}
