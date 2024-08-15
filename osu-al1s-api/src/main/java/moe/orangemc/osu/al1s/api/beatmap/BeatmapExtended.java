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

import moe.orangemc.osu.al1s.api.ruleset.Ruleset;

public interface BeatmapExtended extends Beatmap {
    String getURL();

    float getCS();
    float getHP(); // float drain
    float getAR();
    float getOD(); // float accuracy

    long getLastUpdatedTime();

    Ruleset getMode(); // int mode_int
    boolean getIsConvert();

    int getPassCount();
    int getPlayCount();

    int getCircleCount();
    int getSliderCount();
    int getSpinnerCount();
}
