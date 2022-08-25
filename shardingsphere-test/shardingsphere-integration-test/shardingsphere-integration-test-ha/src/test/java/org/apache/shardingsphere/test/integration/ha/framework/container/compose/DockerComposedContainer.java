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
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.impl.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;

import java.util.List;

/**
 * Composed container, include governance container and database container.
 */
@Slf4j
@Getter
public final class DockerComposedContainer extends AbstractComposedContainer {
    
    private final DatabaseType databaseType;
    
    private final ShardingSphereProxyClusterContainer proxyContainer;
    
    @Getter
    private final List<DockerStorageContainer> storageContainers;
    
    @Getter
    private final GovernanceContainer governanceContainer;
    
    public DockerComposedContainer(final DatabaseType databaseType, final String dockerImageName) {
        super("");
        this.databaseType = databaseType;
        this.proxyContainer = null;
        this.storageContainers = null;
        governanceContainer = getContainers().registerContainer(new ZookeeperContainer());
//        storageContainer = getContainers().registerContainer((DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, dockerImageName,
//                "", StorageContainerConfigurationFactory.newInstance(databaseType)));
//        AdaptorContainerConfiguration containerConfig = ScalingProxyClusterContainerConfigurationFactory.newInstance(databaseType, dockerImageName);
//        ShardingSphereProxyClusterContainer proxyClusterContainer =
//                (ShardingSphereProxyClusterContainer) AdapterContainerFactory.newInstance("Cluster", "proxy", databaseType, storageContainer, "", containerConfig);
//        proxyClusterContainer.dependsOn(governanceContainer, storageContainer);
//        proxyContainer = getContainers().registerContainer(proxyClusterContainer);
    }
    
    public String getProxyJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
    }
}
