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
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.ProxyContainerConstants;
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
        governanceContainer = isClusterMode(parameterized.getRunMode()) ? containers.registerContainer(GovernanceContainerFactory.newInstance("ZooKeeper")) : null;
        StorageContainer storageContainer = containers.registerContainer(StorageContainerFactory.newInstance(parameterized.getDatabaseType(), "", parameterized.getScenario(),
                StorageContainerConfigurationFactory.newInstance(parameterized.getDatabaseType())));
        AdaptorContainerConfiguration containerConfig = new AdaptorContainerConfiguration(parameterized.getScenario(),
                getMountedResources(parameterized.getScenario(), parameterized.getDatabaseType(), parameterized.getRunMode()), AdapterContainerUtil.getAdapterContainerImage());
        jdbcContainer = AdapterContainerFactory.newInstance(parameterized.getRunMode(), "jdbc", parameterized.getDatabaseType(), storageContainer, parameterized.getScenario(), containerConfig);
        proxyContainer = AdapterContainerFactory.newInstance(parameterized.getRunMode(), "proxy", parameterized.getDatabaseType(), storageContainer, parameterized.getScenario(), containerConfig);
        if (proxyContainer instanceof DockerITContainer) {
            if (isClusterMode(parameterized.getRunMode())) {
                ((DockerITContainer) proxyContainer).dependsOn(governanceContainer);
            }
            ((DockerITContainer) proxyContainer).dependsOn(storageContainer);
        }
        containers.registerContainer(proxyContainer);
        containers.registerContainer(jdbcContainer);
    }
    
    private Map<String, String> getMountedResources(final String scenario, final DatabaseType databaseType, final String runMode) {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put(isClusterMode(runMode) ? "/env/common/cluster/proxy/conf/" : "/env/common/standalone/proxy/conf/", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        result.put("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        return result;
    }
    
    private boolean isClusterMode(final String runMode) {
        return "Cluster".equals(runMode);
    }
    
    /**
     * Get jdbc data source.
     *
     * @return data source
     */
    public DataSource getJdbcDataSource() {
        return jdbcContainer.getTargetDataSource(null == governanceContainer ? null : governanceContainer.getServerLists());
    }
    
    /**
     * Get proxy data source.
     *
     * @return data source
     */
    public DataSource getProxyDataSource() {
        return proxyContainer.getTargetDataSource(null == governanceContainer ? null : governanceContainer.getServerLists());
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
