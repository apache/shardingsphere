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

package org.apache.shardingsphere.mcp.execute;

import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.StatementClass;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecuteQueryFacadeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final String sql, final StatementClass expectedStatementClass, final String expectedStatementType,
                        final String expectedTargetObjectName, final String expectedSavepointName) {
        StatementClassifier classifier = new StatementClassifier();
        
        ClassificationResult actual = classifier.classify(sql);
        
        assertThat(actual.getStatementClass(), is(expectedStatementClass));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getTargetObjectName().orElse(""), is(expectedTargetObjectName));
        assertThat(actual.getSavepointName().orElse(""), is(expectedSavepointName));
    }
    
    static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("query", "SELECT * FROM orders", StatementClass.QUERY, "QUERY", "orders", ""),
                Arguments.of("dml", "UPDATE orders SET status = 'DONE'", StatementClass.DML, "UPDATE", "orders", ""),
                Arguments.of("ddl", "CREATE TABLE orders", StatementClass.DDL, "CREATE", "orders", ""),
                Arguments.of("dcl", "GRANT SELECT ON orders TO app_user", StatementClass.DCL, "GRANT", "", ""),
                Arguments.of("transaction", "BEGIN", StatementClass.TRANSACTION_CONTROL, "BEGIN", "", ""),
                Arguments.of("savepoint", "SAVEPOINT sp_1", StatementClass.SAVEPOINT, "SAVEPOINT", "", "sp_1"),
                Arguments.of("explain analyze", "EXPLAIN ANALYZE SELECT * FROM orders", StatementClass.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", "orders", ""));
    }
    
    @Test
    void assertClassifyWithBannedCommand() {
        StatementClassifier classifier = new StatementClassifier();
        
        UnsupportedOperationException actual = assertThrows(UnsupportedOperationException.class, () -> classifier.classify("SET search_path public"));
        
        assertThat(actual.getMessage(), is("Statement is banned by the MCP contract."));
    }
    
    @Test
    void assertClassifyWithMultipleStatements() {
        StatementClassifier classifier = new StatementClassifier();
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> classifier.classify("SELECT 1; SELECT 2"));
        
        assertThat(actual.getMessage(), is("Only one SQL statement is allowed."));
    }
    
    @Test
    void assertExecuteWithUnknownCapability() {
        ExecuteQueryFacade facade = createFacade("Unknown", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 10));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.NOT_FOUND));
    }
    
    @Test
    void assertExecuteQueryWithTruncation() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("SELECT * FROM orders", 1));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(1));
        assertTrue(actual.isTruncated());
    }
    
    @Test
    void assertExecuteUpdate() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("UPDATE orders SET status = 'DONE'", 10));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getAffectedRows(), is(3));
    }
    
    @Test
    void assertExecuteTransactionCommand() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        ExecuteQueryFacade facade = createFacade("MySQL", sessionManager, new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("BEGIN", 10));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMessage(), is("Transaction started."));
    }
    
    @Test
    void assertExecuteDdl() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("CREATE TABLE orders_archive", 10));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
    }
    
    @Test
    void assertExecuteDcl() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("GRANT SELECT ON orders TO app_user", 10));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getMessage(), is("Statement executed."));
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithUnsupportedCapability() {
        ExecuteQueryFacade facade = createFacade("MySQL", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.UNSUPPORTED));
    }
    
    @Test
    void assertExecuteExplainAnalyzeWithSupportedCapability() {
        ExecuteQueryFacade facade = createFacade("H2", new MCPSessionManager(), new AuditRecorder());
        
        ExecuteQueryResponse actual = facade.execute(createExecutionRequest("EXPLAIN ANALYZE SELECT * FROM orders", 10));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(2));
    }
    
    private ExecuteQueryFacade createFacade(final String databaseType, final MCPSessionManager sessionManager, final AuditRecorder auditRecorder) {
        DatabaseCapabilityAssembler capabilityAssembler = new DatabaseCapabilityAssembler(new MetadataCatalog(Map.of("logic_db", databaseType), Collections.emptyList()));
        return new ExecuteQueryFacade(new StatementClassifier(), capabilityAssembler,
                new TransactionCommandExecutor(capabilityAssembler, sessionManager, new DatabaseRuntime(Collections.emptyMap(), Collections.emptyMap())), auditRecorder);
    }
    
    private ExecutionRequest createExecutionRequest(final String sql, final int maxRows) {
        return new ExecutionRequest("session-1", "logic_db", "public", sql, maxRows, 1000, createRuntime());
    }
    
    private DatabaseRuntime createRuntime() {
        LinkedList<ExecuteQueryColumnDefinition> columns = new LinkedList<>();
        columns.add(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INT", false));
        columns.add(new ExecuteQueryColumnDefinition("status", "VARCHAR", "VARCHAR", true));
        LinkedList<List<Object>> rows = new LinkedList<>();
        rows.add(new LinkedList<>(List.of(1, "NEW")));
        rows.add(new LinkedList<>(List.of(2, "DONE")));
        Map<String, QueryResult> queryResults = new LinkedHashMap<>();
        queryResults.put("logic_db:orders", new QueryResult(columns, new LinkedList<>(rows)));
        Map<String, Integer> updateCounts = new LinkedHashMap<>();
        updateCounts.put("logic_db:orders", 3);
        return new DatabaseRuntime(queryResults, updateCounts);
    }
}
