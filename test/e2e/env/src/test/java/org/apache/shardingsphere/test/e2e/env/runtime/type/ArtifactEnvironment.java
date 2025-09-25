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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Artifact environment.
 */
@Getter
public final class ArtifactEnvironment {
    
    private final Collection<Mode> modes;
    
    private final Collection<String> adapters;
    
    private final String regCenterType;
    
    private final Collection<DatabaseType> databaseTypes;
    
    @Getter(AccessLevel.NONE)
    private final Map<DatabaseType, String> databaseImages;
    
    private final List<String> proxyPortBindings;
    
    public ArtifactEnvironment(final Properties props) {
        modes = EnvironmentPropertiesLoader.getListValue(props, "e2e.artifact.modes").stream().map(each -> Mode.valueOf(each.toUpperCase())).collect(Collectors.toList());
        adapters = EnvironmentPropertiesLoader.getListValue(props, "e2e.artifact.adapters");
        regCenterType = props.getProperty("e2e.artifact.regcenter");
        databaseTypes = EnvironmentPropertiesLoader.getListValue(props, "e2e.artifact.databases").stream()
                .map(each -> TypedSPILoader.getService(DatabaseType.class, each.trim())).collect(Collectors.toSet());
        databaseImages = getDatabaseImages(props);
        proxyPortBindings = EnvironmentPropertiesLoader.getListValue(props, "e2e.artifact.proxy.port.bindings");
    }
    
    private Map<DatabaseType, String> getDatabaseImages(final Properties props) {
        Collection<DatabaseType> databaseTypes = ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
        Map<DatabaseType, String> result = new HashMap<>(databaseTypes.size(), 1F);
        for (DatabaseType each : databaseTypes) {
            Optional.ofNullable(props.getProperty(String.format("e2e.artifact.database.%s.image", each.getType().toLowerCase()))).ifPresent(value -> result.put(each, value));
        }
        return result;
    }
    
    /**
     * Get database image.
     *
     * @param databaseType database type
     * @return database image
     */
    public String getDatabaseImage(final DatabaseType databaseType) {
        return databaseImages.get(databaseType);
    }
    
    public enum Mode {
        
        STANDALONE, CLUSTER
    }
    
    @RequiredArgsConstructor
    @Getter
    public enum Adapter {
        
        JDBC("jdbc"),
        
        PROXY("proxy");
        
        private final String value;
    }
}
