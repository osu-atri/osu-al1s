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

package moe.orangemc.osu.al1s.console.api.plugin;

import java.io.File;
import java.util.Objects;

public abstract class PluginLoader {
    public abstract String getAcceptableSuffix();
    public abstract <T extends Plugin> T loadPlugin(File target);

    @Override
    public final int hashCode() {
        return getAcceptableSuffix().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PluginLoader that)) {
            return false;
        }
        return Objects.equals(getAcceptableSuffix(), that.getAcceptableSuffix());
    }
}
