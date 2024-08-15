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

package moe.orangemc.osu.al1s.api.beatmap;

public enum RankStatus {
    RANKED(1),
    QUALIFIED(3),
    LOVED(4),
    APPROVED(2),
    WIP(-1),
    PENDING(0),
    GRAVEYARD(-2),
    // Placeholder for other statuses (etc. deleted or failure)
    UNKNOWN(255);

    private final int index;

    RankStatus() { this.index = 255; }
    RankStatus(int index) { this.index = index; }

    public String getName() { return name().toLowerCase(); }
}
