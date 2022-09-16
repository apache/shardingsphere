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

package org.apache.shardingsphere.test.integration.ha.framework.container.compose;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.AdapterContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.EnvironmentConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.impl.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.ContainerUtil;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.StorageContainerUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.ha.framework.container.config.ProxyClusterContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.ha.framework.container.config.StorageContainerConfigurationFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Composed container, include governance container and storage container.
 */
@Slf4j
@Getter
public final class DockerContainerComposer extends BaseContainerComposer {
    
    private final DatabaseType databaseType;
    
    private final ShardingSphereProxyClusterContainer proxyContainer;
    
    @Getter
    private final List<DockerStorageContainer> storageContainers;
    
    @Getter
    private final GovernanceContainer governanceContainer;
    
    public DockerContainerComposer(final String scenario, final DatabaseType databaseType, final String storageContainerImage) {
        super("");
        this.databaseType = databaseType;
        this.storageContainers = new LinkedList<>();
        governanceContainer = getContainers().registerContainer(new ZookeeperContainer());
        List<StorageContainerConfiguration> containerConfigs = StorageContainerConfigurationFactory.newInstance(scenario, databaseType);
        containerConfigs.forEach(each -> {
            DockerStorageContainer storageContainer = getContainers().registerContainer((DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, storageContainerImage, null, each));
            storageContainer.setNetworkAliases(Collections.singletonList(databaseType.getType().toLowerCase() + "_" + ContainerUtil.generateStorageContainerId()));
            storageContainers.add(storageContainer);
        });
        
        AdaptorContainerConfiguration containerConfig = ProxyClusterContainerConfigurationFactory.newInstance(scenario);
        ShardingSphereProxyClusterContainer proxyClusterContainer = (ShardingSphereProxyClusterContainer) AdapterContainerFactory
                .newInstance(EnvironmentConstants.CLUSTER_MODE, AdapterContainerConstants.PROXY, databaseType, null, "", containerConfig);
        storageContainers.forEach(each -> proxyClusterContainer.dependsOn(governanceContainer, each));
        proxyContainer = getContainers().registerContainer(proxyClusterContainer);
    }
    
    @Override
    public DataSource getProxyDatasource(final String databaseName) {
        return StorageContainerUtil.generateDataSource(DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName),
                StorageContainerConstants.USERNAME, StorageContainerConstants.PASSWORD);
    }
    
    @Override
    public List<DataSource> getExposedDatasource(final String databaseName) {
        return getStorageContainers().stream()
                .map(each -> DataSourceEnvironment.getURL(databaseType, each.getNetworkAliases().get(0), each.getExposedPort(), databaseName))
                .map(each -> StorageContainerUtil.generateDataSource(each, StorageContainerConstants.USERNAME, StorageContainerConstants.PASSWORD))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DataSource> getMappedDatasource(final String databaseName) {
        return getStorageContainers().stream()
                .map(each -> DataSourceEnvironment.getURL(databaseType, each.getHost(), each.getMappedPort(), databaseName))
                .map(each -> StorageContainerUtil.generateDataSource(each, StorageContainerConstants.USERNAME, StorageContainerConstants.PASSWORD))
                .collect(Collectors.toList());
    }
}
