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

import moe.orangemc.osu.al1s.api.auth.Scope;
import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.beatmap.RankStatus;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.bot.OsuBotImpl;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeatmapImpl implements Beatmap {
    private static Map<Integer, BeatmapImpl> cache = new ConcurrentHashMap<>();

    private final int setId;
    private final int id;

    @Inject
    private BeatmapRequestAPI api;
    @Inject
    private OsuBotImpl bot;

    private final Map<String, Object> metadata;

    private BeatmapImpl(int id) {
        bot.checkPermission(Scope.PUBLIC);

        metadata = api.getBeatmapMetadata(id);
        this.setId = (int)((double) getMetadata("beatmapset_id"));
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
    public int getSetId() {
        return setId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BeatmapImpl && ((BeatmapImpl) obj).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    // Extract from metadata.
    String getTitle() { return getMetadata("title"); }
    String getTitleUnicode() { return getMetadata("title_unicode"); }
    String getArtist() { return getMetadata("artist"); }
    String getArtistUnicode() { return getMetadata("artist_unicode"); }
    String getSource() { return getMetadata("source"); }
    float getStarRating() { return getMetadata("difficulty_rating"); }
    public Ruleset getMode() { return Ruleset.valueOf(getMetadata("mode").toString().toUpperCase()); }
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
    int getLength() { return getMetadata("total_length"); }
    User getMapper() { return UserImpl.get(getMetadata("user_id")); }

    // Extended attributes, should be returned.
    String getURL() { return getMetadata("url"); }

    float getBPM() { return getMetadata("bpm"); }

    float getCS() { return getMetadata("cs"); }
    float getHP() { return getMetadata("drain"); }
    float getAR() { return getMetadata("ar"); }
    float getOD() { return getMetadata("accuracy"); } // float accuracy

    long getLastUpdatedTime() { return getMetadata("last_updated"); }
    boolean getIsConvert() { return getMetadata("convert"); }

    int getPassCount() { return getMetadata("passcount"); }
    int getPlayCount() { return getMetadata("playcount"); }
    int getFavouriteCount() { return getMetadata("favourite_count"); }
    boolean isExplicit() { return getMetadata("nsfw"); }
    boolean hasVideo() { return getMetadata("video"); }
    int getOffset() { return getMetadata("offset"); }

    int getCircleCount() { return getMetadata("count_circles"); }
    int getSliderCount() { return getMetadata("count_sliders"); }
    int getSpinnerCount() { return getMetadata("count_spinners"); }

    public static BeatmapImpl get(int id) {
        return cache.computeIfAbsent(id, BeatmapImpl::new);
    }
}
