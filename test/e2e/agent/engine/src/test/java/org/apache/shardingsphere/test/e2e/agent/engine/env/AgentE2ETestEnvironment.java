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

package org.apache.shardingsphere.test.e2e.agent.engine.env;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.agent.engine.container.ShardingSphereJdbcAgentContainer;
import org.apache.shardingsphere.test.e2e.agent.engine.container.plugin.AgentPluginContainerFactory;
import org.apache.shardingsphere.test.e2e.agent.engine.container.plugin.AgentPluginHTTPEndpointProvider;
import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestConfiguration;
import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestImageConfiguration;
import org.apache.shardingsphere.test.e2e.agent.fixture.proxy.ProxyRequestExecutor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.ITContainers;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.option.StorageContainerConfigurationOption;
import org.awaitility.Awaitility;
import org.testcontainers.containers.output.OutputFrame;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Agent E2E test environment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AgentE2ETestEnvironment {
    
    private static final AgentE2ETestEnvironment INSTANCE = new AgentE2ETestEnvironment();
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final AgentE2ETestConfiguration testConfig = AgentE2ETestConfiguration.getInstance();
    
    private final AgentE2ETestImageConfiguration imageConfig = AgentE2ETestImageConfiguration.getInstance();
    
    @Getter
    private final Collection<String> containerLogs = new LinkedList<>();
    
    private ITContainers containers;
    
    @Getter
    private String agentPluginURL;
    
    private ProxyRequestExecutor proxyRequestExecutor;
    
    private boolean initialized;
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static AgentE2ETestEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init environment.
     */
    public void init() {
        if (!AgentE2ETestConfiguration.getInstance().containsTestParameter()) {
            return;
        }
        Optional<DockerITContainer> agentPluginContainer = TypedSPILoader.findService(AgentPluginContainerFactory.class, testConfig.getPluginType()).map(AgentPluginContainerFactory::create);
        if (AdapterType.PROXY.getValue().equalsIgnoreCase(testConfig.getAdapter())) {
            createProxyEnvironment(agentPluginContainer.orElse(null));
        } else if (AdapterType.JDBC.getValue().equalsIgnoreCase(testConfig.getAdapter())) {
            createJDBCEnvironment(agentPluginContainer.orElse(null));
        }
        log.info("Waiting to collect data ...");
        long collectDataWaitSeconds = testConfig.getCollectDataWaitSeconds();
        if (collectDataWaitSeconds > 0L) {
            Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(collectDataWaitSeconds + 1L)).pollDelay(collectDataWaitSeconds, TimeUnit.SECONDS).until(() -> true);
        }
        agentPluginURL = agentPluginContainer.map(optional -> new AgentPluginHTTPEndpointProvider().getHURL(optional, testConfig.getDefaultExposePort())).orElse(null);
        initialized = true;
    }
    
    private void createProxyEnvironment(final DockerITContainer agentPluginContainer) {
        containers = new ITContainers();
        ShardingSphereProxyClusterContainer proxyContainer = new ShardingSphereProxyClusterContainer(databaseType, getAdaptorContainerConfiguration());
        proxyContainer.withLogConsumer(testConfig.isLogEnabled() ? this::collectLogs : null);
        StorageContainer storageContainer = StorageContainerFactory.newInstance(
                databaseType, imageConfig.getMysqlImage(), DatabaseTypedSPILoader.getService(StorageContainerConfigurationOption.class, databaseType), null);
        proxyContainer.dependsOn(storageContainer);
        containers.registerContainer(storageContainer);
        GovernanceContainer governanceContainer = GovernanceContainerFactory.newInstance("ZooKeeper");
        proxyContainer.dependsOn(governanceContainer);
        containers.registerContainer(governanceContainer);
        if (null != agentPluginContainer) {
            proxyContainer.dependsOn(agentPluginContainer);
            containers.registerContainer(agentPluginContainer);
        }
        containers.registerContainer(proxyContainer);
        containers.start();
        try {
            proxyRequestExecutor = new ProxyRequestExecutor(proxyContainer.getTargetDataSource(null).getConnection());
            proxyRequestExecutor.start();
        } catch (final SQLException ignored) {
        }
    }
    
    private AdaptorContainerConfiguration getAdaptorContainerConfiguration() {
        Map<String, String> mountedResources = new HashMap<>(3, 1F);
        mountedResources.put("/env/proxy/conf/global.yaml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "global.yaml");
        mountedResources.put("/env/proxy/conf/database-db.yaml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "database-db.yaml");
        if (!Strings.isNullOrEmpty(testConfig.getPluginType())) {
            mountedResources.put(String.format("/env/agent/conf/%s/agent.yaml", testConfig.getPluginType()), ProxyContainerConstants.AGENT_CONFIG_PATH_IN_CONTAINER + "agent.yaml");
        }
        return new AdaptorContainerConfiguration("sharding_db", Collections.emptyList(), mountedResources, imageConfig.getProxyImage(), "");
    }
    
    private void createJDBCEnvironment(final DockerITContainer agentPluginContainer) {
        containers = new ITContainers();
        StorageContainer storageContainer = StorageContainerFactory.newInstance(
                databaseType, imageConfig.getMysqlImage(), DatabaseTypedSPILoader.getService(StorageContainerConfigurationOption.class, databaseType), null);
        ShardingSphereJdbcAgentContainer jdbcAgentContainer = new ShardingSphereJdbcAgentContainer(
                imageConfig.getJdbcProjectImage(), testConfig.getPluginType(), testConfig.isLogEnabled() ? this::collectLogs : null);
        jdbcAgentContainer.dependsOn(storageContainer);
        if (null != agentPluginContainer) {
            jdbcAgentContainer.dependsOn(agentPluginContainer);
            containers.registerContainer(agentPluginContainer);
        }
        containers.registerContainer(storageContainer);
        containers.registerContainer(jdbcAgentContainer);
        containers.start();
    }
    
    private void collectLogs(final OutputFrame outputFrame) {
        if (!initialized) {
            containerLogs.add(outputFrame.getUtf8StringWithoutLineEnding());
        }
    }
    
    /**
     * Destroy the environment.
     */
    public void destroy() {
        if (!AgentE2ETestConfiguration.getInstance().containsTestParameter()) {
            return;
        }
        if (null != proxyRequestExecutor) {
            proxyRequestExecutor.stop();
        }
        if (null != containers) {
            containers.stop();
        }
    }
}
