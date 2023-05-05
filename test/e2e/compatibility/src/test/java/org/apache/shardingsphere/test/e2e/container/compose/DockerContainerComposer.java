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

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.container.config.proxy.ProxyContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.mysql.MySQLContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;

import java.util.Collections;
import java.util.Map;

/**
 * Composed container, include governance container and database container.
 */
public final class DockerContainerComposer extends BaseContainerComposer {
    
    private final DatabaseType databaseType;
    
    private final ShardingSphereProxyClusterContainer proxyContainer;
    
    @Getter
    private final DockerStorageContainer storageContainer;
    
    public DockerContainerComposer(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        StorageContainerConfiguration storageContainerConfig;
        if (DatabaseTypeUtils.isMySQL(databaseType)) {
            Map<String, String> mountedResources = Collections.singletonMap("/env/mysql/my.cnf", MySQLContainer.MYSQL_CONF_IN_CONTAINER);
            storageContainerConfig = MySQLContainerConfigurationFactory.newInstance(null, null, mountedResources);
        } else {
            storageContainerConfig = StorageContainerConfigurationFactory.newInstance(databaseType);
        }
        DockerStorageContainer storageContainer = getContainers().registerContainer((DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, "", null,
                    storageContainerConfig));
        storageContainer.setNetworkAliases(Collections.singletonList(String.join(".", databaseType.getType().toLowerCase(), "host")));
        this.storageContainer = storageContainer;
        AdaptorContainerConfiguration containerConfig = ProxyContainerConfigurationFactory.newInstance();
        ShardingSphereProxyClusterContainer proxyClusterContainer = (ShardingSphereProxyClusterContainer) AdapterContainerFactory.newInstance(
                AdapterMode.STANDALONE, AdapterType.PROXY, databaseType, null, "", containerConfig);
        proxyContainer = getContainers().registerContainer(proxyClusterContainer);
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
    }
}
