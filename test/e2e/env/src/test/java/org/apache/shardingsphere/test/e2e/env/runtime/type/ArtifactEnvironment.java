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

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
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
    
    private final Map<DatabaseType, String> databaseImages;
    
    private final Map<String, String> proxyPortBindingMap;
    
    public ArtifactEnvironment(final Properties props) {
        modes = Splitter.on(",").trimResults().splitToList(props.getProperty("e2e.artifact.modes", "")).stream()
                .filter(each -> !each.isEmpty()).map(each -> Mode.valueOf(each.toUpperCase())).collect(Collectors.toList());
        adapters = getAdapters(props);
        regCenterType = props.getProperty("e2e.artifact.regcenter");
        databaseTypes = getDatabaseTypes(props);
        databaseImages = getDatabaseImages(props);
        proxyPortBindingMap = getProxyPortBindingMap(props);
    }
    
    private Collection<String> getAdapters(final Properties props) {
        return Splitter.on(",").trimResults().splitToList(props.getProperty("e2e.artifact.adapters", "")).stream().filter(each -> !each.isEmpty()).collect(Collectors.toList());
    }
    
    private Collection<DatabaseType> getDatabaseTypes(final Properties props) {
        return Splitter.on(",").trimResults().splitToList(props.getProperty("e2e.artifact.databases", "")).stream()
                .filter(each -> !each.isEmpty()).map(each -> TypedSPILoader.getService(DatabaseType.class, each.trim())).collect(Collectors.toSet());
    }
    
    private Map<DatabaseType, String> getDatabaseImages(final Properties props) {
        Collection<DatabaseType> databaseTypes = ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
        Map<DatabaseType, String> result = new HashMap<>(databaseTypes.size(), 1F);
        for (DatabaseType each : databaseTypes) {
            Optional.ofNullable(props.getProperty(String.format("e2e.artifact.database.%s.image", each.getType().toLowerCase()))).ifPresent(value -> result.put(each, value));
        }
        return result;
    }
    
    private Map<String, String> getProxyPortBindingMap(final Properties props) {
        List<String> portBindingPair = Splitter.on(":").trimResults().splitToList(props.getProperty("e2e.artifact.proxy.port.bindings", ""));
        return 2 == portBindingPair.size() ? Collections.singletonMap(portBindingPair.get(0), portBindingPair.get(1)) : Collections.emptyMap();
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
