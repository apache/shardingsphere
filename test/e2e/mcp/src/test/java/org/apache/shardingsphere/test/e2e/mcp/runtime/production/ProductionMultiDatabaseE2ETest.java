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

import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionMultiDatabaseE2ETest extends AbstractTransportParameterizedProductionRuntimeE2ETest {
    
    private static final String LOGIC_DATABASE_NAME = "logic_db";
    
    private static final String ANALYTICS_DATABASE_NAME = "analytics_db";
    
    private static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
    
    private String firstJdbcUrl;
    
    private String secondJdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-multi-first", getTransport().getH2AccessMode());
            secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-multi-second", getTransport().getH2AccessMode());
            H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
            H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return createRuntimeDatabases("H2", "H2");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListDatabasesWithMultipleRuntimeDatabases(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource("shardingsphere://databases"));
            assertThat(items.stream().map(each -> String.valueOf(each.get("database"))).toList(), hasItems(LOGIC_DATABASE_NAME, ANALYTICS_DATABASE_NAME));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRefreshMetadataVisibleForSubsequentClientSessionsInTargetDatabaseOnly(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectCrossDatabaseTransactionSwitch(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query",
                    Map.of("database", LOGIC_DATABASE_NAME, "schema", "public", "sql", "BEGIN"));
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", ANALYTICS_DATABASE_NAME, "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id"));
            assertThat(String.valueOf(actual.get("error_code")), is("transaction_state_error"));
            assertFalse(Boolean.parseBoolean(String.valueOf(actual.get("ok"))));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectMismatchedDatabaseType(final String name, final RuntimeTransport transport) {
        useTransport(transport);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> openAndCloseInteractionClient("MySQL", "H2"));
        assertThat(actual.getMessage(), is("Configured databaseType `MySQL` does not match actual database type `H2` for database `logic_db`."));
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final String firstDatabaseType, final String secondDatabaseType) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put(LOGIC_DATABASE_NAME, createRuntimeDatabaseConfiguration(firstDatabaseType, firstJdbcUrl));
        result.put(ANALYTICS_DATABASE_NAME, createRuntimeDatabaseConfiguration(secondDatabaseType, secondJdbcUrl));
        return result;
    }
    
    private List<String> readTableNames(final MCPInteractionClient interactionClient, final String databaseName) throws IOException, InterruptedException {
        return getPayloadItems(interactionClient.readResource(String.format("shardingsphere://databases/%s/schemas/public/tables", databaseName)))
                .stream().map(each -> String.valueOf(each.get("table"))).toList();
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseType, final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", H2_DRIVER_CLASS_NAME);
    }
    
    private void openAndCloseInteractionClient(final String firstDatabaseType, final String secondDatabaseType) {
        ensureRuntimeDatabasesPrepared();
        try {
            MCPInteractionClient interactionClient = createOpenedInteractionClient(createRuntimeDatabases(firstDatabaseType, secondDatabaseType));
            interactionClient.close();
        } catch (final IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private void ensureRuntimeDatabasesPrepared() {
        if (null != firstJdbcUrl && null != secondJdbcUrl) {
            return;
        }
        try {
            MCPInteractionClient interactionClient = createOpenedInteractionClient();
            interactionClient.close();
        } catch (final IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isProductionH2Enabled() || MCPE2ECondition.isProductionStdioEnabled();
    }
    
    private static Stream<Arguments> transports() {
        Stream.Builder<Arguments> result = Stream.builder();
        if (MCPE2ECondition.isProductionH2Enabled()) {
            result.add(Arguments.of("http", RuntimeTransport.HTTP));
        }
        if (MCPE2ECondition.isProductionStdioEnabled()) {
            result.add(Arguments.of("stdio", RuntimeTransport.STDIO));
        }
        return result.build();
    }
}
