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
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.E2EContainers;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.option.GovernanceContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.showprocesslist.parameter.ShowProcessListTestParameter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Cluster show process list composed container.
 */
public final class ClusterShowProcessListContainerComposer implements AutoCloseable {
    
    private final E2EContainers containers;
    
    private final GovernanceContainer governanceContainer;
    
    private final AdapterContainer jdbcContainer;
    
    private final AdapterContainer proxyContainer;
    
    public ClusterShowProcessListContainerComposer(final ShowProcessListTestParameter testParam) {
        containers = new E2EContainers(testParam.getScenario());
        governanceContainer = isClusterMode(testParam.getMode())
                ? containers.registerContainer(new GovernanceContainer(TypedSPILoader.getService(GovernanceContainerOption.class, "ZooKeeper")))
                : null;
        StorageContainer storageContainer = containers.registerContainer(
                new DockerStorageContainer("", DatabaseTypedSPILoader.getService(StorageContainerOption.class, testParam.getDatabaseType()), testParam.getScenario()));
        String proxyImage = E2ETestEnvironment.getInstance().getDockerEnvironment().getProxyImage();
        AdaptorContainerConfiguration containerConfig = new AdaptorContainerConfiguration(testParam.getScenario(), new LinkedList<>(),
                getMountedResources(testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), testParam.getRegCenterType()), proxyImage, "");
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        jdbcContainer = AdapterContainerFactory.newInstance(Adapter.JDBC, testParam.getDatabaseType(), testParam.getScenario(), containerConfig, storageContainer, type);
        proxyContainer = AdapterContainerFactory.newInstance(Adapter.PROXY, testParam.getDatabaseType(), testParam.getScenario(), containerConfig, storageContainer, type);
        if (proxyContainer instanceof DockerE2EContainer) {
            if (isClusterMode(testParam.getMode())) {
                ((DockerE2EContainer) proxyContainer).dependsOn(governanceContainer);
            }
            ((DockerE2EContainer) proxyContainer).dependsOn(storageContainer);
        }
        containers.registerContainer(proxyContainer);
        containers.registerContainer(jdbcContainer);
    }
    
    private Map<String, String> getMountedResources(final String scenario, final DatabaseType databaseType, final Mode mode, final String refCenterType) {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put(isClusterMode(mode) ? String.format("/env/common/cluster/proxy/%s/conf/", refCenterType.toLowerCase())
                : "/env/common/standalone/proxy/conf/", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        result.put("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER);
        return result;
    }
    
    private boolean isClusterMode(final Mode mode) {
        return Mode.CLUSTER == mode;
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
