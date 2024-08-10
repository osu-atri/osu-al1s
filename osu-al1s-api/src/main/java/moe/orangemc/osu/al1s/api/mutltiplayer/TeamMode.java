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

public enum TeamMode {
    HEAD_TO_HEAD(0),
    TAG_COOP(1),
    TEAM_VS(2),
    TAG_TEAM_VS(3);

    private final int id;

    TeamMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
