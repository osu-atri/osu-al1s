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

package moe.orangemc.osu.al1s.api.chat.command;

public class StringReader {
    private final String command;

    private int pos = 0;
    private final String rootCommand;

    public StringReader(String command) {
        this.command = command;
        expect('!');
        this.pos = 1;

        this.rootCommand = readString();
    }

    public String getRootCommand() {
        return rootCommand;
    }

    private void expect(char c) {
        if (this.command.charAt(this.pos) != c) {
            throw new IllegalArgumentException("Expected " + c + " at position " + this.pos);
        }
    }

    private char read() {
        return this.command.charAt(this.pos++);
    }

    private char peek() {
        return this.command.charAt(this.pos);
    }

    public String readString() {
        if (peek() == '"' || peek() == '\'') {
            return readQuotedString();
        }
        return readUnquotedString();
    }

    public String readQuotedString() {
        char quote = read();
        boolean escaping = false;

        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char c = read();
            if (c == '\\') {
                escaping = !escaping;
            }
            if (c == quote && !escaping) {
                return builder.toString();
            }

            builder.append(c);
        }
        throw new IllegalArgumentException("Unterminated quoted string");
    }

    public String readUnquotedString() {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                break;
            }
            builder.append(read());
        }
        return builder.toString();
    }

    public boolean canRead(int i) {
        return this.pos + i <= this.command.length();
    }

    public boolean canRead() {
        return this.pos < this.command.length();
    }

    public void skip() {
        while (this.pos < this.command.length() && this.command.charAt(this.pos) == ' ') {
            this.pos++;
        }
    }

    public String readRemaining() {
        return this.command.substring(this.pos);
    }
}
