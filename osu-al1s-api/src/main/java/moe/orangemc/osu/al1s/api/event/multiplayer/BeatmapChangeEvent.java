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

package moe.orangemc.osu.al1s.api.event.multiplayer;

import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.event.CancellableEvent;
import moe.orangemc.osu.al1s.api.mutltiplayer.MultiplayerRoom;

public class BeatmapChangeEvent extends MultiplayerRoomEvent implements CancellableEvent {
    private final Beatmap beatmap;
    private boolean cancelled;

    public BeatmapChangeEvent(MultiplayerRoom room, Beatmap beatmap) {
        super(room);
        this.beatmap = beatmap;
    }

    public Beatmap getBeatmap() {
        return beatmap;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
