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

package moe.orangemc.osu.al1s.chat.command.argument;

import moe.orangemc.osu.al1s.api.chat.command.ArgumentTypeAdapter;
import moe.orangemc.osu.al1s.api.chat.command.StringReader;

public class BooleanTypeAdapter implements ArgumentTypeAdapter<Boolean> {
    @Override
    public Boolean parse(StringReader reader) {
        String s = reader.readString();
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(s);
        }
        throw new IllegalArgumentException("Invalid boolean value: " + s);
    }
}
