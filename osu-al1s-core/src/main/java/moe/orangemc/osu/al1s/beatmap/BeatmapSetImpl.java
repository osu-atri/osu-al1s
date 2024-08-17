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

package moe.orangemc.osu.al1s.beatmap;

import moe.orangemc.osu.al1s.api.beatmap.BeatmapSet;
import moe.orangemc.osu.al1s.api.beatmap.RankStatus;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;

import java.util.Map;

public class BeatmapSetImpl implements BeatmapSet {
    private final int id;

    @Inject
    private BeatmapSetRequestAPI api;

    private final Map<String, Object> metadata;

    public BeatmapSetImpl(int id) {
        metadata = api.getBeatmapSetMetadata(id);
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
    public boolean equals(Object obj) {
        return obj instanceof BeatmapSetImpl && ((BeatmapSetImpl) obj).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    // Basic attributes.
    String getTitle() { return getMetadata("title"); }
    String getTitleUnicode() { return getMetadata("title_unicode"); }
    String getArtist() { return getMetadata("artist"); }
    String getArtistUnicode() { return getMetadata("artist_unicode"); }
    String getSource() { return getMetadata("source"); }
    User getMapper() { return new UserImpl(getMetadata("user_id")); }
    RankStatus getRankStatus() {
        String status = getMetadata("status").toString();
        try {
            return RankStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // When getting integers instead of strings...
            System.err.printf("Unknown rank status \"%s\".", status);
            return null;
        }
    }
    int getPlayCount() { return getMetadata("play_count"); }
    int getFavouriteCount() { return getMetadata("favourite_count"); }
    boolean getNSFW() { return getMetadata("nsfw"); }
    boolean getHasVideo() { return getMetadata("video"); }
    int getOffset() { return getMetadata("offset"); }

    // Optional and extended attributes aren't listed here yet.
}
