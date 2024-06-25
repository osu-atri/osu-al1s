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

package moe.orangemc.osu.al1s.api.event.auth;

import java.net.InetSocketAddress;
import java.util.UUID;

public class UserAcceptAuthenticationEvent extends UserActionEvent {
    private final String code;
    private final UUID csrfToken;

    private String responseHtml = "<html><body><img src=\"https://storage.googleapis.com/sticker-prod/J4AagQnWMPrpcgB9S4Iu/11.thumb128.webp\"><br>Access granted for AL-1S! <br>You can safely close this tab now!</body></html>";

    public UserAcceptAuthenticationEvent(InetSocketAddress userAddr, String code, UUID csrfToken) {
        super(userAddr);
        this.code = code;
        this.csrfToken = csrfToken;
    }

    public String getCode() {
        return code;
    }

    public UUID getCsrfToken() {
        return csrfToken;
    }

    public String getResponseHtml() {
        return responseHtml;
    }

    public void setResponseHtml(String responseHtml) {
        this.responseHtml = responseHtml;
    }
}
