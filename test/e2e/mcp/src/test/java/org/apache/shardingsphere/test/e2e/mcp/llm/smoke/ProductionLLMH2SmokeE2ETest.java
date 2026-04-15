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

package org.apache.shardingsphere.test.e2e.mcp.llm.smoke;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class ProductionLLMH2SmokeE2ETest extends AbstractLLMSmokeE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private H2RuntimeTestSupport.LLMH2RuntimeFixture runtimeFixture;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            runtimeFixture = H2RuntimeTestSupport.createLLMRuntimeFixture(getTempDir(), "production-llm-h2-smoke", "logic_db");
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeFixture.runtimeDatabases();
    }
    
    @Test
    void assertSmoke() throws IOException {
        assertLLMSmoke(() -> createMinimalSmokeScenario("minimal-smoke-h2", "logic_db", "public", "orders", COUNT_ORDERS_SQL, runtimeFixture.totalOrders()));
    }
}
