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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractProductionMultiDatabaseE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private static final String LOGIC_DATABASE_NAME = "logic_db";
    
    private static final String ANALYTICS_DATABASE_NAME = "analytics_db";
    
    private static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
    
    private String firstJdbcUrl;
    
    private String secondJdbcUrl;
    
    private String firstDatabaseType = "H2";
    
    private String secondDatabaseType = "H2";
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-multi-first", getTransport());
            secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-multi-second", getTransport());
            H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
            H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>();
        result.put(LOGIC_DATABASE_NAME, createRuntimeDatabaseConfiguration(firstDatabaseType, firstJdbcUrl));
        result.put(ANALYTICS_DATABASE_NAME, createRuntimeDatabaseConfiguration(secondDatabaseType, secondJdbcUrl));
        return result;
    }
    
    @Test
    void assertListDatabasesWithMultipleRuntimeDatabases() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases").getStructuredContent());
            assertThat(items.stream().map(each -> String.valueOf(each.get("database"))).toList(), hasItems(LOGIC_DATABASE_NAME, ANALYTICS_DATABASE_NAME));
        }
    }
    
    @Test
    void assertRefreshMetadataVisibleForSubsequentClientSessionsInTargetDatabaseOnly() throws IOException, InterruptedException {
        try (MCPInteractionClient firstInteractionClient = createOpenedInteractionClient()) {
            firstInteractionClient.call("execute_query",
                    Map.of("database", LOGIC_DATABASE_NAME, "schema", "public", "sql", "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            List<String> firstSessionTableNames = readTableNames(firstInteractionClient, LOGIC_DATABASE_NAME);
            try (MCPInteractionClient secondInteractionClient = createOpenedInteractionClient()) {
                List<String> secondSessionTableNames = readTableNames(secondInteractionClient, LOGIC_DATABASE_NAME);
                List<String> analyticsDatabaseTableNames = readTableNames(secondInteractionClient, ANALYTICS_DATABASE_NAME);
                assertTrue(firstSessionTableNames.contains("orders_archive"));
                assertTrue(secondSessionTableNames.contains("orders_archive"));
                assertFalse(analyticsDatabaseTableNames.contains("orders_archive"));
                assertTrue(analyticsDatabaseTableNames.contains("orders"));
            }
        }
    }
    
    @Test
    void assertRejectCrossDatabaseTransactionSwitch() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query",
                    Map.of("database", LOGIC_DATABASE_NAME, "schema", "public", "sql", "BEGIN"));
            Map<String, Object> actual = getStructuredContent(interactionClient.call("execute_query",
                    Map.of("database", ANALYTICS_DATABASE_NAME, "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id")));
            assertThat(String.valueOf(actual.get("error_code")), is("transaction_state_error"));
            assertFalse(Boolean.parseBoolean(String.valueOf(actual.get("ok"))));
        }
    }
    
    @Test
    void assertRejectMismatchedDatabaseType() {
        firstDatabaseType = "MySQL";
        IllegalStateException actual = assertThrows(IllegalStateException.class, this::openAndCloseInteractionClient);
        assertThat(actual.getMessage(), containsString("Configured databaseType `MySQL` does not match actual database type `H2` for database `logic_db`."));
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseType, final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", H2_DRIVER_CLASS_NAME);
    }
    
    private List<String> readTableNames(final MCPInteractionClient interactionClient, final String databaseName) throws IOException, InterruptedException {
        return getPayloadItems(interactionClient.readResource(String.format("shardingsphere://databases/%s/schemas/public/tables", databaseName)).getStructuredContent())
                .stream().map(each -> String.valueOf(each.get("table"))).toList();
    }
    
    private void openAndCloseInteractionClient() {
        try {
            createOpenedInteractionClient().close();
        } catch (final IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
