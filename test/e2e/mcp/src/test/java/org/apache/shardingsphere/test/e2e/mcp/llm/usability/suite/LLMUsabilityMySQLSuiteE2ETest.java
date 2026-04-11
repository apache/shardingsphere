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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.suite;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.runtime.support.MySQLRuntimeTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class LLMUsabilityMySQLSuiteE2ETest extends AbstractLLMUsabilityE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private MySQLRuntimeTestSupport.LLMMySQLRuntimeFixture runtimeFixture;
    
    @AfterEach
    void closeRuntimeFixture() {
        if (null != runtimeFixture) {
            runtimeFixture.close();
            runtimeFixture = null;
        }
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(), "Docker is required for the MySQL-backed usability suite.");
        try {
            runtimeFixture = MySQLRuntimeTestSupport.createLLMRuntimeFixture("logic_db");
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeFixture.runtimeDatabases();
    }
    
    @Test
    void assertMinimalBaseline() throws IOException {
        assertAdvisoryUsabilitySuite("minimal-usability-mysql",
                () -> new LLMUsabilityScenarioCatalog().createMinimalBaseline("mysql", "logic_db", runtimeFixture.schemaName(),
                        "orders", COUNT_ORDERS_SQL, runtimeFixture.totalOrders()));
    }
}
