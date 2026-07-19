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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.option.GovernanceContainerOption;
import org.testcontainers.containers.GenericContainer;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy-backed runtime fixture support for workflow E2E tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyWorkflowRuntimeTestSupport {
    
    private static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    private static final String STORAGE_NETWORK_ALIAS = "mysql.workflow.host";
    
    private static final String GOVERNANCE_NETWORK_ALIAS = "zk.workflow.host";
    
    private static final String PROXY_USER = "proxy";
    
    private static final String PROXY_PASSWORD = "Proxy@123";
    
    /**
     * Create Proxy-backed runtime fixture.
     *
     * @return runtime fixture
     * @throws SQLException SQL exception
     */
    public static ProxyWorkflowRuntimeFixture createFixture() throws SQLException {
        GenericContainer<?> storageContainer = MySQLRuntimeTestSupport.createContainer().withNetworkAliases(STORAGE_NETWORK_ALIAS);
        ShardingSphereProxyEmbeddedContainer proxyContainer = createProxyContainer("/proxy/workflow/global.yaml");
        boolean success = false;
        storageContainer.start();
        try {
            MySQLRuntimeTestSupport.initializeDatabase(storageContainer);
            proxyContainer.dependsOn(storageContainer);
            proxyContainer.start();
            success = true;
            return new ProxyWorkflowRuntimeFixture(List.of(storageContainer), proxyContainer, createRuntimeDatabases(proxyContainer.getProxyPort()));
        } finally {
            if (!success) {
                proxyContainer.stop();
                storageContainer.stop();
            }
        }
    }
    
    /**
     * Create Cluster-mode Proxy-backed runtime fixture.
     *
     * @return runtime fixture
     * @throws SQLException SQL exception
     */
    public static ProxyWorkflowRuntimeFixture createClusterFixture() throws SQLException {
        GenericContainer<?> storageContainer = MySQLRuntimeTestSupport.createContainer().withNetworkAliases(STORAGE_NETWORK_ALIAS);
        GovernanceContainer governanceContainer = new GovernanceContainer(TypedSPILoader.getService(GovernanceContainerOption.class, "ZooKeeper"));
        governanceContainer.withNetworkAliases(GOVERNANCE_NETWORK_ALIAS);
        ShardingSphereProxyEmbeddedContainer proxyContainer = createProxyContainer("/proxy/workflow/cluster/global.yaml");
        boolean success = false;
        storageContainer.start();
        try {
            MySQLRuntimeTestSupport.initializeDatabase(storageContainer);
            proxyContainer.dependsOn(storageContainer, governanceContainer);
            proxyContainer.start();
            success = true;
            return new ProxyWorkflowRuntimeFixture(List.of(storageContainer, governanceContainer), proxyContainer, createRuntimeDatabases(proxyContainer.getProxyPort()));
        } finally {
            if (!success) {
                proxyContainer.stop();
                governanceContainer.stop();
                storageContainer.stop();
            }
        }
    }
    
    private static ShardingSphereProxyEmbeddedContainer createProxyContainer(final String globalConfigurationResource) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        Map<String, String> mountedResources = new LinkedHashMap<>(2, 1F);
        mountedResources.put(globalConfigurationResource, "/opt/shardingsphere-proxy/conf/global.yaml");
        mountedResources.put("/proxy/workflow/database-logic-db.yaml", "/opt/shardingsphere-proxy/conf/database-logic-db.yaml");
        return new ShardingSphereProxyEmbeddedContainer(databaseType, new AdaptorContainerConfiguration(LOGICAL_DATABASE_NAME, List.of(), mountedResources, "", ""), 0);
    }
    
    private static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final int proxyPort) {
        String jdbcUrl = String.format("jdbc:mysql://127.0.0.1:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                proxyPort, LOGICAL_DATABASE_NAME);
        return Map.of(LOGICAL_DATABASE_NAME, new RuntimeDatabaseConfiguration(jdbcUrl, PROXY_USER, PROXY_PASSWORD, "com.mysql.cj.jdbc.Driver"));
    }
    
    /**
     * Proxy-backed runtime fixture.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class ProxyWorkflowRuntimeFixture implements AutoCloseable {
        
        private final List<GenericContainer<?>> supportingContainers;
        
        private final ShardingSphereProxyEmbeddedContainer proxyContainer;
        
        private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
        
        private final String logicalDatabaseName = LOGICAL_DATABASE_NAME;
        
        @Override
        public void close() {
            proxyContainer.stop();
            supportingContainers.forEach(GenericContainer::stop);
        }
    }
}
