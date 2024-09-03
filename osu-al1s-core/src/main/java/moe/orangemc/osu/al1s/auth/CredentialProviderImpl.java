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

package moe.orangemc.osu.al1s.auth;

import moe.orangemc.osu.al1s.api.auth.*;
import moe.orangemc.osu.al1s.auth.credential.AuthorizationCodeGrantCredentialImpl;
import moe.orangemc.osu.al1s.auth.credential.CredentialBase;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;

public class CredentialProviderImpl implements CredentialProvider {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Credential> T newCredential(AuthenticateType authenticateType) {
        return (T)switch (authenticateType) {
            case CLIENT_CREDENTIALS -> new CredentialBase();
            case AUTHORIZATION_CODE -> new AuthorizationCodeGrantCredentialImpl();
            case REFRESH_TOKEN -> throw new UnsupportedOperationException("Please use Token#refresh() to refresh a token");
        };
    }

    @Override
    public IrcCredential newIrcCredential() {
        return new IrcCredentialImpl();
    }

    @Override
    public Token loadToken(byte[] serialized) {
        return TokenImpl.deserialize(serialized);
    }

    @Override
    public Token loadToken(byte[] serialized, byte[] key) {
        return TokenImpl.deserialize(serialized, key);
    }
}
