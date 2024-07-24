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

package moe.orangemc.osu.al1s.injecttest;

import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.inject.api.InjectTiming;

public class TestInjectee {
    @Inject
    private TestProvider testProvider;

    @Inject(when = InjectTiming.POST)
    private TestProvider postTestProvider;

    public TestInjectee() {
        if (postTestProvider != null) {
            throw new IllegalStateException("Post injection should not be done yet");
        }

        if (testProvider != null) {
            return;
        }

        throw new IllegalStateException("Injection should have been done");
    }

    public TestProvider getTestProvider() {
        return testProvider;
    }

    public TestProvider getPostTestProvider() {
        return postTestProvider;
    }
}
