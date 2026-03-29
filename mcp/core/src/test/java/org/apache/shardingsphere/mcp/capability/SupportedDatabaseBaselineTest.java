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

package org.apache.shardingsphere.mcp.capability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportedDatabaseBaselineTest {
    
    @Test
    void assertCreateDefault() {
        Set<String> expectedDatabaseTypes = createDatabaseTypes("MySQL", "PostgreSQL", "openGauss", "SQLServer", "MariaDB", "Oracle", "ClickHouse", "Doris", "Hive", "Presto", "Firebird", "H2");
        Set<String> actualDatabaseTypes = createDatabaseTypes(
                DatabaseCapabilityCatalog.find("logic_db", "MYSQL", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "POSTGRESQL", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "OPENGAUSS", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "SQLSERVER", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "MARIADB", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "ORACLE", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "CLICKHOUSE", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "DORIS", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "HIVE", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "PRESTO", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "FIREBIRD", "").orElseThrow().getDatabaseType(),
                DatabaseCapabilityCatalog.find("logic_db", "H2", "").orElseThrow().getDatabaseType());
        assertThat(actualDatabaseTypes.size(), is(12));
        assertThat(actualDatabaseTypes, is(expectedDatabaseTypes));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transactionMatrixCases")
    void assertTransactionMatrix(final String caseName, final String databaseType, final boolean expectedSupportsTransactionControl,
                                 final boolean expectedSupportsSavepoint, final Set<String> expectedTransactionStatements) {
        Optional<DatabaseCapability> actualCapability = DatabaseCapabilityCatalog.find("logic_db", databaseType, "");
        
        assertTrue(actualCapability.isPresent(), caseName);
        assertThat(actualCapability.get().isSupportsTransactionControl(), is(expectedSupportsTransactionControl));
        assertThat(actualCapability.get().isSupportsSavepoint(), is(expectedSupportsSavepoint));
        assertThat(actualCapability.get().getSupportedTransactionStatements(), is(expectedTransactionStatements));
    }
    
    private static Stream<Arguments> transactionMatrixCases() {
        return Stream.of(
                Arguments.of("MYSQL", "MYSQL", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("POSTGRESQL", "POSTGRESQL", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("OPENGAUSS", "OPENGAUSS", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("SQLSERVER", "SQLSERVER", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("MARIADB", "MARIADB", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("ORACLE", "ORACLE", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("CLICKHOUSE", "CLICKHOUSE", false, false, createTransactionStatements()),
                Arguments.of("DORIS", "DORIS", true, false, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK")),
                Arguments.of("HIVE", "HIVE", false, false, createTransactionStatements()),
                Arguments.of("PRESTO", "PRESTO", true, false, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK")),
                Arguments.of("FIREBIRD", "FIREBIRD", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")),
                Arguments.of("H2", "H2", true, true, createTransactionStatements("BEGIN", "START TRANSACTION", "COMMIT", "ROLLBACK", "SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT", "RELEASE SAVEPOINT")));
    }
    
    private static Set<String> createTransactionStatements(final String... statements) {
        return new LinkedHashSet<>(Arrays.asList(statements));
    }
    
    private static Set<String> createDatabaseTypes(final String... databaseTypes) {
        return new LinkedHashSet<>(Arrays.asList(databaseTypes));
    }
}
