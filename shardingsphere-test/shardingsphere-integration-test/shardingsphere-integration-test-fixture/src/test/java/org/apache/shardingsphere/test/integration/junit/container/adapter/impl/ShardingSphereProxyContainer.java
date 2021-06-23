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

package org.apache.shardingsphere.test.integration.junit.container.adapter.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy container.
 */
@Slf4j
public final class ShardingSphereProxyContainer extends ShardingSphereAdapterContainer {
    
    private static final String AGENT_HOME_IN_CONTAINER = "/usr/local/shardingsphere-agent";
    
    private static final String PROPERTY_AGENT_HOME = "AGENT_HOME";
    
    private final AtomicReference<DataSource> dataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyContainer(final ParameterizedArray parameterizedArray) {
        this(null, parameterizedArray);
    }
    
    public ShardingSphereProxyContainer(final String dockerName, final ParameterizedArray parameterizedArray) {
        super(Objects.isNull(dockerName) ? "ShardingSphere-Proxy" : dockerName, "apache/shardingsphere-proxy-test", parameterizedArray);
    }
    
    /**
     * Mount path into container from classpath.
     *
     * @param classPathResource resource path in classpath
     * @param containerPath     path in container
     * @return self
     */
    public ShardingSphereProxyContainer withClassPathResourceMapping(final String classPathResource, final String containerPath) {
        withCopyFileToContainer(MountableFile.forClasspathResource(classPathResource), containerPath);
        return this;
    }
    
    /**
     * Mount configure path into container from classpath.
     *
     * @param resourcePath resource path
     * @return self
     */
    public ShardingSphereProxyContainer withConfMapping(final String resourcePath) {
        return withClassPathResourceMapping(resourcePath, "/opt/shardingsphere-proxy/conf");
    }
    
    /**
     * Mount the agent into container.
     *
     * @param agentHome agent home
     * @return self
     */
    public ShardingSphereProxyContainer withAgent(final String agentHome) {
        withEnv(PROPERTY_AGENT_HOME, AGENT_HOME_IN_CONTAINER);
        withFileSystemBind(agentHome, AGENT_HOME_IN_CONTAINER, BindMode.READ_ONLY);
        return this;
    }
    
    @Override
    protected void configure() {
        withConfMapping("/docker/" + getParameterizedArray().getScenario() + "/proxy/conf");
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*ShardingSphere-Proxy start success.*"));
        super.configure();
    }
    
    @Override
    protected void execute() {
        log.info("Mapped port 3307: {}", getMappedPort(3307));
        log.info("Mapped port 3308: {}", getMappedPort(3308));
    }
    
    /**
     * Get DataSource.
     *
     * @return DataSource
     */
    public DataSource getDataSource() {
        DataSource dataSource = dataSourceProvider.get();
        if (Objects.isNull(dataSource)) {
            dataSourceProvider.lazySet(createDataSource());
        }
        return dataSourceProvider.get();
    }
    
    private DataSource createDataSource() {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setJdbcUrl(getURL());
        result.setUsername(getAuthentication().getUsername());
        result.setPassword(getAuthentication().getPassword());
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        result.setConnectionInitSql("SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
        return new HikariDataSource(result);
    }
    
    protected String getURL() {
        return String.format("jdbc:mysql://%s:%s/%s?useServerPrepStmts=true&serverTimezone=UTC&useSSL=false&useLocalSessionState=true&characterEncoding=utf-8",
                getHost(), getMappedPort(3307), getParameterizedArray().getScenario());
    }
}
