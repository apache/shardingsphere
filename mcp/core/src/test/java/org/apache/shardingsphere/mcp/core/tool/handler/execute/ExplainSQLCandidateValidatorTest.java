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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExplainSQLCandidateValidatorTest {
    
    private final MCPDatabaseCapability databaseCapability = createCapability();
    
    private final ExplainSQLCandidateValidator validator = new ExplainSQLCandidateValidator(new MCPStatementAnalyzer());
    
    @Test
    void assertValidate() {
        ClassificationResult actual = validator.validate("SELECT * FROM logic_db.foo_orders", "EXPLAIN FORMAT=JSON SELECT * FROM logic_db.foo_orders", databaseCapability);
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.EXPLAIN));
        assertThat(actual.getStatementType(), is("EXPLAIN"));
        assertThat(actual.getNormalizedSql(), is("EXPLAIN FORMAT=JSON SELECT * FROM logic_db.foo_orders"));
        assertThat(actual.getReferencedObjectNames(), contains("logic_db.foo_orders"));
    }
    
    @Test
    void assertValidateWithSignificantWhitespace() {
        ClassificationResult actual = validator.validate("SELECT * FROM foo_orders WHERE status = 'READY  TO SHIP'",
                "EXPLAIN QUERY TREE SELECT * FROM foo_orders WHERE status = 'READY  TO SHIP'", databaseCapability);
        assertThat(actual.getNormalizedSql(), is("EXPLAIN QUERY TREE SELECT * FROM foo_orders WHERE status = 'READY  TO SHIP'"));
    }
    
    @Test
    void assertValidateHiveCBO() {
        ClassificationResult actual = validator.validate("SELECT * FROM foo_orders", "EXPLAIN CBO SELECT * FROM foo_orders", databaseCapability);
        assertThat(actual.getNormalizedSql(), is("EXPLAIN CBO SELECT * FROM foo_orders"));
    }
    
    @Test
    void assertValidateWithNonExecutableComments() {
        ClassificationResult actual = validator.validate("SELECT '/*!80018 ANALYZE */' FROM foo_orders",
                "EXPLAIN /* plan only */ SELECT '/*!80018 ANALYZE */' FROM foo_orders", databaseCapability);
        assertThat(actual.getNormalizedSql(), is("EXPLAIN /* plan only */ SELECT '/*!80018 ANALYZE */' FROM foo_orders"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertValidateWithInvalidCandidateCases")
    void assertValidateWithInvalidCandidate(final String name, final String sql, final String explainSql, final String expectedMessage) {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> validator.validate(sql, explainSql, databaseCapability));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Stream<Arguments> assertValidateWithInvalidCandidateCases() {
        return Stream.of(
                Arguments.of("mutating explained sql", "UPDATE foo_orders SET status = 'DONE'", "EXPLAIN UPDATE foo_orders SET status = 'DONE'",
                        "database_gateway_execute_explain_query only supports QUERY statements as the explained SQL."),
                Arguments.of("missing explain prefix", "SELECT * FROM foo_orders", "SELECT * FROM foo_orders", "explain_sql must start with EXPLAIN."),
                Arguments.of("explain analyze", "SELECT * FROM foo_orders", "EXPLAIN ANALYZE SELECT * FROM foo_orders",
                        "EXPLAIN ANALYZE is not supported by the MCP explain query tool."),
                Arguments.of("explain analyse", "SELECT * FROM foo_orders", "EXPLAIN ANALYSE SELECT * FROM foo_orders",
                        "EXPLAIN ANALYZE is not supported by the MCP explain query tool."),
                Arguments.of("mysql executable comment", "SELECT * FROM foo_orders", "EXPLAIN /*!80018 ANALYZE */ SELECT * FROM foo_orders",
                        "Executable comments are not supported by the MCP explain query tool."),
                Arguments.of("mariadb executable comment", "SELECT * FROM foo_orders", "EXPLAIN /*M!100000 ANALYZE */ SELECT * FROM foo_orders",
                        "Executable comments are not supported by the MCP explain query tool."),
                Arguments.of("explain plan for", "SELECT * FROM foo_orders", "EXPLAIN PLAN FOR SELECT * FROM foo_orders",
                        "EXPLAIN PLAN FOR workflows are not supported by the MCP explain query tool."),
                Arguments.of("rewritten sql", "SELECT * FROM foo_orders", "EXPLAIN SELECT order_id FROM foo_orders",
                        "explain_sql must include the original sql argument without rewriting it."),
                Arguments.of("quoted identifier case rewrite", "SELECT * FROM \"Foo_Orders\"", "EXPLAIN SELECT * FROM \"foo_orders\"",
                        "explain_sql must include the original sql argument without rewriting it."),
                Arguments.of("literal whitespace rewrite", "SELECT * FROM foo_orders WHERE status = 'READY  TO SHIP'",
                        "EXPLAIN SELECT * FROM foo_orders WHERE status = 'READY TO SHIP'",
                        "explain_sql must include the original sql argument without rewriting it."),
                Arguments.of("quoted identifier whitespace rewrite", "SELECT * FROM \"foo  orders\"", "EXPLAIN SELECT * FROM \"foo orders\"",
                        "explain_sql must include the original sql argument without rewriting it."),
                Arguments.of("create table wrapper", "SELECT * FROM foo_orders", "EXPLAIN CREATE TABLE archived_orders AS SELECT * FROM foo_orders",
                        "explain_sql must not wrap the original sql argument in another statement."),
                Arguments.of("nested explain wrapper", "SELECT * FROM foo_orders", "EXPLAIN EXPLAIN SELECT * FROM foo_orders",
                        "explain_sql must not wrap the original sql argument in another statement."),
                Arguments.of("prepare wrapper", "SELECT * FROM foo_orders", "EXPLAIN PREPARE archived_orders FROM SELECT * FROM foo_orders",
                        "explain_sql must not wrap the original sql argument in another statement."),
                Arguments.of("output redirection", "SELECT * FROM foo_orders", "EXPLAIN INTO OUTFILE SELECT * FROM foo_orders",
                        "EXPLAIN output redirection is not supported by the MCP explain query tool."));
    }
    
    private MCPDatabaseCapability createCapability() {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.getDatabaseType()).thenReturn("MySQL");
        return result;
    }
}
