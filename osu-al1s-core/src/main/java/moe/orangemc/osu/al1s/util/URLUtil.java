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

package moe.orangemc.osu.al1s.util;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class URLUtil {
    public static URL newURL(String url) {
        return SneakyExceptionHelper.call(() -> new URI(url).toURL());
    }

    public static URL concat(URL base, String path) {
        return SneakyExceptionHelper.call(() -> new URI(base.toURI() + path).toURL());
    }

    public static String encode(String target) {
        if (target == null) {
            return null;
        }

        return URLEncoder.encode(target, Charset.defaultCharset()).replaceAll("\\+", "%20");
    }

    public static String decode(String target) {
        if (target == null) {
            return null;
        }

        return URLDecoder.decode(target, Charset.defaultCharset());
    }

    public static Map<String, String> extractQueryParams(URI url) {
        Map<String, String> params = new HashMap<>();

        String query = url.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length != 2) {
                    continue;
                }

                params.put(pair[0], pair[1]);
            }
        }

        return params;
    }
}
