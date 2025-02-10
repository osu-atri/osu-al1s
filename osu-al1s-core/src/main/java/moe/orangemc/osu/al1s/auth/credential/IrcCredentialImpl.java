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

package moe.orangemc.osu.al1s.auth.credential;

import moe.orangemc.osu.al1s.api.auth.IrcCredential;
import moe.orangemc.osu.al1s.auth.util.CryptoUtil;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class IrcCredentialImpl implements IrcCredential {
    private String username;
    private String password;

    @Override
    public IrcCredential setIrcUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public IrcCredential setIrcPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public byte[] serialize() {
        return serialize(SneakyExceptionHelper.call(() -> MessageDigest.getInstance("SHA-256").digest(InetAddress.getLocalHost().getHostName().getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public byte[] serialize(byte[] key) {
        return CryptoUtil.encrypt(SneakyExceptionHelper.call(() -> {
            return String.format("%s:%s", username, password).getBytes();
        }), key, "AES");
    }

    public static IrcCredentialImpl deserialize(byte[] serialized, byte[] key) {
        String[] data = new String(CryptoUtil.decrypt(serialized, key, "AES"), StandardCharsets.UTF_8).split(":");
        return (IrcCredentialImpl) new IrcCredentialImpl().setIrcUsername(data[0]).setIrcPassword(data[1]);
    }

    public static IrcCredentialImpl deserialize(byte[] serialized) {
        return deserialize(serialized, SneakyExceptionHelper.call(() -> MessageDigest.getInstance("SHA-256").digest(InetAddress.getLocalHost().getHostName().getBytes(StandardCharsets.UTF_8))));
    }
}
