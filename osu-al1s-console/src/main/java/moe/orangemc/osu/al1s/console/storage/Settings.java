/*
 * Copyright 2025 Astro angelfish
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

package moe.orangemc.osu.al1s.console.storage;

import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class Settings {
    @Inject(name="cwd")
    private File cwd;

    private URL serverUrl;
    private String serverBotName;
    private String ircHost;
    private int ircPort;

    public Settings() {
        File settingsFile = new File(cwd, "settings.yml");

        if (settingsFile.exists()) {
            loadFromFile(settingsFile);
        } else {
            this.serverUrl = SneakyExceptionHelper.call(() -> new URI("https://osu.ppy.sh/").toURL());
            this.serverBotName = "BanchoBot";
            this.ircHost = "irc.ppy.sh";
            this.ircPort = 6667;

            writeSettings(settingsFile);
        }
    }

    private void loadFromFile(File settingsFile) {
        SneakyExceptionHelper.voidCallAutoClose(() -> new FileReader(settingsFile), (fis) -> {
            Yaml yaml = new Yaml();
            Map<String, Object> map = yaml.load(fis);

            serverUrl = new URI(map.get("server-url").toString()).toURL();
            serverBotName = map.get("server-bot-name").toString();

            Map<String, Object> irc = (Map<String, Object>) map.get("irc");
            ircHost = irc.get("host").toString();
            ircPort = Integer.parseInt(irc.get("port").toString());
        });
    }

    public URL getServerUrl() {
        return serverUrl;
    }

    public String getServerBotName() {
        return serverBotName;
    }

    private void writeSettings(File settingsFile) {
        SneakyExceptionHelper.voidCallAutoClose(() -> new FileWriter(settingsFile), (fis) -> {
            Yaml yaml = new Yaml();
            yaml.dump(Map.of(
                    "server-url", serverUrl.toString(),
                    "server-bot-name", serverBotName,
                    "irc", Map.of(
                            "host", ircHost,
                            "port", ircPort
                    )
            ), fis);
        });
    }

    public String getIrcHost() {
        return ircHost;
    }

    public int getIrcPort() {
        return ircPort;
    }
}
