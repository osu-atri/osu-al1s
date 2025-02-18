/*
 * Copyright 2025 Astro angelfish
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

package moe.orangemc.osu.al1s.console.storage;

import moe.orangemc.osu.al1s.api.auth.CredentialProvider;
import moe.orangemc.osu.al1s.api.auth.IrcCredential;
import moe.orangemc.osu.al1s.api.auth.Token;
import moe.orangemc.osu.al1s.console.util.SetUtil;
import moe.orangemc.osu.al1s.inject.api.Inject;
import moe.orangemc.osu.al1s.util.DigestUtil;
import moe.orangemc.osu.al1s.util.SneakyExceptionHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TokenStorage {
    @Inject
    private CredentialProvider credentialProvider;

    private final File storageFolder = new File("token");

    private final List<Token> availableTokens = new ArrayList<>();
    private final List<IrcCredential> availableIrcCredentials = new ArrayList<>();

    public TokenStorage() {
        ensureFolder();
        load();
    }

    private void ensureFolder() {
        if (!storageFolder.exists() || !storageFolder.isDirectory()) {
            storageFolder.delete();
            storageFolder.mkdirs();
        }

        Path folderPath = storageFolder.toPath();
        SneakyExceptionHelper.voidCall(() -> validatePathPermission(folderPath));

        File[] files = storageFolder.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            Path filePath = file.toPath();
            SneakyExceptionHelper.voidCall(() -> validatePathPermission(filePath));
        }
    }

    private void validatePathPermission(Path target) throws IOException {
        Set<PosixFilePermission> current = Files.getPosixFilePermissions(target);

        Files.setOwner(target, target.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty("user.name")));

        if (!current.containsAll(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
                || SetUtil.containsAny(current, Set.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE))) {
            Files.setPosixFilePermissions(target, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }
    }

    public List<Token> getAvailableTokens() {
        return availableTokens;
    }

    public List<IrcCredential> getAvailableIrcCredentials() {
        return availableIrcCredentials;
    }

    public void save() {
        ensureFolder();

        File[] files = storageFolder.listFiles();
        if (files == null) {
            throw new IllegalStateException("Failed to list files in the storage folder");
        }
        for (File file : files) {
            file.delete();
        }

        writeTokens();
        writeIrcCredentials();
    }

    private void writeIrcCredentials() {
        for (IrcCredential irc : availableIrcCredentials) {
            byte[] data = irc.serialize();
            File file = new File(storageFolder, DigestUtil.sha256sumBytes(data) + ".dat");
            SneakyExceptionHelper.voidCallAutoClose(() -> new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file))), dos -> {
                dos.writeByte(1);
                dos.write(data);
            });
        }
    }

    private void writeTokens() {
        for (Token tk : availableTokens) {
            byte[] data = tk.serialize();
            File file = new File(storageFolder, DigestUtil.sha256sumBytes(data) + ".dat");
            SneakyExceptionHelper.voidCallAutoClose(() -> new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file))), dos -> {
                dos.writeByte(0);
                dos.write(data);
            });
        }
    }

    private void load() {
        ensureFolder();

        File[] files = storageFolder.listFiles();

        if (files == null) {
            throw new IllegalStateException("Failed to list files in the storage folder");
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            SneakyExceptionHelper.voidCallAutoClose(() -> new DataInputStream(new GZIPInputStream(new FileInputStream(file))), dis -> {
                byte type = dis.readByte();
                byte[] data = new byte[dis.available()];
                dis.readFully(data);

                if (type == 0) {
                    availableTokens.add(credentialProvider.loadToken(data));
                } else if (type == 1) {
                    availableIrcCredentials.add(credentialProvider.loadIrcCredential(data));
                }
            });
        }
    }
}
