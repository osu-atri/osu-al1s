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

package moe.orangemc.osu.al1s.multiplayer;

import moe.orangemc.osu.al1s.api.bot.OsuBot;
import moe.orangemc.osu.al1s.api.mutltiplayer.MultiplayerRoom;
import moe.orangemc.osu.al1s.api.mutltiplayer.MultiplayerTeam;
import moe.orangemc.osu.al1s.api.mutltiplayer.TeamMode;
import moe.orangemc.osu.al1s.api.mutltiplayer.WinCondition;
import moe.orangemc.osu.al1s.api.ruleset.Mod;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.chat.ChatDriver;
import moe.orangemc.osu.al1s.chat.OsuChannelImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RoomImpl extends OsuChannelImpl implements MultiplayerRoom {
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Inject
    private ChatDriver chatDriver;
    @Inject
    private OsuBotImpl manager;

    private final int id;
    private final String name;
    private final String password;

    public RoomImpl(String roomName) {
        String response = "";
        Pattern pattern = Pattern.compile("(\\d+)");

        this.id = Integer.parseInt(pattern.matcher(response).group(1));
        this.name = roomName;

        SecureRandom random = new SecureRandom();
        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            passwordBuilder.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        this.password = passwordBuilder.toString();
        setPassword(password);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.sendMessage("!mp name " + name);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String password) {
        this.sendMessage("!mp password " + password);
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> roomInfo = new java.util.HashMap<>(Collections.emptyMap());
        this.sendMessage("!mp settings");
        try { wait(1000); } catch (Exception ignored) {}
        long requestTime = this.getMessageTimes("!mp settings", true, false).getFirst();
        List<String> infoStr = this.getServerMessagesTillNow(requestTime, true);
        for (String i : infoStr)
        {
            String[] infoPair = i.split(":");
            // Add more arguments here
            switch (infoPair[0]) {
                case "Active mods":
                    roomInfo.computeIfAbsent("Mods", _ -> infoPair[1].trim());
                    break;
                // Team mode: HeadToHead, Win condition: Score
                case "Team mode":
                    roomInfo.computeIfAbsent("TeamMode", _ -> infoPair[1].split(",")[0].trim());
                    roomInfo.computeIfAbsent("WinCondition", _ -> infoPair[2].trim());
                    break;
            }
        }
        return roomInfo;
    }

    @Override
    public Map<Integer, User> getPlayers() {
        return Map.of();
    }

    @Override
    public void kickPlayer(User user) {
        this.sendMessage("!mp kick " + user.getUsername());
    }

    @Override
    public void banPlayer(User user) {
        this.sendMessage("!mp ban " + user.getUsername());
    }

    @Override
    public void invitePlayer(User user) {
        this.sendMessage("!mp invite " + user.getUsername());
    }

    @Override
    public int getPlayerSlot(User user) {
        return 0;
    }

    @Override
    public void movePlayer(User user, int slot) {
        this.sendMessage("!mp move " + user.getUsername() + " " + slot);
    }

    @Override
    public @Nullable MultiplayerTeam getTeam(User user) {
        return null;
    }

    @Override
    public void setTeam(User user, MultiplayerTeam team) {
        this.sendMessage("!mp team " + user.getUsername() + " " + team.getId());
    }

    @Nullable
    @Override
    public User getHost() {
        return null;
    }

    @Override
    public void setHost(@Nullable User user) {
        if (user == null) {
            this.sendMessage("!mp clearhost");
        } else {
            this.sendMessage("!mp host " + user.getUsername());
        }
    }

    @Override
    public int getOpenSlotCount() {
        return 0;
    }

    @Override
    public Set<Mod> getRoomMods() {
        return Set.of();
    }

    @Override
    public void setRoomMods(Set<Mod> mods) {
        this.sendMessage("!mp mods " + mods.stream().map(Mod::getShortName).reduce((a, b) -> a + b).orElse(""));
    }

    @Override
    public void setRoomMods(Mod... mods) {
        this.setRoomMods(Set.of(mods));
    }

    @Override
    public Set<Mod> getUserMods(User user) {
        return Set.of();
    }

    @Override
    public Set<User> getReferees() {
        Set<User> users = new java.util.HashSet<>(Collections.emptySet());
        this.sendMessage("!mp listrefs");
        try { wait(1000); } catch (Exception ignored) {}
        // Wait when?
        List<Long> times = this.getMessageTimes("BanchoBot: Match referees:", true, false);
        List<String> msgToNow = this.getServerMessagesTillNow(times.getFirst(), true);
        msgToNow.removeLast();
        for (String i : msgToNow) {
            String trimmedStr = i.split(":")[1].trim();
            users.add(new UserImpl(trimmedStr));
        }
        return users;
    }

    @Override
    public void addReferee(User user) {
        this.sendMessage("!mp addref " + user.getUsername());
    }

    @Override
    public void removeReferee(User user) {
        this.sendMessage("!mp removeref " + user.getUsername());
    }

    @Override
    public TeamMode getTeamMode() {
        return null;
    }

    @Override
    public WinCondition getWinCondition() {
        return null;
    }

    @Override
    public void setProperties(TeamMode teamMode, WinCondition winCondition) {
        this.sendMessage("!mp set " + teamMode.getId() + " " + winCondition.getId());
    }

    @Override
    public void start(int delay) {
        this.sendMessage("!mp start " + delay);
    }

    @Override
    public void abort() {
        this.sendMessage("!mp abort");
    }

    @Override
    public void close() {
        this.sendMessage("!mp close");
    }

    @Override
    public @NotNull OsuBot getManagingBot() {
        return manager;
    }
}
