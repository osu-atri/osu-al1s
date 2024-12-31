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

package moe.orangemc.osu.al1s.ruleset;

import moe.orangemc.osu.al1s.api.beatmap.Beatmap;
import moe.orangemc.osu.al1s.api.ruleset.*;
import moe.orangemc.osu.al1s.api.user.User;

import java.util.Set;

public class PlayScoreBuilder {
    private long id;
    private PlayResult result;
    private Ruleset ruleset;
    private Beatmap map;
    private int score;
    private Set<Mod> mods;
    private double accuracy;
    private int maxCombo;
    private int isPerfectCombo;
    private int count50;
    private int count100;
    private int count300;
    private int countMiss;
    private double pp;
    private PlayGrade grade;
    private User player;

    public PlayScoreBuilder id(long id) {
        this.id = id;
        return this;
    }

    public PlayScoreBuilder result(PlayResult result) {
        this.result = result;
        return this;
    }

    public PlayScoreBuilder ruleset(Ruleset ruleset) {
        this.ruleset = ruleset;
        return this;
    }

    public PlayScoreBuilder map(Beatmap map) {
        this.map = map;
        return this;
    }

    public PlayScoreBuilder score(int score) {
        this.score = score;
        return this;
    }

    public PlayScoreBuilder mods(Set<Mod> mods) {
        this.mods = mods;
        return this;
    }

    public PlayScoreBuilder accuracy(double accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public PlayScoreBuilder maxCombo(int maxCombo) {
        this.maxCombo = maxCombo;
        return this;
    }

    public PlayScoreBuilder isPerfectCombo(int isPerfectCombo) {
        this.isPerfectCombo = isPerfectCombo;
        return this;
    }

    public PlayScoreBuilder count50(int count50) {
        this.count50 = count50;
        return this;
    }

    public PlayScoreBuilder count100(int count100) {
        this.count100 = count100;
        return this;
    }

    public PlayScoreBuilder count300(int count300) {
        this.count300 = count300;
        return this;
    }

    public PlayScoreBuilder countMiss(int countMiss) {
        this.countMiss = countMiss;
        return this;
    }

    public PlayScoreBuilder pp(double pp) {
        this.pp = pp;
        return this;
    }

    public PlayScoreBuilder grade(PlayGrade grade) {
        this.grade = grade;
        return this;
    }

    public PlayScoreBuilder player(User player) {
        this.player = player;
        return this;
    }

    public PlayScore build() {
        return new PlayScore(id, result, ruleset, map, score, mods, accuracy, maxCombo, isPerfectCombo, count50, count100, count300, countMiss, pp, grade, player);
    }
}
