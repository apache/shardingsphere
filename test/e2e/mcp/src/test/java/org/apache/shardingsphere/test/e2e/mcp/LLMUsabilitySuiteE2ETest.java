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

import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class LLMUsabilitySuiteE2ETest extends AbstractLLMUsabilityE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final String COUNT_ORDERS_JDBC_SQL = "SELECT COUNT(*) AS total_orders FROM public.orders";
    
    private String jdbcUrl;
    
    private int totalOrders;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "llm-usability-h2");
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
            totalOrders = H2RuntimeTestSupport.querySingleInt(jdbcUrl, COUNT_ORDERS_JDBC_SQL);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    @Test
    void assertMinimalBaseline() throws IOException, InterruptedException {
        assertAdvisoryUsabilitySuite("minimal-usability-h2",
                new LLMUsabilityScenarioCatalog().createMinimalBaseline("h2", "logic_db", "public", "orders", COUNT_ORDERS_SQL, totalOrders));
    }
}
