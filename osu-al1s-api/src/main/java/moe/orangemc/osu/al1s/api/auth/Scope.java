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

package moe.orangemc.osu.al1s.api.auth;

public record Scope(String name) {
    public static String join(Scope... scopes) {
        StringBuilder sb = new StringBuilder();
        for (Scope scope : scopes) {
            sb.append(scope.name()).append(" ");
        }
        return sb.toString().trim().replaceAll(" ", "+");
    }

    public static final ChatScope CHAT = new ChatScope();
    public static final ForumScope FORUM = new ForumScope();
    public static final FriendsScope FRIENDS = new FriendsScope();

    public static final Scope PUBLIC = new Scope("public");
    public static final Scope IDENTIFY = new Scope("identify");
    public static final Scope DELEGATE = new Scope("delegate");

    public static class ChatScope {
        public final Scope READ = new Scope("chat.read");
        public final Scope WRITE = new Scope("chat.write");
        public final Scope WRITE_MANAGE = new Scope("chat.write_manage");
    }

    public static class ForumScope {
        public final Scope WRITE = new Scope("forum.write");
    }

    public static class FriendsScope {
        public final Scope READ = new Scope("friends.read");
    }
}
