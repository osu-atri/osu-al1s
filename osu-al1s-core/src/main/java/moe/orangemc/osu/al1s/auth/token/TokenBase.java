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

package moe.orangemc.osu.al1s.auth.token;

import moe.orangemc.osu.al1s.api.auth.AuthenticateType;
import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.auth.Token;
import moe.orangemc.osu.al1s.auth.credential.CredentialBase;

import java.util.ArrayList;
import java.util.List;

public abstract class TokenBase implements Token {
    private final CredentialBase referer;
    private final ServerTokenResponse serverAuthData;

    public TokenBase(CredentialBase referer, ServerTokenResponse serverAuthData) {
        this.referer = referer;
        this.serverAuthData = serverAuthData;
    }

    @Override
    public AuthenticateType getAuthenticateType() {
        return referer.getGrantType();
    }

    @Override
    public List<Scope> getAllowedScopes() {
        return new ArrayList<>(referer.getScopes());
    }

    @Override
    public long getExpire() {
        return serverAuthData.expires();
    }

    @Override
    public void refresh() {
        if (serverAuthData.refreshToken() == null) {
            throw new UnsupportedOperationException("Only tokens created with " + AuthenticateType.AUTHORIZATION_CODE + " or " + AuthenticateType.REFRESH_TOKEN + " can be refreshed.");
        }
    }

    public CredentialBase getReferer() {
        return referer;
    }

    public ServerTokenResponse getServerAuthData() {
        return serverAuthData;
    }
}
