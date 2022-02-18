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

import com.google.common.io.ByteStreams;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy container.
 */
@Slf4j
public final class ShardingSphereProxyContainer extends DockerITContainer implements AdapterContainer {
    
    private static final String AGENT_HOME_IN_CONTAINER = "/usr/local/shardingsphere-agent";
    
    private static final String PROPERTY_AGENT_HOME = "AGENT_HOME";
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyContainer(final DatabaseType databaseType, final String scenario) {
        super("ShardingSphere-Proxy", "apache/shardingsphere-proxy-test");
        this.databaseType = databaseType;
        this.scenario = scenario;
    }
    
    /**
     * Mount path into container from classpath.
     *
     * @param classPathResource resource path in classpath
     * @param containerPath path in container
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
        withConfMapping("/env/" + scenario + "/docker/proxy/conf/" + databaseType.getName().toLowerCase());
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*ShardingSphere-Proxy .* mode started successfully.*"));
        super.configure();
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
        YamlUserConfiguration userConfig = loadUserConfiguration();
        result.setUsername(userConfig.getUsername());
        result.setPassword(userConfig.getPassword());
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        if ("MySQL".equals(databaseType.getName())) {
            result.setConnectionInitSql("SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
        }
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private YamlUserConfiguration loadUserConfiguration() {
        String serverFile = "/env/" + scenario + "/docker/proxy/conf/" + databaseType.getName().toLowerCase() + "/server.yaml";
        YamlProxyServerConfiguration serverConfig = YamlEngine.unmarshal(
                ByteStreams.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream(serverFile))), YamlProxyServerConfiguration.class);
        return YamlUsersConfigurationConverter.convertYamlUserConfiguration(getProxyUsers(serverConfig)).stream().findFirst().orElse(new YamlUserConfiguration());
    }
    
    private Collection<String> getProxyUsers(final YamlProxyServerConfiguration serverConfig) {
        return serverConfig.getRules()
                .stream().filter(each -> each instanceof YamlAuthorityRuleConfiguration).findFirst().map(each -> ((YamlAuthorityRuleConfiguration) each).getUsers()).orElse(Collections.emptyList());
    }
}
