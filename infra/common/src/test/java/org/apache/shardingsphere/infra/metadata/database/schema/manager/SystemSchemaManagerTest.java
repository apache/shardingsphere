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

package org.apache.shardingsphere.infra.metadata.database.schema.manager;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaManagerTest {
    
    private DialectSystemSchemaManager commonSystemSchemaManager;
    
    @BeforeEach
    void setUp() {
        commonSystemSchemaManager = getSystemSchemaManagerMap().get("common");
    }
    
    @AfterEach
    void tearDown() {
        getSystemSchemaManagerMap().put("common", commonSystemSchemaManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private static Map<String, DialectSystemSchemaManager> getSystemSchemaManagerMap() {
        return (Map<String, DialectSystemSchemaManager>) Plugins.getMemberAccessor()
                .get(SystemSchemaManager.class.getDeclaredField("DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP"), null);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTablesArguments")
    void assertGetTables(final String name, final String databaseType, final String schema, final int expectedSize, final String expectedSampleTable) {
        Collection<String> actualTables = SystemSchemaManager.getTables(databaseType, schema);
        assertThat(actualTables.size(), is(expectedSize));
        assertTrue(actualTables.contains(expectedSampleTable));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isSystemTableArguments")
    void assertIsSystemTable(final String name, final String schema, final String tableName, final boolean expectedResult) {
        assertThat(SystemSchemaManager.isSystemTable(schema, tableName), is(expectedResult));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isSystemTableWithDatabaseTypeArguments")
    void assertIsSystemTableWithDatabaseType(final String name, final String databaseType, final String schema,
                                             final String tableName, final boolean commonSchemaManagerAvailable, final boolean expectedResult) {
        if (!commonSchemaManagerAvailable) {
            getSystemSchemaManagerMap().remove("common");
        }
        assertThat(SystemSchemaManager.isSystemTable(databaseType, schema, tableName), is(expectedResult));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isSystemTableWithTableNamesArguments")
    void assertIsSystemTableWithTableNames(final String name, final String databaseType, final String schema,
                                           final Collection<String> tableNames, final boolean commonSchemaManagerAvailable, final boolean expectedResult) {
        if (!commonSchemaManagerAvailable) {
            getSystemSchemaManagerMap().remove("common");
        }
        assertThat(SystemSchemaManager.isSystemTable(databaseType, schema, tableNames), is(expectedResult));
    }
    
    @Test
    void assertGetAllInputStreams() {
        Collection<InputStream> actualInputStreams = SystemSchemaManager.getAllInputStreams("MySQL", "information_schema");
        assertThat(actualInputStreams.size(), is(95));
        for (InputStream each : actualInputStreams) {
            assertNotNull(each);
        }
    }
    
    @Test
    void assertGetAllInputStreamsWithTrunkDatabaseType() {
        Collection<InputStream> actualInputStreams = SystemSchemaManager.getAllInputStreams("H2", "information_schema");
        assertThat(actualInputStreams.size(), is(95));
        for (InputStream each : actualInputStreams) {
            assertNotNull(each);
        }
    }
    
    private static Stream<Arguments> getTablesArguments() {
        return Stream.of(
                Arguments.of("mysql information_schema", "MySQL", "information_schema", 95, "columns"),
                Arguments.of("mysql schema", "MySQL", "mysql", 40, "user"),
                Arguments.of("mysql performance_schema", "MySQL", "performance_schema", 114, "accounts"),
                Arguments.of("mysql sys", "MySQL", "sys", 53, "host_summary"),
                Arguments.of("common shardingsphere schema", "MySQL", "shardingsphere", 1, "cluster_information"),
                Arguments.of("postgresql information_schema", "PostgreSQL", "information_schema", 69, "columns"),
                Arguments.of("postgresql pg_catalog", "PostgreSQL", "pg_catalog", 134, "pg_database"),
                Arguments.of("openGauss information_schema", "openGauss", "information_schema", 66, "columns"),
                Arguments.of("openGauss pg_catalog", "openGauss", "pg_catalog", 240, "pg_database"));
    }
    
    private static Stream<Arguments> isSystemTableArguments() {
        return Stream.of(
                Arguments.of("common information_schema table", "information_schema", "columns", true),
                Arguments.of("postgresql catalog table", "pg_catalog", "pg_database", true),
                Arguments.of("common shardingsphere table", "shardingsphere", "cluster_information", true),
                Arguments.of("non system table", "sharding_db", "t_order", false),
                Arguments.of("unknown table in common schema", "shardingsphere", "foo_tbl", false));
    }
    
    private static Stream<Arguments> isSystemTableWithDatabaseTypeArguments() {
        return Stream.of(
                Arguments.of("mysql with null schema", "MySQL", null, "columns", true, true),
                Arguments.of("postgresql with null schema", "PostgreSQL", null, "pg_database", true, true),
                Arguments.of("mysql null schema unknown table", "MySQL", null, "foo_tbl", true, false),
                Arguments.of("mysql database-specific schema", "MySQL", "information_schema", "columns", true, true),
                Arguments.of("postgresql database-specific schema", "PostgreSQL", "pg_catalog", "pg_database", true, true),
                Arguments.of("mysql falls back to common schema", "MySQL", "shardingsphere", "cluster_information", true, true),
                Arguments.of("unknown database falls back to common schema", "NO_DB", "shardingsphere", "cluster_information", true, true),
                Arguments.of("unknown database without common schema manager", "NO_DB", "shardingsphere", "cluster_information", false, false),
                Arguments.of("unknown database unknown table", "NO_DB", "foo_schema", "foo_tbl", true, false));
    }
    
    private static Stream<Arguments> isSystemTableWithTableNamesArguments() {
        return Stream.of(
                Arguments.of("mysql schema all tables exist", "MySQL", "information_schema", Arrays.asList("columns", "tables", "schemata"), true, true),
                Arguments.of("mysql schema missing one table", "MySQL", "information_schema", Arrays.asList("columns", "nonexistent_table"), true, false),
                Arguments.of("postgresql catalog tables exist", "PostgreSQL", "pg_catalog", Arrays.asList("pg_database", "pg_tables"), true, true),
                Arguments.of("unknown database falls back to common collection", "NO_DB", "shardingsphere", Collections.singleton("cluster_information"), true, true),
                Arguments.of("unknown database without common collection", "NO_DB", "shardingsphere", Collections.singleton("cluster_information"), false, false),
                Arguments.of("unknown database unknown collection", "NO_DB", "foo_schema", Collections.singleton("foo_tbl"), true, false),
                Arguments.of("empty table collection", "MySQL", "foo_schema", Collections.emptyList(), true, true));
    }
}
