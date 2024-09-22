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

/**
 * A class to read messages and extract valid commands from them.
 */
public class StringReader {
    private final String command;

    private int pos = 0;
    private final String rootCommand;

    private int checkpoint = 0;

    /**
     * Initializes a command line from the given string.<br />
     * The command string must start with a prompt <code>!</code>.<br />
     * The root command (a.k.a. verb) would be stored in {@link #rootCommand},<br />
     * while the position {@link #pos} points at the first character of the first argument (if exists).
     * @param command the string to extract command and arguments from
     */
    public StringReader(String command) {
        this.command = command;
        expect('!');
        this.pos = 1;

        this.rootCommand = readString();
        skip();
    }

    /**
     * Gets the root command (verb).
     * @return the verb
     */
    public String getRootCommand() {
        return rootCommand;
    }

    /**
     * Makes sure the given character exists at the internal position ({@link #pos}) of a string.
     * @param c the expected character
     * @throws IllegalArgumentException when the character at {@link #pos} isn't the given one
     */
    private void expect(char c) {
        if (this.command.charAt(this.pos) != c) {
            throw new IllegalArgumentException("Expected " + c + " at position " + this.pos);
        }
    }

    /**
     * Gets the character next to the current one (at {@link #pos}) in the string.
     * @return the next character
     */
    private char read() {
        return this.command.charAt(this.pos++);
    }

    /**
     * Gets the character at the current position {@link #pos} in the string.
     * @return the current character
     */
    private char peek() {
        return this.command.charAt(this.pos);
    }

    /**
     * Gets the root command (usually the first word of the string) from a string.
     * @return the command extracted from the given string
     * @throws IllegalArgumentException if quotes aren't closed properly
     */
    public String readString() {
        if (!canRead()) {
            return "";
        }

        if (peek() == '"' || peek() == '\'') {
            return readQuotedString();
        }
        return readUnquotedString();
    }

    /**
     * Handles strings with quotes.<br />
     * The quote character defaults to the first character of the string.<br />
     * For example, if the string is <code>"shuodedaoli"</code>,<br />
     * Then it returns <code>shuodedaoli</code>.
     * @return the quoted part of the string
     * @throws IllegalArgumentException if the given string doesn't have the quote character at the end of it
     */
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

    /**
     * Handles normal strings. Stops upon getting whitespaces to extract commands.
     * @return the characters before the first whitespace
     */
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

    /**
     * Points to the next character without reading the content.
     */
    public void skip() {
        while (this.pos < this.command.length() && this.command.charAt(this.pos) == ' ') {
            this.pos++;
        }
    }

    /**
     * Gets all the remaining characters after {@link #pos}.
     * @return a string of characters after {@link #pos}
     */
    public String readRemaining() {
        return this.command.substring(this.pos);
    }

    /**
     * Marks the current position {@link #pos} as the {@link #checkpoint} for later use.
     */
    public void mark() {
        this.checkpoint = this.pos;
    }

    /**
     * Restores the position {@link #pos} to the {@link #checkpoint}.<br />
     * If not set, the checkpoint defaults to 0.
     */
    public void reset() {
        this.pos = this.checkpoint;
    }
}
