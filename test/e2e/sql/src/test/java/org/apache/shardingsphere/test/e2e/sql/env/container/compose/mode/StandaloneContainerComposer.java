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
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.E2EContainers;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.NativeStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.sql.env.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.e2e.sql.env.container.config.SQLE2EProxyContainerConfigurationFactory;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Standalone composed container.
 */
public final class StandaloneContainerComposer implements ContainerComposer {
    
    private final E2EContainers containers;
    
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public StandaloneContainerComposer(final String scenario, final DatabaseType databaseType, final Adapter adapter) {
        containers = new E2EContainers(scenario);
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        storageContainer = containers.registerContainer(Type.DOCKER == type
                ? new DockerStorageContainer(E2ETestEnvironment.getInstance().getDockerEnvironment().getDatabaseImage(databaseType),
                        DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType), scenario)
                : new NativeStorageContainer(databaseType, scenario));
        AdaptorContainerConfiguration containerConfig = SQLE2EProxyContainerConfigurationFactory.newInstance(scenario, "standalone", databaseType);
        AdapterContainer adapterContainer = AdapterContainerFactory.newInstance(adapter, databaseType, scenario, containerConfig, storageContainer, type);
        if (adapterContainer instanceof DockerE2EContainer) {
            ((DockerE2EContainer) adapterContainer).dependsOn(storageContainer);
        }
        this.adapterContainer = containers.registerContainer(adapterContainer);
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
