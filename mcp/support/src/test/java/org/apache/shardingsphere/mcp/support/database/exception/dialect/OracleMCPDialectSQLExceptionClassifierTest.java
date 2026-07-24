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

class OracleMCPDialectSQLExceptionClassifierTest {
    
    private final OracleMCPDialectSQLExceptionClassifier classifier = new OracleMCPDialectSQLExceptionClassifier();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final int errorCode, final MCPJDBCErrorCategory expected) {
        assertThat(classifier.classify(new SQLException("error", "42000", errorCode)), is(Optional.of(expected)));
    }
    
    @Test
    void assertClassifyUnknownError() {
        assertThat(classifier.classify(new SQLException("error", "42000", 999)), is(Optional.empty()));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("invalid identifier", 904, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("table or view does not exist", 942, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE),
                Arguments.of("invalid username or password", 1017, MCPJDBCErrorCategory.AUTHENTICATION),
                Arguments.of("insufficient privileges", 1031, MCPJDBCErrorCategory.AUTHORIZATION),
                Arguments.of("object does not exist", 4043, MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE));
    }
}
