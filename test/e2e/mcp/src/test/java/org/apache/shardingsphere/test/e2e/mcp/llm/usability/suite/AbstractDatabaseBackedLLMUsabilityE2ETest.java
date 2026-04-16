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

import org.junit.jupiter.api.Test;

import java.io.IOException;

abstract class AbstractDatabaseBackedLLMUsabilityE2ETest extends AbstractLLMUsabilityE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    @Test
    void assertMinimalBaseline() throws IOException {
        assertUsabilitySuite(getSuiteId(),
                () -> new LLMUsabilityScenarioCatalog().createMinimalBaseline(getRuntimeKind(), "logic_db", getRuntimeSchemaName(), "orders", COUNT_ORDERS_SQL, getRuntimeTotalOrders()));
    }
    
    @Override
    protected final RuntimeFixture createH2RuntimeFixture() throws IOException {
        return createMultiDatabaseH2RuntimeFixture("logic_db", "analytics_db");
    }
    
    @Override
    protected final RuntimeFixture createMySQLRuntimeFixture() throws IOException {
        return createMySQLDatabaseRuntimeFixture("logic_db", "Docker is required for the MySQL-backed usability suite.");
    }
    
    protected abstract String getSuiteId();
}
