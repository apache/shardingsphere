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

package org.apache.shardingsphere.test.e2e.mcp;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityBuilder;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SupportedDatabaseContractMatrixE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCapabilityMatrixCases")
    void assertCapabilityMatrix(final String name, final String databaseType, final boolean expectedTransactionControl,
                                final boolean expectedSavepoint, final boolean expectedIndexSupport) {
        DatabaseCapabilityBuilder assembler = new DatabaseCapabilityBuilder(
                new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot(databaseType, "", Collections.emptyList()))));
        DatabaseCapability actual = assembler.assembleDatabaseCapability("logic_db").get();
        assertThat(actual.isSupportsTransactionControl(), is(expectedTransactionControl));
        assertThat(actual.isSupportsSavepoint(), is(expectedSavepoint));
        assertThat(actual.getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX), is(expectedIndexSupport));
    }
    
    static Stream<Arguments> assertCapabilityMatrixCases() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", true, true, true),
                Arguments.of("postgresql", "PostgreSQL", true, true, true),
                Arguments.of("open gauss", "openGauss", true, true, true),
                Arguments.of("sql server", "SQLServer", true, true, true),
                Arguments.of("mariadb", "MariaDB", true, true, true),
                Arguments.of("oracle", "Oracle", true, true, true),
                Arguments.of("clickhouse", "ClickHouse", false, false, false),
                Arguments.of("doris", "Doris", true, false, true),
                Arguments.of("hive", "Hive", false, false, false),
                Arguments.of("presto", "Presto", true, false, false),
                Arguments.of("firebird", "Firebird", true, true, true),
                Arguments.of("h2", "H2", true, true, true));
    }
}
