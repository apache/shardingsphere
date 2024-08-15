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

package org.apache.shardingsphere.test.e2e.container.compose;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Pair;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.url.core.ShardingSphereURL;
import org.apache.shardingsphere.infra.url.core.ShardingSphereURLLoadEngine;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.e2e.container.compose.mode.ClusterContainerComposer;
import org.apache.shardingsphere.test.e2e.container.compose.mode.StandaloneContainerComposer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioCommonPath;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Composed container registry.
 */
public final class ContainerComposerRegistry implements AutoCloseable {
    
    {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }
    
    private final Map<String, ContainerComposer> containerComposers = new HashMap<>(7, 1F);
    
    /**
     * Get container composer.
     *
     * @param key key
     * @param scenario scenario
     * @param databaseType databaseType
     * @param adapterMode adapterMode
     * @param adapterType adapterType
     * @return composed container
     */
    public ContainerComposer getContainerComposer(final String key, final String scenario, final DatabaseType databaseType, final AdapterMode adapterMode, final AdapterType adapterType) {
        if (containerComposers.containsKey(key)) {
            return containerComposers.get(key);
        }
        synchronized (containerComposers) {
            if (!containerComposers.containsKey(key)) {
                containerComposers.put(key, createContainerComposer(isClusterMode(adapterMode, adapterType), scenario, databaseType, adapterMode, adapterType));
            }
            return containerComposers.get(key);
        }
    }
    
    private boolean isClusterMode(final AdapterMode adapterMode, final AdapterType adapterType) {
        // TODO cluster mode often throw exception sometimes, issue is #15517
        return AdapterMode.CLUSTER == adapterMode && AdapterType.PROXY == adapterType || AdapterType.PROXY_RANDOM == adapterType;
    }
    
    private ContainerComposer createContainerComposer(final boolean clusterMode, final String scenario, final DatabaseType databaseType, final AdapterMode adapterMode, final AdapterType adapterType) {
        Map<DatabaseType, Collection<String>> storageDatabaseTypeMap = getStorageDatabaseTypeMap(getYamlConfig(scenario, databaseType));
        return clusterMode ? new ClusterContainerComposer(scenario, databaseType, adapterMode, adapterType, storageDatabaseTypeMap)
                : new StandaloneContainerComposer(scenario, databaseType, adapterMode, adapterType, storageDatabaseTypeMap);
    }
    
    private static Map<DatabaseType, Collection<String>> getStorageDatabaseTypeMap(final YamlJDBCConfiguration rootConfig) {
        return rootConfig.getDataSources().entrySet().stream().map(entry -> new Pair<>(entry.getKey(), DatabaseTypeFactory.get((String) entry.getValue().get("url"))))
                .collect(HashMultimap::<DatabaseType, String>create, (map, pair) -> map.put(pair.getSecond(), pair.getFirst()), Multimap::putAll).asMap();
    }
    
    @SneakyThrows(IOException.class)
    private YamlJDBCConfiguration getYamlConfig(final String scenario, final DatabaseType databaseType) {
        ShardingSphereURLLoadEngine urlLoadEngine = new ShardingSphereURLLoadEngine(ShardingSphereURL.parse("absolutepath:" + new ScenarioCommonPath(scenario).getRuleConfigurationFile(databaseType)));
        return YamlEngine.unmarshal(urlLoadEngine.loadContent(), YamlJDBCConfiguration.class);
    }
    
    @Override
    public void close() {
        for (ContainerComposer each : containerComposers.values()) {
            closeTargetDataSource(each.getTargetDataSource());
            closeActualDataSourceMap(each.getActualDataSourceMap());
            closeContainer(each);
        }
        containerComposers.clear();
    }
    
    @SneakyThrows
    private void closeTargetDataSource(final DataSource targetDataSource) {
        Preconditions.checkState(targetDataSource instanceof AutoCloseable, "target data source is not implement AutoCloseable");
        ((AutoCloseable) targetDataSource).close();
    }
    
    @SneakyThrows
    private void closeActualDataSourceMap(final Map<String, DataSource> actualDataSourceMap) {
        for (DataSource each : actualDataSourceMap.values()) {
            Preconditions.checkState(each instanceof AutoCloseable, "actual data source is not implement AutoCloseable");
            ((AutoCloseable) each).close();
        }
    }
    
    private void closeContainer(final ContainerComposer containerComposer) {
        containerComposer.close();
    }
}
