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

package org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.wait.JDBCConnectionWaitStrategy;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy container.
 */
@Slf4j
public final class ShardingSphereProxyStandaloneContainer extends DockerITContainer implements AdapterContainer {
    
    private static final String AGENT_HOME_IN_CONTAINER = "/usr/local/shardingsphere-agent";
    
    private static final String PROPERTY_AGENT_HOME = "AGENT_HOME";
    
    private final DatabaseType databaseType;
    
    private final AdaptorContainerConfiguration config;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyStandaloneContainer(final DatabaseType databaseType, final AdaptorContainerConfiguration config) {
        super("ShardingSphere-Proxy", "apache/shardingsphere-proxy-test");
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
        withEnv(PROPERTY_AGENT_HOME, AGENT_HOME_IN_CONTAINER);
        withFileSystemBind(agentHome, AGENT_HOME_IN_CONTAINER, BindMode.READ_ONLY);
        return this;
    }
    
    @Override
    protected void configure() {
        withExposedPorts(3307);
        mountConfigurationFiles();
        setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(
                DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), config.getProxyDataSourceName()), "root", "Root@123")));
    }
    
    private void mountConfigurationFiles() {
        config.getMountedResources().forEach((key, value) -> withClasspathResourceMapping(key, value, BindMode.READ_ONLY));
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (Objects.isNull(dataSource)) {
            targetDataSourceProvider.set(createProxyDataSource());
        }
        return targetDataSourceProvider.get();
    }
    
    private DataSource createProxyDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(databaseType));
        result.setJdbcUrl(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), config.getProxyDataSourceName()));
        result.setUsername("proxy");
        result.setPassword("Proxy@123");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return "proxy";
    }
}
