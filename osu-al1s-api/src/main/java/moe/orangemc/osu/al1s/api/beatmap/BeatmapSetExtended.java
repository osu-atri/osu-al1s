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

/*
* Just messed up.
* I don't understand why the 'tags' attribute also exists in BeatmapSet.
* Is this intentional? And the former is nullable?
* Maybe we should remove the nullable optional attributes from Basic interface.
* */

public interface BeatmapSetExtended extends BeatmapSet {
    float getBPM();
    boolean getDownloadAvailability();

    // Timestamps.
    long getSubmittedDate();
    long getRankedDate();
}
