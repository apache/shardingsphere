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

package org.apache.shardingsphere.test.e2e.sql.env.container.compose.mode;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.ITContainers;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.adapter.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.NativeStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.cluster.ClusterEnvironment.Type;
import org.apache.shardingsphere.test.e2e.sql.env.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.sql.env.container.config.SQLE2EProxyContainerConfigurationFactory;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Cluster composed container.
 */
public final class ClusterContainerComposer implements ContainerComposer {
    
    private final ITContainers containers;
    
    private final GovernanceContainer governanceContainer;
    
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public ClusterContainerComposer(final String scenario, final DatabaseType databaseType, final AdapterMode adapterMode, final AdapterType adapterType) {
        containers = new ITContainers(scenario);
        // TODO support other types of governance
        governanceContainer = containers.registerContainer(GovernanceContainerFactory.newInstance("ZooKeeper"));
        Type envType = E2ETestEnvironment.getInstance().getClusterEnvironment().getType();
        storageContainer = containers.registerContainer(Type.DOCKER == envType
                ? new DockerStorageContainer(E2ETestEnvironment.getInstance().getClusterEnvironment().getDatabaseImages().get(databaseType),
                        DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType), scenario)
                : new NativeStorageContainer(databaseType, scenario));
        AdaptorContainerConfiguration containerConfig = SQLE2EProxyContainerConfigurationFactory.newInstance(scenario, "cluster", databaseType);
        AdapterContainer adapterContainer = AdapterContainerFactory.newInstance(adapterMode, adapterType, databaseType, scenario, containerConfig, storageContainer, envType.name());
        if (adapterContainer instanceof DockerITContainer) {
            ((DockerITContainer) adapterContainer).dependsOn(governanceContainer, storageContainer);
        }
        if (adapterContainer instanceof ShardingSphereProxyEmbeddedContainer) {
            ((ShardingSphereProxyEmbeddedContainer) adapterContainer).dependsOn(governanceContainer, storageContainer);
        }
        this.adapterContainer = containers.registerContainer(adapterContainer);
    }
    
    @Override
    public void start() {
        containers.start();
    }
    
    @Override
    public DataSource getTargetDataSource() {
        return adapterContainer.getTargetDataSource(governanceContainer.getServerLists());
    }
    
    @Override
    public Map<String, DataSource> getActualDataSourceMap() {
        return storageContainer.getActualDataSourceMap();
    }
    
    @Override
    public Map<String, DataSource> getExpectedDataSourceMap() {
        return storageContainer.getExpectedDataSourceMap();
    }
    
    @Override
    public void stop() {
        containers.stop();
    }
}
