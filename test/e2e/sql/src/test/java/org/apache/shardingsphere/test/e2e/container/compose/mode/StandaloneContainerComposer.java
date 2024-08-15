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

package org.apache.shardingsphere.test.e2e.container.compose.mode;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.container.config.ProxyStandaloneContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.ITContainers;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Standalone composed container.
 */
public final class StandaloneContainerComposer implements ContainerComposer {
    
    private final ITContainers containers;
    
    private final Collection<StorageContainer> actualStorageContainer = new LinkedList<>();
    
    private final StorageContainer expectStorageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public StandaloneContainerComposer(final String scenario, final DatabaseType databaseType, final AdapterMode adapterMode, final AdapterType adapterType,
                                       final Map<DatabaseType, Collection<String>> storageDatabaseTypeMap) {
        containers = new ITContainers(scenario);
        // TODO add more version of databases
        for (Map.Entry<DatabaseType, Collection<String>> entry : storageDatabaseTypeMap.entrySet()) {
            actualStorageContainer.add(containers.registerContainer(StorageContainerFactory.newInstance(entry.getKey(), "",
                    StorageContainerConfigurationFactory.newInstance(entry.getKey(), scenario, Type.ACTUAL), entry.getValue())));
        }
        expectStorageContainer = containers.registerContainer(StorageContainerFactory.newInstance(databaseType, "",
                StorageContainerConfigurationFactory.newInstance(databaseType, scenario, Type.EXPECTED), DatabaseEnvironmentManager.getDatabases(scenario, Type.EXPECTED)),
                String.join(".", databaseType.getType(), scenario, "expected.host"));
        adapterContainer = containers.registerContainer(AdapterContainerFactory.newInstance(adapterMode, adapterType, databaseType, scenario,
                ProxyStandaloneContainerConfigurationFactory.newInstance(scenario, databaseType)));
        if (adapterContainer instanceof DockerITContainer) {
            ((DockerITContainer) adapterContainer).dependsOn(actualStorageContainer);
        }
    }
    
    @Override
    public void start() {
        containers.start();
    }
    
    @Override
    public DataSource getTargetDataSource() {
        return adapterContainer.getTargetDataSource(null);
    }
    
    @Override
    public Map<String, DataSource> getActualDataSourceMap() {
        return actualStorageContainer.stream().map(StorageContainer::getDataSourceMap).reduce((a, b) -> {
            a.putAll(b);
            return a;
        }).orElse(Collections.emptyMap());
    }
    
    @Override
    public Map<String, DataSource> getExpectedDataSourceMap() {
        return expectStorageContainer.getDataSourceMap();
    }
    
    @Override
    public void stop() {
        containers.stop();
    }
}
