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

import moe.orangemc.osu.al1s.api.user.User;

public interface BeatmapSet {
    // Basic attributes.
    int getSetID();
    String getTitle();
    String getTitleUnicode();
    String getArtist();
    String getArtistUnicode();
    String getSource();
    User getMapper();
    // Can use User Interface for this.
    String getMapperName();
    int getMapperID();
    RankStatus getRankStatus();
    int getPlayCount();
    int getFavouriteCount();
    boolean getNSFW();
    boolean getHasVideo();
    int getOffset();

    // Optional attributes.
    // We need to check their types or comment they out.
    String getLanguage();
    String getGenre();
    String[] getTags();
    float getRatings();
}
