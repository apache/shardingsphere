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

package org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy container.
 */
@Slf4j
public final class ShardingSphereProxyStandaloneContainer extends DockerITContainer implements AdapterContainer {
    
    private static final String PROPERTY_AGENT_HOME = "AGENT_HOME";
    
    private final DatabaseType databaseType;
    
    private final AdaptorContainerConfiguration config;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyStandaloneContainer(final DatabaseType databaseType, final AdaptorContainerConfiguration config) {
        super(ProxyContainerConstants.PROXY_CONTAINER_NAME_PREFIX, config.getAdapterContainerImage());
        this.databaseType = databaseType;
        this.config = config;
    }
    
    /**
     * Mount the agent into container.
     *
     * @param agentHome agent home
     * @return self
     */
    public ShardingSphereProxyStandaloneContainer withAgent(final String agentHome) {
        withEnv(PROPERTY_AGENT_HOME, ProxyContainerConstants.AGENT_HOME_IN_CONTAINER);
        withFileSystemBind(agentHome, ProxyContainerConstants.AGENT_HOME_IN_CONTAINER, BindMode.READ_ONLY);
        return this;
    }
    
    @Override
    protected void configure() {
        withExposedPorts(3307);
        mountConfigurationFiles();
        setWaitStrategy(new JdbcConnectionWaitStrategy(() -> DriverManager.getConnection(
                DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), config.getProxyDataSourceName()), "proxy", "Proxy@123")));
    }
    
    private void mountConfigurationFiles() {
        config.getMountedResources().forEach((key, value) -> withClasspathResourceMapping(key, value, BindMode.READ_ONLY));
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (null == dataSource) {
            targetDataSourceProvider.set(StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), config.getProxyDataSourceName()),
                    ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD));
        }
        return targetDataSourceProvider.get();
    }
    
    @Override
    public String getAbbreviation() {
        return ProxyContainerConstants.PROXY_CONTAINER_ABBREVIATION;
    }
}
