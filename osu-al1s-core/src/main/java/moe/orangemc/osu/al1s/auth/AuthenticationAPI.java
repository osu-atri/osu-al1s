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

import com.google.gson.Gson;
import moe.orangemc.osu.al1s.auth.credential.CredentialBase;
import moe.orangemc.osu.al1s.auth.token.ServerTokenResponse;
import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.util.HttpUtil;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.net.URL;
import java.util.Set;

public class AuthenticationAPI {
    private final Gson gson = new Gson();

    private final OsuBotImpl requester;
    private final URL targetURL;
    private final URL userRequestURL;

    public AuthenticationAPI(OsuBotImpl requester, URL rootUrl) {
        this.requester = requester;
        targetURL = URLUtil.concat(rootUrl, "oauth/token");
        userRequestURL = URLUtil.concat(rootUrl, "oauth/authorize");
    }

    public TokenImpl authorize(CredentialBase credential) {
        Set<Runnable> preHook = credential.getPreHook(this);

        for (Runnable runnable : preHook) {
            runnable.run();
        }

        ServerTokenResponse str = gson.fromJson(HttpUtil.post(targetURL, credential.toUrlEncodedForm()), ServerTokenResponse.class);
        return new TokenImpl(credential, str);
    }

    public URL getUserRequestURL() {
        return userRequestURL;
    }

    public OsuBotImpl getRequester() {
        return requester;
    }
}
