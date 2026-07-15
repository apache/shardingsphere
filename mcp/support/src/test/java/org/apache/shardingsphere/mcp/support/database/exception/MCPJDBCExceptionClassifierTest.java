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

package org.apache.shardingsphere.mcp.support.database.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPJDBCExceptionClassifierTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final SQLException cause, final MCPJDBCErrorCategory expected) {
        assertThat(MCPJDBCExceptionClassifier.classify(cause), is(expected));
    }
    
    @Test
    void assertClassifyCauseChain() {
        assertThat(MCPJDBCExceptionClassifier.classify(new IllegalStateException(new SQLException("missing table", "42P01"))),
                is(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE));
    }
    
    @Test
    void assertClassifyNextExceptionChain() {
        SQLException cause = new SQLException("batch failed");
        cause.setNextException(new SQLException("syntax error", "42601"));
        assertThat(MCPJDBCExceptionClassifier.classify(cause), is(MCPJDBCErrorCategory.SYNTAX));
    }
    
    @Test
    void assertClassifySuppressedException() {
        SQLException cause = new SQLException("query failed");
        cause.addSuppressed(new SQLTimeoutException("timeout"));
        assertThat(MCPJDBCExceptionClassifier.classify(cause), is(MCPJDBCErrorCategory.TIMEOUT));
    }
    
    @Test
    void assertClassifyClassifiedQueryFailure() {
        MCPDatabaseQueryFailedException cause = new MCPDatabaseQueryFailedException(
                MCPJDBCErrorCategory.AUTHORIZATION, new SQLSyntaxErrorException("access denied", "42000", 1044));
        assertThat(MCPJDBCExceptionClassifier.classify(cause), is(MCPJDBCErrorCategory.AUTHORIZATION));
    }
    
    @Test
    void assertClassifyDatabaseSyntaxFailure() {
        assertThat(MCPJDBCExceptionClassifier.classify(new MCPDatabaseSQLSyntaxException(new SQLException("syntax error", "42000", 1064))),
                is(MCPJDBCErrorCategory.SYNTAX));
    }
    
    @Test
    void assertClassifyUnambiguousJDBCTypeBeforeDialect() {
        assertThat(MCPJDBCExceptionClassifier.classify("MySQL", new SQLTimeoutException("timeout", "42000", 1064)), is(MCPJDBCErrorCategory.TIMEOUT));
    }
    
    @Test
    void assertClassifyMariaDBAlias() {
        assertThat(MCPJDBCExceptionClassifier.classify("MariaDB", new SQLException("syntax error", "42000", 1064)), is(MCPJDBCErrorCategory.SYNTAX));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("timeout", new SQLTimeoutException(), MCPJDBCErrorCategory.TIMEOUT),
                Arguments.of("feature not supported", new SQLFeatureNotSupportedException(), MCPJDBCErrorCategory.FEATURE_NOT_SUPPORTED),
                Arguments.of("connection subtype", new SQLNonTransientConnectionException(), MCPJDBCErrorCategory.CONNECTION),
                Arguments.of("connection SQLState", new SQLException("", "08006"), MCPJDBCErrorCategory.CONNECTION),
                Arguments.of("authentication", new SQLException("", "28000"), MCPJDBCErrorCategory.AUTHENTICATION),
                Arguments.of("authorization", new SQLException("", "42501"), MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("object not visible", new SQLSyntaxErrorException("", "42P01"), MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("syntax subtype", new SQLSyntaxErrorException(), MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("syntax SQLState", new SQLException("", "42601"), MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("ambiguous class 42", new SQLException("", "42000"), MCPJDBCErrorCategory.QUERY_FAILED),
                Arguments.of("unknown", new SQLException(), MCPJDBCErrorCategory.QUERY_FAILED));
    }
}
