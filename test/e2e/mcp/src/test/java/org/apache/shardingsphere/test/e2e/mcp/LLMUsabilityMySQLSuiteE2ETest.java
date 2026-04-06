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

package org.apache.shardingsphere.test.e2e.mcp;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class LLMUsabilityMySQLSuiteE2ETest extends AbstractLLMUsabilityE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private GenericContainer<?> container;
    
    private int totalOrders;
    
    private String schemaName;
    
    @AfterEach
    void stopContainer() {
        if (null != container) {
            container.stop();
            container = null;
        }
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(isDockerAvailable(), "Docker is required for the MySQL-backed usability suite.");
        try {
            container = MySQLRuntimeTestSupport.createContainer();
            container.start();
            MySQLRuntimeTestSupport.initializeDatabase(container);
            schemaName = MySQLRuntimeTestSupport.detectSchema(container);
            totalOrders = MySQLRuntimeTestSupport.querySingleInt(container, "SELECT COUNT(*) AS total_orders FROM " + schemaName + ".orders");
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return MySQLRuntimeTestSupport.createRuntimeDatabases(container, "logic_db");
    }
    
    @Test
    void assertMinimalBaseline() throws IOException, InterruptedException {
        assertAdvisoryUsabilitySuite("minimal-usability-mysql",
                new LLMUsabilityScenarioCatalog().createMinimalBaseline("mysql", "logic_db", schemaName, "orders", COUNT_ORDERS_SQL, totalOrders));
    }
    
    private boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }
}
