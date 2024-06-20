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

package moe.orangemc.osu.al1s.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ArisServiceProvider<T> {
    private String defaultProviderName;
    private final Class<T> serviceType;

    public ArisServiceProvider(String defaultProviderName, Class<T> serviceType) {
        this.defaultProviderName = defaultProviderName;
        this.serviceType = serviceType;
    }

    public List<T> providers() {
        List<T> services = new ArrayList<>();
        ServiceLoader<T> loader = ServiceLoader.load(serviceType);
        for (T service : loader) {
            services.add(service);
        }

        return services;
    }

    public void replaceDefaultProvider(String name) {
        defaultProviderName = name;
    }

    public T provider(String name) {
        ServiceLoader<T> loader = ServiceLoader.load(serviceType);
        for (T service : loader) {
            if (service.getClass().getName().equals(name)) {
                return service;
            }
        }

        throw new NoSuchProviderException(serviceType);
    }

    public T defaultProvider() {
        return provider(defaultProviderName);
    }
}
