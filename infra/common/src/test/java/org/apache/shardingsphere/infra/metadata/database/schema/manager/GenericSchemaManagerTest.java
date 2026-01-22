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

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenericSchemaManagerTest {
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesAdded() {
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        when(reloadDatabase.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("foo_schema",
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList(),
                mock(DatabaseType.class))));
        ShardingSphereSchema currentSchemas = new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.emptyList(), mock(DatabaseType.class));
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(Collections.singleton(currentSchemas));
        when(currentDatabase.containsSchema("foo_schema")).thenReturn(true);
        when(currentDatabase.getSchema("foo_schema")).thenReturn(currentSchemas);
        Collection<ShardingSphereSchema> actual = GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(reloadDatabase, currentDatabase);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAllTables().size(), is(1));
        assertTrue(actual.iterator().next().containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetToBeAlteredSchemasWithTablesDropped() {
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        when(reloadDatabase.containsSchema("foo_schema")).thenReturn(true);
        when(reloadDatabase.getSchema("foo_schema")).thenReturn(new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.emptyList(),
                mock(DatabaseType.class)));
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("foo_schema",
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList(),
                mock(DatabaseType.class))));
        Collection<ShardingSphereSchema> actual = GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(reloadDatabase, currentDatabase);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAllTables().size(), is(1));
        assertTrue(actual.iterator().next().containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetToBeAddedTables() {
        ShardingSphereSchema reloadSchema = new ShardingSphereSchema("foo_schema",
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE)), Collections.emptyList(),
                mock(DatabaseType.class));
        Collection<ShardingSphereTable> actual = GenericSchemaManager.getToBeAddedTables(reloadSchema, new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.emptyList(),
                mock(DatabaseType.class)));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertGetToBeDroppedTables() {
        ShardingSphereSchema reloadSchema = new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.emptyList(), mock(DatabaseType.class));
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("foo_schema",
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE)), Collections.emptyList(),
                mock(DatabaseType.class));
        Collection<ShardingSphereTable> actual = GenericSchemaManager.getToBeDroppedTables(reloadSchema, currentSchema);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertGetToBeDroppedSchemaNames() {
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("foo_schema", mock(DatabaseType.class))));
        Collection<String> actual = GenericSchemaManager.getToBeDroppedSchemaNames(mock(ShardingSphereDatabase.class), currentDatabase);
        assertThat(actual, is(Collections.singleton("foo_schema")));
    }
}
