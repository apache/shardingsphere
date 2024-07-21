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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.agent.engine.container.ITContainers;
import org.apache.shardingsphere.test.e2e.agent.engine.container.MySQLContainer;
import org.apache.shardingsphere.test.e2e.agent.engine.container.ShardingSphereJdbcContainer;
import org.apache.shardingsphere.test.e2e.agent.engine.container.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.e2e.agent.engine.container.plugin.AgentPluginContainerFactory;
import org.apache.shardingsphere.test.e2e.agent.engine.container.plugin.AgentPluginHTTPEndpointProvider;
import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestConfiguration;
import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestImageConfiguration;
import org.apache.shardingsphere.test.e2e.agent.fixture.proxy.ProxyRequestExecutor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainerFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Agent E2E test environment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AgentE2ETestEnvironment {
    
    private static final AgentE2ETestEnvironment INSTANCE = new AgentE2ETestEnvironment();
    
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
            createProxyEnvironment(agentPluginContainer);
        } else if (AdapterType.JDBC.getValue().equalsIgnoreCase(testConfig.getAdapter())) {
            createJDBCEnvironment(agentPluginContainer);
        }
        log.info("Waiting to collect data ...");
        long collectDataWaitSeconds = testConfig.getCollectDataWaitSeconds();
        if (collectDataWaitSeconds > 0L) {
            Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(collectDataWaitSeconds + 1L)).pollDelay(collectDataWaitSeconds, TimeUnit.SECONDS).until(() -> true);
        }
        agentPluginURL = agentPluginContainer.map(optional -> new AgentPluginHTTPEndpointProvider().getHURL(optional, testConfig.getDefaultExposePort())).orElse(null);
        initialized = true;
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void createProxyEnvironment(final Optional<DockerITContainer> agentPluginContainer) {
        containers = new ITContainers();
        MySQLContainer storageContainer = new MySQLContainer(imageConfig.getMysqlImage());
        GovernanceContainer governanceContainer = GovernanceContainerFactory.newInstance("ZooKeeper");
        ShardingSphereProxyContainer proxyContainer = new ShardingSphereProxyContainer(imageConfig.getProxyImage(), testConfig.getPluginType(), testConfig.isLogEnabled() ? this::collectLogs : null);
        proxyContainer.dependsOn(storageContainer);
        proxyContainer.dependsOn(governanceContainer);
        agentPluginContainer.ifPresent(proxyContainer::dependsOn);
        agentPluginContainer.ifPresent(optional -> containers.registerContainer(optional));
        containers.registerContainer(storageContainer);
        containers.registerContainer(governanceContainer);
        containers.registerContainer(proxyContainer);
        containers.start();
        try {
            proxyRequestExecutor = new ProxyRequestExecutor(proxyContainer.getConnection());
            proxyRequestExecutor.start();
        } catch (final SQLException ignored) {
        }
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void createJDBCEnvironment(final Optional<DockerITContainer> agentPluginContainer) {
        containers = new ITContainers();
        MySQLContainer storageContainer = new MySQLContainer(imageConfig.getMysqlImage());
        ShardingSphereJdbcContainer jdbcContainer = new ShardingSphereJdbcContainer(
                imageConfig.getJdbcProjectImage(), testConfig.getPluginType(), testConfig.isLogEnabled() ? this::collectLogs : null);
        jdbcContainer.dependsOn(storageContainer);
        agentPluginContainer.ifPresent(jdbcContainer::dependsOn);
        agentPluginContainer.ifPresent(optional -> containers.registerContainer(optional));
        containers.registerContainer(storageContainer);
        containers.registerContainer(jdbcContainer);
        containers.start();
    }
    
    private void collectLogs(final OutputFrame outputFrame) {
        if (!initialized) {
            containerLogs.add(outputFrame.getUtf8StringWithoutLineEnding());
        }
    }
    
    /**
     * Destroy environment.
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
