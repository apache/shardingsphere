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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenericSchemaManagerTest {
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesAdded() {
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        ShardingSphereSchema reloadSchema = createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE));
        when(reloadDatabase.getAllSchemas()).thenReturn(Collections.singleton(reloadSchema));
        ShardingSphereSchema currentSchema = createSchema("foo_schema");
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(Collections.singleton(currentSchema));
        when(currentDatabase.containsSchema("foo_schema")).thenReturn(true);
        when(currentDatabase.getSchema("foo_schema")).thenReturn(currentSchema);
        Collection<ShardingSphereSchema> actualSchemas = GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(reloadDatabase, currentDatabase);
        assertThat(actualSchemas.size(), is(1));
        ShardingSphereSchema actualSchema = actualSchemas.iterator().next();
        assertThat(actualSchema.getName(), is("foo_schema"));
        assertThat(actualSchema.getAllTables().size(), is(1));
        assertTrue(actualSchema.containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesAddedWithNewSchema() {
        ShardingSphereSchema reloadSchema = createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE));
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        when(reloadDatabase.getAllSchemas()).thenReturn(Collections.singleton(reloadSchema));
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(Collections.emptyList());
        when(currentDatabase.containsSchema("foo_schema")).thenReturn(false);
        Collection<ShardingSphereSchema> actualSchemas = GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(reloadDatabase, currentDatabase);
        assertThat(actualSchemas.size(), is(1));
        ShardingSphereSchema actualSchema = actualSchemas.iterator().next();
        assertThat(actualSchema.getName(), is("foo_schema"));
        assertThat(actualSchema.getAllTables().size(), is(1));
        assertTrue(actualSchema.containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesDropped() {
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        when(reloadDatabase.containsSchema("foo_schema")).thenReturn(true);
        when(reloadDatabase.containsSchema("bar_schema")).thenReturn(false);
        when(reloadDatabase.getSchema("foo_schema")).thenReturn(createSchema("foo_schema"));
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(
                Arrays.asList(createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)), createSchema("bar_schema", createTable("bar_tbl", TableType.TABLE))));
        Collection<ShardingSphereSchema> actualSchemas = GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(reloadDatabase, currentDatabase);
        assertThat(actualSchemas.size(), is(1));
        ShardingSphereSchema actualSchema = actualSchemas.iterator().next();
        assertThat(actualSchema.getName(), is("foo_schema"));
        assertThat(actualSchema.getAllTables().size(), is(1));
        assertTrue(actualSchema.containsTable("foo_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getToBeAddedTablesArguments")
    void assertGetToBeAddedTables(final String name, final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema, final Collection<String> expectedTableNames) {
        assertThat(GenericSchemaManager.getToBeAddedTables(reloadSchema, currentSchema).stream().map(ShardingSphereTable::getName).collect(Collectors.toSet()), is(expectedTableNames));
    }
    
    @Test
    void assertGetToBeDroppedTables() {
        assertThat(GenericSchemaManager.getToBeDroppedTables(createSchema("foo_schema"), createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)))
                .stream().map(ShardingSphereTable::getName).collect(Collectors.toSet()), is(Collections.singleton("foo_tbl")));
    }
    
    @Test
    void assertGetToBeDroppedTablesWithExistingTable() {
        assertTrue(GenericSchemaManager.getToBeDroppedTables(
                createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)), createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE))).isEmpty());
    }
    
    @Test
    void assertGetToBeDroppedSchemaNames() {
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(Stream.of(createSchema("foo_schema"), createSchema("bar_schema")).collect(Collectors.toList()));
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        when(reloadDatabase.containsSchema("foo_schema")).thenReturn(true);
        assertThat(GenericSchemaManager.getToBeDroppedSchemaNames(reloadDatabase, currentDatabase), is(Collections.singleton("bar_schema")));
    }
    
    private static Stream<Arguments> getToBeAddedTablesArguments() {
        return Stream.of(
                Arguments.of("table absent in current schema",
                        createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)), createSchema("foo_schema"), Collections.singleton("foo_tbl")),
                Arguments.of("same table definition already exists",
                        createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)), createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)), Collections.emptySet()),
                Arguments.of("same table name but different definition",
                        createSchema("foo_schema", createTable("foo_tbl", TableType.TABLE)), createSchema("foo_schema", createTable("foo_tbl", TableType.VIEW)), Collections.singleton("foo_tbl")));
    }
    
    private static ShardingSphereSchema createSchema(final String name, final ShardingSphereTable... tables) {
        return new ShardingSphereSchema(name, mock(DatabaseType.class), Stream.of(tables).collect(Collectors.toList()), Collections.emptyList());
    }
    
    private static ShardingSphereTable createTable(final String name, final TableType type) {
        return new ShardingSphereTable(name, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), type);
    }
}
