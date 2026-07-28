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

class SQLServerMCPDialectSQLExceptionClassifierTest {
    
    private final SQLServerMCPDialectSQLExceptionClassifier classifier = new SQLServerMCPDialectSQLExceptionClassifier();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final int errorCode, final MCPJDBCErrorCategory expected) {
        assertThat(classifier.classify(new SQLException("error", "S0001", errorCode)), is(Optional.of(expected)));
    }
    
    @Test
    void assertClassifyUnknownError() {
        assertThat(classifier.classify(new SQLException("error", "S0001", 999)), is(Optional.empty()));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("incorrect syntax", 102, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("incorrect syntax near keyword", 156, MCPJDBCErrorCategory.SYNTAX),
                Arguments.of("invalid column name", 207, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("invalid object name", 208, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("permission denied", 229, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("database access denied", 916, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("stored procedure not found", 2812, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("cannot open database", 4060, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("login untrusted domain", 18452, MCPJDBCErrorCategory.AUTHENTICATION),
                Arguments.of("login failed", 18456, MCPJDBCErrorCategory.AUTHENTICATION));
    }
}
