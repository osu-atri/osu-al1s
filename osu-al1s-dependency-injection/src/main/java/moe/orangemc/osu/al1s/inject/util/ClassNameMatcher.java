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

package moe.orangemc.osu.al1s.inject.util;

public class ClassNameMatcher {
    public static boolean matches(String className, String wildcardPattern) {
        int nameIndex = 0;
        int patternIndex = 0;

        while (nameIndex < className.length() && patternIndex < wildcardPattern.length()) {
            if (wildcardPattern.charAt(patternIndex) == '*') {
                if (patternIndex == wildcardPattern.length() - 1) {
                    return true;
                }
                patternIndex++;
                while (nameIndex < className.length() && className.charAt(nameIndex) != wildcardPattern.charAt(patternIndex)) {
                    nameIndex++;
                }
            } else if (wildcardPattern.charAt(patternIndex) == '?') {
                nameIndex++;
                patternIndex++;
            } else if (className.charAt(nameIndex) == wildcardPattern.charAt(patternIndex)) {
                nameIndex++;
                patternIndex++;
            } else {
                return false;
            }
        }
        return nameIndex == className.length() && patternIndex == wildcardPattern.length();
    }
}
