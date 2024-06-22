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
import moe.orangemc.osu.al1s.api.auth.AuthorizationCodeGrantCredential;
import moe.orangemc.osu.al1s.util.URLUtil;

public class AuthorizationCodeGrantCredentialImpl extends CredentialBase implements AuthorizationCodeGrantCredential {
    private String redirectUri;

    @Override
    public AuthorizationCodeGrantCredential setRedirectUri(String uri) {
        this.redirectUri = uri;
        return this;
    }

    @Override
    public AuthenticateType getGrantType() {
        return AuthenticateType.AUTHORIZATION_CODE;
    }

    @Override
    public String toUrlEncodedForm() {
        return super.toUrlEncodedForm() + "&redirect_uri=" + URLUtil.encode(redirectUri);
    }
}
