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

package org.apache.shardingsphere.test.e2e.operation.showprocesslist.container.composer;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.ITContainers;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.option.GovernanceContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.util.AdapterContainerUtils;
import org.apache.shardingsphere.test.e2e.operation.showprocesslist.env.ShowProcessListEnvironment;
import org.apache.shardingsphere.test.e2e.operation.showprocesslist.parameter.ShowProcessListTestParameter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Cluster show process list composed container.
 */
public final class ClusterShowProcessListContainerComposer implements AutoCloseable {
    
    private final ITContainers containers;
    
    private final GovernanceContainer governanceContainer;
    
    private final AdapterContainer jdbcContainer;
    
    private final AdapterContainer proxyContainer;
    
    public ClusterShowProcessListContainerComposer(final ShowProcessListTestParameter testParam) {
        containers = new ITContainers(testParam.getScenario());
        governanceContainer = isClusterMode(testParam.getRunMode())
                ? containers.registerContainer(new GovernanceContainer(TypedSPILoader.getService(GovernanceContainerOption.class, "ZooKeeper")))
                : null;
        StorageContainer storageContainer = containers.registerContainer(
                new DockerStorageContainer("", DatabaseTypedSPILoader.getService(StorageContainerOption.class, testParam.getDatabaseType()), testParam.getScenario()));
        AdaptorContainerConfiguration containerConfig = new AdaptorContainerConfiguration(testParam.getScenario(), new LinkedList<>(),
                getMountedResources(testParam.getScenario(), testParam.getDatabaseType(), testParam.getRunMode(), testParam.getGovernanceCenter()), AdapterContainerUtils.getAdapterContainerImage(),
                "");
        String envType = ShowProcessListEnvironment.getInstance().getItEnvType().name();
        jdbcContainer = AdapterContainerFactory.newInstance(AdapterType.JDBC, testParam.getDatabaseType(), testParam.getScenario(), containerConfig, storageContainer, envType);
        proxyContainer = AdapterContainerFactory.newInstance(AdapterType.PROXY, testParam.getDatabaseType(), testParam.getScenario(), containerConfig, storageContainer, envType);
        if (proxyContainer instanceof DockerITContainer) {
            if (isClusterMode(testParam.getRunMode())) {
                ((DockerITContainer) proxyContainer).dependsOn(governanceContainer);
            }
            ((DockerITContainer) proxyContainer).dependsOn(storageContainer);
        }
        containers.registerContainer(proxyContainer);
        containers.registerContainer(jdbcContainer);
    }
    
    private Map<String, String> getMountedResources(final String scenario, final DatabaseType databaseType, final String runMode, final String governanceCenter) {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put(isClusterMode(runMode) ? String.format("/env/common/cluster/proxy/%s/conf/", governanceCenter.toLowerCase())
                : "/env/common/standalone/proxy/conf/", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
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
    
    @Override
    public void close() {
        containers.stop();
    }
}
