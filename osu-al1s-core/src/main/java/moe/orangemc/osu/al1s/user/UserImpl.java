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

import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.chat.OsuChannelImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;

import java.util.HashMap;
import java.util.Map;

public class UserImpl extends OsuChannelImpl implements User {
    public static UserImpl ME = new UserImpl();
    private static final Map<Integer, UserImpl> byId = new HashMap<>();
    private static final Map<String, UserImpl> byUsername = new HashMap<>();

    private final int id;

    @Inject
    private UserRequestAPI api;

    private final Map<String, Object> metadata;


    private UserImpl() {
        metadata = api.getSelfMetadata();
        this.id = (int)((double) getMetadata("id"));
    }

    private UserImpl(int id) {
        metadata = api.getUserMetadata(id);
        this.id = (int)((double) getMetadata("id"));
    }

    private UserImpl(String username) {
        metadata = api.getUserMetadata(username);
        this.id = (int)((double) getMetadata("id"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return getMetadata("username");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UserImpl && ((UserImpl) obj).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static UserImpl get(int id) {
        return byId.computeIfAbsent(id, UserImpl::new);
    }

    public static UserImpl get(String username) {
        return byUsername.computeIfAbsent(username, UserImpl::new);
    }
}
