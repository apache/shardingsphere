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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.container.wait.JDBCConnectionWaitStrategy;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.google.common.base.Strings;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy container for cluster mode.
 */
public final class ShardingSphereProxyClusterContainer extends DockerITContainer implements AdapterContainer {
    
    private static final String AGENT_HOME_IN_CONTAINER = "/usr/local/shardingsphere-agent";
    
    private static final String PROPERTY_AGENT_HOME = "AGENT_HOME";
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final String module;
    
    private final StorageContainer storageContainer;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyClusterContainer(final DatabaseType databaseType, final String scenario, final StorageContainer storageContainer, final String module) {
        super("ShardingSphere-Proxy", "apache/shardingsphere-proxy-test");
        this.databaseType = databaseType;
        this.scenario = scenario;
        this.module = module;
        this.storageContainer = storageContainer;
    }
    
    /**
     * Mount the agent into container.
     *
     * @param agentHome agent home
     * @return self
     */
    public ShardingSphereProxyClusterContainer withAgent(final String agentHome) {
        withEnv(PROPERTY_AGENT_HOME, AGENT_HOME_IN_CONTAINER);
        withFileSystemBind(agentHome, AGENT_HOME_IN_CONTAINER, BindMode.READ_ONLY);
        return this;
    }
    
    @Override
    protected void configure() {
        withExposedPorts(3307);
        mapConfigurationFiles();
        if (Strings.isNullOrEmpty(module)) {
            setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType,
                    getHost(), getMappedPort(3307), scenario), "proxy", "Proxy@123")));
        }
        if ("scaling".equalsIgnoreCase(module)) {
            scalingConfigure();
        }
    }
    
    private void scalingConfigure() {
        if (DatabaseTypeUtil.isPostgreSQL(databaseType)) {
            setWaitStrategy(
                    new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), "postgres"), "proxy", "Proxy@123")));
        } else if (DatabaseTypeUtil.isMySQL(databaseType)) {
            setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), ""), "proxy", "Proxy@123")));
        }
    }
    
    private void mapConfigurationFiles() {
        String pathInContainer = "/opt/shardingsphere-proxy/conf";
        if (Strings.isNullOrEmpty(module)) {
            withClasspathResourceMapping("/env/common/standalone/proxy/conf/", pathInContainer, BindMode.READ_ONLY);
            withClasspathResourceMapping("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), pathInContainer, BindMode.READ_ONLY);
        }
        if ("scaling".equalsIgnoreCase(module)) {
            mapScalingConfigurationFiles();
        }
    }
    
    private void mapScalingConfigurationFiles() {
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            String majorVersion = DatabaseTypeUtil.parseMajorVersion(((GenericContainer<?>) storageContainer).getDockerImageName());
            withClasspathResourceMapping(String.format("/env/%s/server-%s.yaml", databaseType.getType().toLowerCase(), majorVersion),
                    "/opt/shardingsphere-proxy/conf/server.yaml", BindMode.READ_ONLY);
        } else {
            withClasspathResourceMapping(String.format("/env/%s/server.yaml", databaseType.getType().toLowerCase()), "/opt/shardingsphere-proxy/conf/server.yaml", BindMode.READ_ONLY);
        }
        withClasspathResourceMapping("/env/logback.xml", "/opt/shardingsphere-proxy/conf/logback.xml", BindMode.READ_ONLY);
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
