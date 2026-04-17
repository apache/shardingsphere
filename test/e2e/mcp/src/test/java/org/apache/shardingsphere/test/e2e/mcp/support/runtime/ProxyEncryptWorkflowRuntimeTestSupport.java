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

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.testcontainers.containers.GenericContainer;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy-backed runtime fixture support for workflow E2E tests.
 */
public final class ProxyEncryptWorkflowRuntimeTestSupport {
    
    private static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    private static final String PHYSICAL_DATABASE_NAME = "orders";
    
    private static final String STORAGE_NETWORK_ALIAS = "mysql.workflow.host";
    
    private static final String PROXY_USER = "proxy";
    
    private static final String PROXY_PASSWORD = "Proxy@123";
    
    private static final int PROXY_PORT = 3307;
    
    private ProxyEncryptWorkflowRuntimeTestSupport() {
    }
    
    /**
     * Create Proxy-backed runtime fixture.
     *
     * @return runtime fixture
     * @throws SQLException SQL exception
     */
    public static ProxyEncryptWorkflowRuntimeFixture createFixture() throws SQLException {
        GenericContainer<?> storageContainer = MySQLRuntimeTestSupport.createContainer().withNetworkAliases(STORAGE_NETWORK_ALIAS);
        ShardingSphereProxyEmbeddedContainer proxyContainer = null;
        boolean success = false;
        storageContainer.start();
        try {
            MySQLRuntimeTestSupport.initializeDatabase(storageContainer);
            proxyContainer = createProxyContainer();
            proxyContainer.dependsOn(storageContainer);
            proxyContainer.start();
            success = true;
            return new ProxyEncryptWorkflowRuntimeFixture(storageContainer, proxyContainer, createRuntimeDatabases());
        } finally {
            if (!success) {
                if (null != proxyContainer) {
                    proxyContainer.stop();
                }
                storageContainer.stop();
            }
        }
    }
    
    private static ShardingSphereProxyEmbeddedContainer createProxyContainer() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        Map<String, String> mountedResources = new LinkedHashMap<>(2, 1F);
        mountedResources.put("/proxy/workflow/global.yaml", "/opt/shardingsphere-proxy/conf/global.yaml");
        mountedResources.put("/proxy/workflow/database-logic-db.yaml", "/opt/shardingsphere-proxy/conf/database-logic-db.yaml");
        return new ShardingSphereProxyEmbeddedContainer(databaseType, new AdaptorContainerConfiguration(LOGICAL_DATABASE_NAME, List.of(), mountedResources, "", ""));
    }
    
    private static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        String jdbcUrl = String.format("jdbc:mysql://127.0.0.1:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                PROXY_PORT, LOGICAL_DATABASE_NAME);
        return Map.of(LOGICAL_DATABASE_NAME, new RuntimeDatabaseConfiguration("MySQL", jdbcUrl, PROXY_USER, PROXY_PASSWORD, "com.mysql.cj.jdbc.Driver"));
    }
    
    /**
     * Proxy-backed runtime fixture.
     */
    @Getter
    public static final class ProxyEncryptWorkflowRuntimeFixture implements AutoCloseable {
        
        private final GenericContainer<?> storageContainer;
        
        private final ShardingSphereProxyEmbeddedContainer proxyContainer;
        
        private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
        
        private final String logicalDatabaseName = LOGICAL_DATABASE_NAME;
        
        private final String physicalDatabaseName = PHYSICAL_DATABASE_NAME;
        
        private ProxyEncryptWorkflowRuntimeFixture(final GenericContainer<?> storageContainer, final ShardingSphereProxyEmbeddedContainer proxyContainer,
                                                   final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
            this.storageContainer = storageContainer;
            this.proxyContainer = proxyContainer;
            this.runtimeDatabases = runtimeDatabases;
        }
        
        @Override
        public void close() {
            proxyContainer.stop();
            storageContainer.stop();
        }
    }
}
