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

import moe.orangemc.osu.al1s.auth.token.TokenImpl;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class HttpUtil {
    @Inject
    private static OsuBotImpl referer;

    public static String post(URL targetURL, String urlParameters) {
        return post(targetURL, urlParameters, Collections.emptyMap());
    }

    public static String post(URL targetURL, String urlParameters, Map<String, String> headers) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) targetURL.openConnection();
            connection.setRequestMethod("POST");
            return performRequest(connection, urlParameters, headers);
        } catch (Exception e) {
            return SneakyExceptionHelper.raise(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String get(URL targetURL) {
        return get(targetURL, Collections.emptyMap());
    }

    public static String get(URL targetURL, Map<String, String> headers) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) targetURL.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Accept", "application/json");
            if (referer != null && referer.getToken() != null) {
                connection.setRequestProperty("Authorization", ((TokenImpl) referer.getToken()).toHttpToken());
            }

            headers.forEach(connection::setRequestProperty);

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Get Response
            return readResponse(connection);
        } catch (Exception e) {
            return SneakyExceptionHelper.raise(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String put(URL targetURL, String urlParameters) {
        return put(targetURL, urlParameters, Collections.emptyMap());
    }

    public static String put(URL targetURL, String urlParameters, Map<String, String> headers) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) targetURL.openConnection();
            connection.setRequestMethod("PUT");
            return performRequest(connection, urlParameters, headers);
        } catch (Exception e) {
            return SneakyExceptionHelper.raise(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String delete(URL targetURL) {
        return delete(targetURL, "");
    }

    public static String delete(URL targetURL, String urlParameters) {
        return delete(targetURL, urlParameters, Collections.emptyMap());
    }

    public static String delete(URL targetURL, String urlParameters, Map<String, String> headers) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) targetURL.openConnection();
            connection.setRequestMethod("DELETE");
            return performRequest(connection, urlParameters, headers);
        } catch (Exception e) {
            return SneakyExceptionHelper.raise(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String performRequest(HttpURLConnection connection, String urlParameters, Map<String, String> headers) throws Exception {
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        connection.setRequestProperty("Content-Length",
                Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        if (referer != null && referer.getToken() != null) {
            connection.setRequestProperty("Authorization", ((TokenImpl) referer.getToken()).toHttpToken());
        }

        headers.forEach(connection::setRequestProperty);

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.close();

        //Get Response
        return readResponse(connection);
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        try {
            connection.setDoInput(true);
        } catch (IllegalStateException _) {

        }

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\n');
        }
        rd.close();
        return response.toString();
    }
}
