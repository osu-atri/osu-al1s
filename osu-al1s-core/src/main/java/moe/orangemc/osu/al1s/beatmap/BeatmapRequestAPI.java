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

package moe.orangemc.osu.al1s.beatmap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.HttpUtil;
import moe.orangemc.osu.al1s.util.URLUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BeatmapRequestAPI {
    @Inject
    private OsuBotImpl referer;
    // Does this support anonymous requesting? Is a referer really needed...

    @Inject
    private Gson gson;

    private final URL targetURL;
    private final URL setURL;

    private final HashMap<Integer, Map<String, Object>> mapCache;

    public BeatmapRequestAPI() {
        targetURL = URLUtil.concat(referer.getBaseUrl(), "api/v2/beatmaps/");
        setURL = URLUtil.concat(referer.getBaseUrl(), "api/v2/beatmapsets/");
        mapCache = new HashMap<>();
    }

    /**
     * Request a beatmap of the specified ID.
     * @param id the <b>Beatmap</b> ID
     * @return A map representing <a href="https://osu.ppy.sh/docs/index.html#beatmapextended">BeatmapExtended</a> object.
     */
    public Map<String, Object> getBeatmapMetadata(int id) {
        if (mapCache.containsKey(id)) {
            return mapCache.get(id);
        }
        String response = HttpUtil.get(URLUtil.concat(targetURL, String.valueOf(id)));
        Map<String, Object> result = gson.fromJson(response, new TypeToken<>() {}.getType());
        String setResponse = HttpUtil.get(URLUtil.concat(targetURL, result.get("beatmapset_id").toString()));
        Map<String, Object> setResult = gson.fromJson(setResponse, new TypeToken<>() {}.getType());
        result.putAll(setResult);

        mapCache.put(id, result);
        return result;
    }
}
