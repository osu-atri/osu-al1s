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

package moe.orangemc.osu.al1s.api.user.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * All available punishment types one player would receive.
 */
public enum PunishmentType {
    WARN("note"),
    MUTE("silence"),
    BAN("restriction");

    private static final Map<String, PunishmentType> byName = new HashMap<>();

    public final String name;
    PunishmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PunishmentType fromName(String name) {
        return byName.get(name);
    }

    static {
        for (PunishmentType value : values()) {
            byName.put(value.name, value);
        }
    }
}
