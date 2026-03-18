/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.env.runtime.type;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Docker environment.
 */
public final class DockerEnvironment {
    
    private final Map<DatabaseType, Collection<String>> databaseImages;
    
    @Getter
    private final String proxyImage;
    
    @Getter
    private final List<String> proxyPortBindings;
    
    public DockerEnvironment(final Properties props) {
        databaseImages = loadDatabaseImages(props);
        proxyImage = props.getProperty("e2e.docker.proxy.image", ProxyContainerConstants.PROXY_CONTAINER_IMAGE);
        proxyPortBindings = EnvironmentPropertiesLoader.getListValue(props, "e2e.docker.proxy.port.bindings");
    }
    
    private Map<DatabaseType, Collection<String>> loadDatabaseImages(final Properties props) {
        Collection<DatabaseType> databaseTypes = ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
        Map<DatabaseType, Collection<String>> result = new HashMap<>(databaseTypes.size(), 1F);
        for (DatabaseType each : databaseTypes) {
            Collection<String> images = EnvironmentPropertiesLoader.getListValue(props, String.format("e2e.docker.database.%s.images", each.getType().toLowerCase()));
            if (!images.isEmpty()) {
                result.put(each, images);
            }
        }
        return result;
    }
    
    /**
     * Get database images.
     *
     * @param databaseType database type
     * @return database images
     */
    public Collection<String> getDatabaseImages(final DatabaseType databaseType) {
        return databaseImages.getOrDefault(databaseType, Collections.emptyList());
    }
    
    /**
     * Get database image.
     *
     * @param databaseType database type
     * @return database image
     */
    public String getDatabaseImage(final DatabaseType databaseType) {
        Collection<String> images = databaseImages.getOrDefault(databaseType, Collections.emptyList());
        return images.isEmpty() ? null : images.iterator().next();
    }
}
