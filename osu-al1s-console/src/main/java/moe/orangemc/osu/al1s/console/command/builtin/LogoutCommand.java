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

package moe.orangemc.osu.al1s.console.command.builtin;

import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.chat.command.Command;
import moe.orangemc.osu.al1s.api.chat.command.CommandBase;
import moe.orangemc.osu.al1s.auth.credential.IrcCredentialImpl;
import moe.orangemc.osu.al1s.console.ArisBotImpl;
import moe.orangemc.osu.al1s.console.util.StringUtil;
import moe.orangemc.osu.al1s.inject.api.Inject;

public class LogoutCommand implements CommandBase {
    @Inject
    private ArisBotImpl arisBot;

    @Override
    public String getName() {
        return "logout";
    }

    @Override
    public String getDescription() {
        return "Log out of an account, and wipe all related credentials";
    }

    @Override
    public String getUsage() {
        return "<username>";
    }

    @Command
    public void logout(String username) {
        OsuBot bot = arisBot.findBot(username);

        arisBot.removeBot(bot);
        arisBot.getTokenStorage().getAvailableIrcCredentials().removeIf((c) -> StringUtil.osuStyleLike(((IrcCredentialImpl)c).getUsername(), username));
        arisBot.getTokenStorage().getAvailableTokens().remove(bot.getToken());
    }
}
