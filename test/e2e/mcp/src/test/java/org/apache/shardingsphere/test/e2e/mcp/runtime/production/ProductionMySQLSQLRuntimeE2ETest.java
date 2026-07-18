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

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionMySQLSQLRuntimeE2ETest extends AbstractProductionMySQLRuntimeE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteUpdateWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            assertThat(String.valueOf(actual.get("result_kind")), is("update_count"));
            assertThat(String.valueOf(actual.get("affected_rows")), is("1"));
            assertThat(MySQLRuntimeTestSupport.querySingleString(getContainer(), String.format("SELECT status FROM %s.orders WHERE order_id = 1", getPhysicalSchemaName())), is("PENDING"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteUpdateWithoutApprovalArgumentWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = status WHERE order_id = -1"));
            assertThat(String.valueOf(actual.get("result_kind")), is("update_count"));
            assertThat(String.valueOf(actual.get("affected_rows")), is("0"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteRollbackWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> beginResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("ROLLBACK"));
            assertThat(String.valueOf(beginResponse.get("summary")), is("Transaction started."));
            assertThat(String.valueOf(rollbackResponse.get("summary")), is("Transaction rolled back."));
            assertThat(MySQLRuntimeTestSupport.querySingleString(getContainer(), String.format("SELECT status FROM %s.orders WHERE order_id = 1", getPhysicalSchemaName())), is("NEW"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectBlankSavepointNameWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("SAVEPOINT"));
            assertRecoveryResponse(actual, "Savepoint name is required.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteSavepointFlowWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> savepointResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("SAVEPOINT sp_1"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("ROLLBACK TO SAVEPOINT sp_1"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("summary")), is("Savepoint created."));
            assertThat(String.valueOf(rollbackResponse.get("summary")), is("Savepoint rolled back."));
            assertThat(String.valueOf(commitResponse.get("summary")), is("Transaction committed."));
            assertThat(MySQLRuntimeTestSupport.querySingleString(getContainer(), String.format("SELECT status FROM %s.orders WHERE order_id = 1", getPhysicalSchemaName())), is("PENDING"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteReleaseSavepointWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            Map<String, Object> savepointResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("SAVEPOINT sp_release"));
            Map<String, Object> releaseResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("RELEASE SAVEPOINT sp_release"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("summary")), is("Savepoint created."));
            assertThat(String.valueOf(releaseResponse.get("summary")), is("Savepoint released."));
            assertThat(String.valueOf(commitResponse.get("summary")), is("Transaction committed."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertExecuteDdlRefreshesMetadataWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> executeResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            Map<String, Object> actualItem = MCPPayloadAssertions.getSingleItem(interactionClient.readResource(
                    "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders_archive"));
            assertThat(String.valueOf(executeResponse.get("summary")), is("Statement executed."));
            assertThat(String.valueOf(actualItem.get("table")), is("orders_archive"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertCloseRollsBackPendingTransactionWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            interactionClient.close();
            assertThat(MySQLRuntimeTestSupport.querySingleString(getContainer(), String.format("SELECT status FROM %s.orders WHERE order_id = 1", getPhysicalSchemaName())), is("NEW"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertListDatabasesWithMultipleRuntimeDatabasesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
            List<String> actualDatabaseNames = getPayloadItems(interactionClient.readResource("shardingsphere://databases")).stream()
                    .map(each -> String.valueOf(each.get("database"))).toList();
            assertThat(actualDatabaseNames, hasItems(LOGICAL_DATABASE_NAME, "analytics_db", "warehouse"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRefreshMetadataVisibleForTargetDatabaseOnlyWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient firstInteractionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
            firstInteractionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments(LOGICAL_DATABASE_NAME, "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            List<String> firstSessionTableNames = readTableNames(firstInteractionClient, LOGICAL_DATABASE_NAME);
            try (MCPInteractionClient secondInteractionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
                List<String> secondSessionTableNames = readTableNames(secondInteractionClient, LOGICAL_DATABASE_NAME);
                List<String> analyticsDatabaseTableNames = readTableNames(secondInteractionClient, "analytics_db");
                assertTrue(firstSessionTableNames.contains("orders_archive"));
                assertTrue(secondSessionTableNames.contains("orders_archive"));
                assertFalse(analyticsDatabaseTableNames.contains("orders_archive"));
                assertTrue(analyticsDatabaseTableNames.contains("metrics"));
            }
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectCrossDatabaseTransactionSwitchWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments(LOGICAL_DATABASE_NAME, "BEGIN"));
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "analytics_db", "schema", "analytics_db", "sql", "SELECT metric_name FROM metrics ORDER BY metric_id"));
            assertRecoveryResponse(actual, "Cross-database transaction switching is not supported.");
        }
    }
}
