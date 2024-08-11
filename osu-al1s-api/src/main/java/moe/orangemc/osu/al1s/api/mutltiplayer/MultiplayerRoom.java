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

package moe.orangemc.osu.al1s.api.mutltiplayer;

import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.chat.OsuChannel;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface MultiplayerRoom extends OsuChannel {
    int getId();

    String getName();
    void setName(String name);

    String getPassword();
    void setPassword(String password);

    Map<Integer, User> getPlayers();
    void kickPlayer(User user);
    void banPlayer(User user);
    void invitePlayer(User user);

    int getPlayerSlot(User user);
    void movePlayer(User user, int slot);

    @Nullable MultiplayerTeam getTeam(User user);
    void setTeam(User user, MultiplayerTeam team);

    @Nullable User getHost();
    void setHost(@Nullable User user);

    int getOpenSlotCount();

    Set<Mod> getRoomMods();
    void setRoomMods(Set<Mod> mods);
    void setRoomMods(Mod... mods);

    Set<Mod> getUserMods(User user);

    Set<User> getReferees();
    void addReferee(User user);
    void removeReferee(User user);

    TeamMode getTeamMode();
    WinCondition getWinCondition();
    void setProperties(TeamMode teamMode, WinCondition winCondition);

    void start(int delay);
    void abort();

    void close();

    @NotNull OsuBot getManagingBot();

    @Override
    default String getChannelName() {
        return "#mp_" + getId();
    }
}
