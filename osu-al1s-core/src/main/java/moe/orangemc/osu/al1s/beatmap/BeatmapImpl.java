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

import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.beatmap.BeatmapSet;
import moe.orangemc.osu.al1s.api.beatmap.RankStatus;
import moe.orangemc.osu.al1s.api.ruleset.Ruleset;
import moe.orangemc.osu.al1s.api.user.User;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.user.UserImpl;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public class BeatmapImpl implements Beatmap {
    private final int setId;
    private final int id;

    @Inject
    private BeatmapRequestAPI api;

    private final Map<String, Object> metadata;

    public BeatmapImpl(int id) {
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
    BeatmapSet getMapSet() { throw new NotImplementedException("Leave me alone..."); }
    float getStarRating() { return getMetadata("difficulty_rating"); }
    Ruleset getMode() { return Ruleset.valueOf(getMetadata("mode").toString().toUpperCase()); }
    RankStatus getRankStatus() { return RankStatus.valueOf(getMetadata("status").toString().toUpperCase()); }
    int getLength() { return getMetadata("total_length"); }
    User getMapper() { return new UserImpl(getMetadata("user_id")); }

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

    int getCircleCount() { return getMetadata("count_circles"); }
    int getSliderCount() { return getMetadata("count_sliders"); }
    int getSpinnerCount() { return getMetadata("count_spinners"); }
}
