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

package org.apache.shardingsphere.mcp.support.database.exception.dialect;

import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class MySQLMCPDialectSQLExceptionClassifierTest {
    
    private final MySQLMCPDialectSQLExceptionClassifier classifier = new MySQLMCPDialectSQLExceptionClassifier();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final int errorCode, final MCPJDBCErrorCategory expected) {
        assertThat(classifier.classify(new SQLException("error", "42000", errorCode)), is(Optional.of(expected)));
    }
    
    @Test
    void assertClassifyUnrelatedSQLState() {
        assertThat(classifier.classify(new SQLException("missing table", "42S02", 1146)), is(Optional.empty()));
    }
    
    @Test
    void assertTypes() {
        assertThat(classifier.getType(), is("MySQL"));
        assertThat(classifier.getTypeAliases(), contains("MariaDB"));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("database access denied", 1044, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("unknown database", 1049, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("parse error", 1064, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("table access denied", 1142, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("column access denied", 1143, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("syntax error", 1149, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("specific access denied", 1227, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("missing routine", 1305, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("routine access denied", 1370, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("ambiguous error", 1055, MCPJDBCErrorCategory.QUERY_FAILED));
    }
}
