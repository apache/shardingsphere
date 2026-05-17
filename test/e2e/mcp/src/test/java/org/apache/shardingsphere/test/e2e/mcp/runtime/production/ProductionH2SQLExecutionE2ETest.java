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

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class ProductionH2SQLExecutionE2ETest extends AbstractProductionH2RuntimeE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteSelect(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteSelectWithTruncation(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(((List<?>) actual.get("rows")).size(), is(1));
            assertThat(String.valueOf(actual.get("truncated")), is("true"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteUpdate(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> payload = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            assertThat(String.valueOf(payload.get("result_kind")), is("update_count"));
            assertThat(String.valueOf(payload.get("affected_rows")), is("1"));
            assertThat(H2RuntimeTestSupport.querySingleString(getJdbcUrl(), "SELECT status FROM public.orders WHERE order_id = 1"), is("PENDING"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectUnapprovedExecuteUpdate(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1", "execution_mode", "execute"));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("database_gateway_execute_update approved_by_user=true is required for real side effects."));
            assertThat(String.valueOf(((Map<?, ?>) actual.get("recovery")).get("category")), is("approval_required"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteExplainAnalyze(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "EXPLAIN ANALYZE SELECT * FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(String.valueOf(actual.get("statement_type")), is("EXPLAIN ANALYZE"));
            assertFalse(((List<?>) actual.get("rows")).isEmpty());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteQueryTimeout(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql",
                            "SELECT COUNT(*) FROM SYSTEM_RANGE(1, 200000) a CROSS JOIN SYSTEM_RANGE(1, 200000) b", "timeout_ms", 1));
            assertThat(String.valueOf(actual.get("error_code")), is("timeout"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectExecuteMultiStatement(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1; SELECT 2"));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("Only one SQL statement is allowed."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectBlankSavepointName(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("SAVEPOINT"));
            assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
            assertThat(String.valueOf(actual.get("message")), is("Savepoint name is required."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteSavepointFlow(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> savepointResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("SAVEPOINT sp_1"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("ROLLBACK TO SAVEPOINT sp_1"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("message")), is("Savepoint created."));
            assertThat(String.valueOf(rollbackResponse.get("message")), is("Savepoint rolled back."));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
            assertThat(H2RuntimeTestSupport.querySingleString(getJdbcUrl(), "SELECT status FROM public.orders WHERE order_id = 1"), is("PENDING"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteReleaseSavepoint(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            Map<String, Object> savepointResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("SAVEPOINT sp_release"));
            Map<String, Object> releaseResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("RELEASE SAVEPOINT sp_release"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("message")), is("Savepoint created."));
            assertThat(String.valueOf(releaseResponse.get("message")), is("Savepoint released."));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteTransactionalDdlRefreshesMetadataOnCommit(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("COMMIT"));
            Map<String, Object> actualItem = MCPPayloadAssertions.getSingleItem(interactionClient.readResource(
                    "shardingsphere://databases/logic_db/schemas/public/tables/orders_archive"));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
            assertThat(String.valueOf(actualItem.get("table")), is("orders_archive"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertCloseRollsBackPendingTransaction(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            interactionClient.close();
            assertThat(H2RuntimeTestSupport.querySingleString(getJdbcUrl(), "SELECT status FROM public.orders WHERE order_id = 1"), is("NEW"));
        }
    }
}
