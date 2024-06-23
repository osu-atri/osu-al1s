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
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.util.URLUtil;

public class RefreshingCredentialImpl extends CredentialBase {
    private final TokenImpl referer;

    public RefreshingCredentialImpl(TokenImpl referer) {
        this.referer = referer;

        this.setClientId(referer.getReferer().getClientId())
                .setScopes(referer.getReferer().getScopes())
                .setClientSecret(referer.getReferer().getClientSecret());
    }

    @Override
    public AuthenticateType getGrantType() {
        return AuthenticateType.REFRESH_TOKEN;
    }

    @Override
    public String toUrlEncodedForm() {
        return super.toUrlEncodedForm() + "&refresh_token=" + URLUtil.encode(referer.getServerAuthData().refreshToken());
    }
}
