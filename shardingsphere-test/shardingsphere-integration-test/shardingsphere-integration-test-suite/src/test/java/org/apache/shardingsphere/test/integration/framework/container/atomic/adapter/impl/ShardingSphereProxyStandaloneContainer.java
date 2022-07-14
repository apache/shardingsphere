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

package org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.wait.JDBCConnectionWaitStrategy;
import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainer;
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
    
    private final String scenario;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyStandaloneContainer(final DatabaseType databaseType, final String scenario) {
        super("ShardingSphere-Proxy", "apache/shardingsphere-proxy-test");
        this.databaseType = databaseType;
        this.scenario = scenario;
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
        mapConfigurationFiles();
        setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), scenario), "root", "root")));
    }
    
    private void mapConfigurationFiles() {
        String containerPath = "/opt/shardingsphere-proxy/conf";
        withClasspathResourceMapping("/env/common/standalone/proxy/conf/", containerPath, BindMode.READ_ONLY);
        withClasspathResourceMapping("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), containerPath, BindMode.READ_ONLY);
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
        result.setJdbcUrl(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), scenario));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
}
