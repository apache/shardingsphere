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

package org.apache.shardingsphere.test.integration.showprocesslist.container.composer;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.ITContainers;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.GovernanceContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.AdapterContainerUtil;
import org.apache.shardingsphere.test.integration.showprocesslist.parameter.ShowProcessListParameterized;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Cluster show process list composed container.
 */
public final class ClusterShowProcessListContainerComposer {
    
    private final ITContainers containers;
    
    private final GovernanceContainer governanceContainer;
    
    private final AdapterContainer jdbcContainer;
    
    private final AdapterContainer proxyContainer;
    
    public ClusterShowProcessListContainerComposer(final ShowProcessListParameterized parameterized) {
        containers = new ITContainers(parameterized.getScenario());
        governanceContainer = containers.registerContainer(GovernanceContainerFactory.newInstance("ZooKeeper"));
        StorageContainer storageContainer = containers.registerContainer(StorageContainerFactory.newInstance(parameterized.getDatabaseType(), "", parameterized.getScenario(),
                StorageContainerConfigurationFactory.newInstance(parameterized.getDatabaseType())));
        AdaptorContainerConfiguration containerConfig = new AdaptorContainerConfiguration(parameterized.getScenario(),
                getMountedResources(parameterized.getScenario(), parameterized.getDatabaseType()), AdapterContainerUtil.getAdapterContainerImage());
        jdbcContainer = AdapterContainerFactory.newInstance(
                "cluster", "jdbc", parameterized.getDatabaseType(), storageContainer, parameterized.getScenario(), containerConfig);
        proxyContainer = AdapterContainerFactory.newInstance(
                "cluster", "proxy", parameterized.getDatabaseType(), storageContainer, parameterized.getScenario(), containerConfig);
        if (proxyContainer instanceof DockerITContainer) {
            ((DockerITContainer) proxyContainer).dependsOn(governanceContainer, storageContainer);
        }
        containers.registerContainer(proxyContainer);
        containers.registerContainer(jdbcContainer);
    }
    
    private Map<String, String> getMountedResources(final String scenario, final DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(2, 1);
        String pathInContainer = "/opt/shardingsphere-proxy/conf";
        result.put("/env/common/cluster/proxy/conf/", pathInContainer);
        result.put("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), pathInContainer);
        return result;
    }
    
    /**
     * Get jdbc data source.
     *
     * @return data source
     */
    public DataSource getJdbcDataSource() {
        return jdbcContainer.getTargetDataSource(governanceContainer.getServerLists());
    }
    
    /**
     * Get proxy data source.
     *
     * @return data source
     */
    public DataSource getProxyDataSource() {
        return proxyContainer.getTargetDataSource(governanceContainer.getServerLists());
    }
    
    /**
     * Start.
     */
    public void start() {
        containers.start();
    }
    
    /**
     * Stop.
     */
    public void stop() {
        containers.stop();
    }
}
