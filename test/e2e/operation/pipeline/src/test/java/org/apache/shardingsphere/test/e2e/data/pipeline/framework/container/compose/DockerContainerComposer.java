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

package org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.type.OracleDatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.config.proxy.PipelineProxyClusterContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DockerImageVersion;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.impl.ZookeeperContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Composed container, include governance container and database container.
 */
@Slf4j
public final class DockerContainerComposer extends BaseContainerComposer {
    
    private final DatabaseType databaseType;
    
    private final ShardingSphereProxyClusterContainer proxyContainer;
    
    @Getter
    private final List<DockerStorageContainer> storageContainers = new LinkedList<>();
    
    @Getter
    private final GovernanceContainer governanceContainer;
    
    public DockerContainerComposer(final DatabaseType databaseType, final String storageContainerImage, final int storageContainerCount) {
        this.databaseType = databaseType;
        governanceContainer = getContainers().registerContainer(new ZookeeperContainer());
        if (storageContainerCount < 1) {
            throw new InvalidParameterException("storageContainerCount must >= 1");
        }
        for (int i = 0; i < storageContainerCount; i++) {
            StorageContainerConfiguration storageContainerConfig;
            int majorVersion = databaseType instanceof MySQLDatabaseType ? new DockerImageVersion(storageContainerImage).getMajorVersion() : 0;
            storageContainerConfig = StorageContainerConfigurationFactory.newInstance(databaseType, majorVersion);
            DockerStorageContainer storageContainer = getContainers().registerContainer((DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, storageContainerImage,
                    storageContainerConfig));
            storageContainer.setNetworkAliases(Collections.singletonList(String.join(".", databaseType.getType().toLowerCase() + "_" + i, "host")));
            storageContainers.add(storageContainer);
        }
        AdaptorContainerConfiguration containerConfig = PipelineProxyClusterContainerConfigurationFactory.newInstance(databaseType, storageContainerImage);
        ShardingSphereProxyClusterContainer proxyClusterContainer = (ShardingSphereProxyClusterContainer) AdapterContainerFactory.newInstance(
                AdapterMode.CLUSTER, AdapterType.PROXY, databaseType, null, "", containerConfig);
        for (DockerStorageContainer each : storageContainers) {
            proxyClusterContainer.dependsOn(governanceContainer, each);
        }
        proxyContainer = getContainers().registerContainer(proxyClusterContainer);
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        if (databaseType instanceof OracleDatabaseType) {
            return DataSourceEnvironment.getURL(TypedSPILoader.getService(DatabaseType.class, "MySQL"), proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
        }
        return DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
    }
    
    @Override
    public int getProxyCDCPort() {
        return proxyContainer.getMappedPort(33071);
    }
    
    @Override
    public void cleanUpDatabase(final String databaseName) {
    }
}
