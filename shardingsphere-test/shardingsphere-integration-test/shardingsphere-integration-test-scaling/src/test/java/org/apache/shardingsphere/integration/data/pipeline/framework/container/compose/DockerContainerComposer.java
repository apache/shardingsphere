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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.config.proxy.ScalingProxyClusterContainerConfigurationFactory;
import org.apache.shardingsphere.integration.data.pipeline.util.DockerImageVersion;
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
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.mysql.MySQLContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;

import java.util.Collections;

/**
 * Composed container, include governance container and database container.
 */
public final class DockerContainerComposer extends BaseContainerComposer {
    
    private final DatabaseType databaseType;
    
    private final ShardingSphereProxyClusterContainer proxyContainer;
    
    @Getter
    private final DockerStorageContainer storageContainer;
    
    @Getter
    private final GovernanceContainer governanceContainer;
    
    public DockerContainerComposer(final String proxyContainerImage, final DatabaseType databaseType, final String storageContainerImage) {
        this.databaseType = databaseType;
        governanceContainer = getContainers().registerContainer(new ZookeeperContainer());
        StorageContainerConfiguration storageContainerConfig;
        if (DatabaseTypeUtil.isMySQL(databaseType) && new DockerImageVersion(storageContainerImage).getMajorVersion() > 5) {
            storageContainerConfig = MySQLContainerConfigurationFactory.newInstance(null, null,
                    Collections.singletonMap("/env/mysql/mysql8/my.cnf", StorageContainerConstants.MYSQL_CONF_IN_CONTAINER));
        } else {
            storageContainerConfig = StorageContainerConfigurationFactory.newInstance(databaseType);
        }
        storageContainer = getContainers().registerContainer((DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, storageContainerImage,
                "", StorageContainerConfigurationFactory.newInstance(databaseType)));
        AdaptorContainerConfiguration containerConfig = ScalingProxyClusterContainerConfigurationFactory.newInstance(proxyContainerImage, databaseType, storageContainerImage);
                "", storageContainerConfig));
        AdaptorContainerConfiguration containerConfig = ScalingProxyClusterContainerConfigurationFactory.newInstance(databaseType, storageContainerImage);
        ShardingSphereProxyClusterContainer proxyClusterContainer = (ShardingSphereProxyClusterContainer) AdapterContainerFactory.newInstance(EnvironmentConstants.CLUSTER_MODE,
                AdapterContainerConstants.PROXY, databaseType, storageContainer, "", containerConfig);
        proxyClusterContainer.dependsOn(governanceContainer, storageContainer);
        proxyContainer = getContainers().registerContainer(proxyClusterContainer);
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
    }
    
    @Override
    public void cleanUpDatabase(final String databaseName) {
    }
}
