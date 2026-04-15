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
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

class HttpLLMUsabilityH2SuiteE2ETest extends AbstractLLMUsabilityE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private H2RuntimeTestSupport.LLMH2RuntimeFixture runtimeFixture;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            runtimeFixture = H2RuntimeTestSupport.createLLMRuntimeFixture(getTempDir(), "llm-usability-h2", "logic_db", getTransport());
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeFixture.runtimeDatabases();
    }
    
    @Override
    protected RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Test
    void assertMinimalBaseline() throws IOException {
        assertAdvisoryUsabilitySuite("minimal-usability-h2",
                () -> new LLMUsabilityScenarioCatalog().createMinimalBaseline("h2", "logic_db", "public", "orders", COUNT_ORDERS_SQL, runtimeFixture.totalOrders()));
    }
}
