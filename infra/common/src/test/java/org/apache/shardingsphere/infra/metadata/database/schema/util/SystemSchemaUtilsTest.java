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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemSchemaUtilsTest {
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType POSTGRESQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final DatabaseType OPEN_GAUSS_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private static final DatabaseType FIXTURE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("containsSystemSchemaArguments")
    void assertContainsSystemSchema(final String name, final DatabaseType databaseType, final Collection<String> schemaNames,
                                    final String databaseName, final boolean complete, final boolean expectedContainsSystemSchema) {
        assertThat(SystemSchemaUtils.containsSystemSchema(databaseType, schemaNames, mockDatabase(databaseName, complete, databaseType)), is(expectedContainsSystemSchema));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isSystemSchemaArguments")
    void assertIsSystemSchema(final String name, final DatabaseType databaseType, final String databaseName, final boolean complete, final boolean expectedSystemSchema) {
        assertThat(SystemSchemaUtils.isSystemSchema(mockDatabase(databaseName, complete, databaseType)), is(expectedSystemSchema));
    }
    
    private ShardingSphereDatabase mockDatabase(final String databaseName, final boolean complete, final DatabaseType databaseType) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(databaseName);
        when(result.isComplete()).thenReturn(complete);
        when(result.getProtocolType()).thenReturn(databaseType);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isDriverQuerySystemCatalogArguments")
    void assertIsDriverQuerySystemCatalog(final String name, final DatabaseType databaseType, final Collection<ProjectionSegment> projections, final boolean expectedDriverQuerySystemCatalog) {
        assertThat(SystemSchemaUtils.isDriverQuerySystemCatalog(databaseType, projections), is(expectedDriverQuerySystemCatalog));
    }
    
    private static Stream<Arguments> containsSystemSchemaArguments() {
        return Stream.of(
                Arguments.of("returns true when input schema names contain a MySQL system schema", MYSQL_DATABASE_TYPE,
                        Arrays.asList("information_schema", "mysql"), "information_schema", false, true),
                Arguments.of("returns false immediately for a complete MySQL database without default schema", MYSQL_DATABASE_TYPE,
                        Collections.emptyList(), "information_schema", true, false),
                Arguments.of("returns true when incomplete MySQL database name is a system schema", MYSQL_DATABASE_TYPE,
                        Collections.emptyList(), "information_schema", false, true),
                Arguments.of("returns false when schema names do not contain a MySQL system schema", MYSQL_DATABASE_TYPE,
                        Collections.singletonList("sharding_db"), "sharding_db", false, false),
                Arguments.of("returns false when PostgreSQL falls back to database name with default schema", POSTGRESQL_DATABASE_TYPE,
                        Collections.emptyList(), "pg_catalog", true, false));
    }
    
    private static Stream<Arguments> isSystemSchemaArguments() {
        return Stream.of(
                Arguments.of("returns true for an incomplete MySQL system database", MYSQL_DATABASE_TYPE, "information_schema", false, true),
                Arguments.of("returns true for a complete PostgreSQL system schema", POSTGRESQL_DATABASE_TYPE, "pg_catalog", true, true),
                Arguments.of("returns false for a complete MySQL database without default schema", MYSQL_DATABASE_TYPE, "information_schema", true, false),
                Arguments.of("returns false for a non-system fixture database", FIXTURE_DATABASE_TYPE, "foo_db", false, false));
    }
    
    private static Stream<Arguments> isDriverQuerySystemCatalogArguments() {
        return Stream.of(
                Arguments.of("returns false when multiple projections are present", OPEN_GAUSS_DATABASE_TYPE,
                        Arrays.asList(new ExpressionProjectionSegment(0, "version()".length(), "version()"), new ExpressionProjectionSegment(0, "current_database()".length(), "current_database()")),
                        false),
                Arguments.of("returns false when the only projection is not an expression", OPEN_GAUSS_DATABASE_TYPE,
                        Collections.singletonList(mock(ProjectionSegment.class)), false),
                Arguments.of("returns false when the database type does not define system catalog expressions", MYSQL_DATABASE_TYPE,
                        Collections.singletonList(new ExpressionProjectionSegment(0, "version()".length(), "version()")), false),
                Arguments.of("returns true when openGauss projection matches a system catalog expression", OPEN_GAUSS_DATABASE_TYPE,
                        Collections.singletonList(new ExpressionProjectionSegment(0, "version()".length(), "version()")), true));
    }
}
