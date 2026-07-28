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
import static org.hamcrest.Matchers.is;

class FirebirdMCPDialectSQLExceptionClassifierTest {
    
    private final FirebirdMCPDialectSQLExceptionClassifier classifier = new FirebirdMCPDialectSQLExceptionClassifier();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final String sqlState, final int errorCode, final MCPJDBCErrorCategory expected) {
        assertThat(classifier.classify(new SQLException("error", sqlState, errorCode)), is(Optional.of(expected)));
    }
    
    @Test
    void assertClassifyUnknownError() {
        assertThat(classifier.classify(new SQLException("error", "HY000", 999)), is(Optional.empty()));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("permission denied", "28000", 335544352, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("login", "28000", 335544472, MCPJDBCErrorCategory.AUTHENTICATION),
                Arguments.of("login error", "28000", 335545106, MCPJDBCErrorCategory.AUTHENTICATION),
                Arguments.of("column unknown", "42S22", 335544578, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("table unknown", "42S02", 335544580, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("procedure unknown", "42000", 335544581, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("token unknown", "42000", 335544634, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("ambiguous dynamic SQL error", "42000", 335544569, MCPJDBCErrorCategory.QUERY_FAILED));
    }
}
