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

package moe.orangemc.osu.al1s.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.HttpUtil;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.net.URL;
import java.util.Map;

public class UserRequestAPI {
    @Inject
    private OsuBotImpl referer;

    @Inject
    private Gson gson;

    private final URL targetURL;

    public UserRequestAPI() {
        targetURL = URLUtil.concat(referer.getBaseUrl(), "api/v2/users/");
    }

    public Map<String, Object> getUserMetadata(int id) {
        String response = HttpUtil.get(URLUtil.concat(targetURL, String.valueOf(id)));
        return gson.fromJson(response, new TypeToken<>() {}.getType());
    }

    public Map<String, Object> getUserMetadata(String username) {
        String response = HttpUtil.get(URLUtil.concat(targetURL, username + "?key=username"));
        return gson.fromJson(response, new TypeToken<>() {}.getType());
    }

    public Map<String, Object> getSelfMetadata() {
        String response = HttpUtil.get(URLUtil.concat(referer.getBaseUrl(), "api/v2/me"));
        return gson.fromJson(response, new TypeToken<>() {}.getType());
    }
}
