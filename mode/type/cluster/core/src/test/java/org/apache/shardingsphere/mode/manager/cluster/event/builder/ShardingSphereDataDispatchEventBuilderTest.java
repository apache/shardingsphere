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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.TableDataChangedEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDataDispatchEventBuilderTest {
    
    private final ShardingSphereDataDispatchEventBuilder builder = new ShardingSphereDataDispatchEventBuilder();
    
    @Test
    void assertGetSubscribedKey() {
        assertThat(builder.getSubscribedKey(), is("/statistics/databases"));
    }
    
    @Test
    void assertBuildDatabaseDataChangedEventWithAdd() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db", "", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((DatabaseDataAddedEvent) actual.get()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertBuildDatabaseDataChangedEventWithUpdate() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((DatabaseDataAddedEvent) actual.get()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertBuildDatabaseDataChangedEventWithDelete() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db", "", Type.DELETED));
        assertTrue(actual.isPresent());
        assertThat(((DatabaseDataDeletedEvent) actual.get()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertBuildDatabaseDataChangedEventWithIgnore() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db", "", Type.IGNORED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildSchemaDataChangedEventWithAdd() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema", "", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((SchemaDataAddedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((SchemaDataAddedEvent) actual.get()).getSchemaName(), is("foo_schema"));
    }
    
    @Test
    void assertBuildSchemaDataChangedEventWithUpdate() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((SchemaDataAddedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((SchemaDataAddedEvent) actual.get()).getSchemaName(), is("foo_schema"));
    }
    
    @Test
    void assertBuildSchemaDataChangedEventWithDelete() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema", "", Type.DELETED));
        assertTrue(actual.isPresent());
        assertThat(((SchemaDataDeletedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((SchemaDataDeletedEvent) actual.get()).getSchemaName(), is("foo_schema"));
    }
    
    @Test
    void assertBuildSchemaDataChangedEventWithIgnore() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema", "", Type.IGNORED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildTableDataChangedEventWithAdd() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((TableDataChangedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((TableDataChangedEvent) actual.get()).getSchemaName(), is("foo_schema"));
        assertThat(((TableDataChangedEvent) actual.get()).getAddedTable(), is("foo_tbl"));
        assertNull(((TableDataChangedEvent) actual.get()).getDeletedTable());
    }
    
    @Test
    void assertBuildTableDataChangedEventWithUpdate() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((TableDataChangedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((TableDataChangedEvent) actual.get()).getSchemaName(), is("foo_schema"));
        assertThat(((TableDataChangedEvent) actual.get()).getAddedTable(), is("foo_tbl"));
        assertNull(((TableDataChangedEvent) actual.get()).getDeletedTable());
    }
    
    @Test
    void assertBuildTableDataChangedEventWithDelete() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        assertTrue(actual.isPresent());
        assertThat(((TableDataChangedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((TableDataChangedEvent) actual.get()).getSchemaName(), is("foo_schema"));
        assertNull(((TableDataChangedEvent) actual.get()).getAddedTable());
        assertThat(((TableDataChangedEvent) actual.get()).getDeletedTable(), is("foo_tbl"));
    }
    
    @Test
    void assertBuildTableDataChangedEventWithIgnore() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.IGNORED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildRowDataChangedEventWithAdd() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/xxx", "{uniqueKey: 1}", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getSchemaName(), is("foo_schema"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getTableName(), is("foo_tbl"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getYamlRowData().getUniqueKey(), is("1"));
    }
    
    @Test
    void assertBuildRowDataChangedEventWithUpdate() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "{uniqueKey: 1}", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getSchemaName(), is("foo_schema"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getTableName(), is("foo_tbl"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual.get()).getYamlRowData().getUniqueKey(), is("1"));
    }
    
    @Test
    void assertBuildRowDataChangedEventWithDelete() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "", Type.DELETED));
        assertTrue(actual.isPresent());
        assertThat(((ShardingSphereRowDataDeletedEvent) actual.get()).getDatabaseName(), is("foo_db"));
        assertThat(((ShardingSphereRowDataDeletedEvent) actual.get()).getSchemaName(), is("foo_schema"));
        assertThat(((ShardingSphereRowDataDeletedEvent) actual.get()).getTableName(), is("foo_tbl"));
        assertThat(((ShardingSphereRowDataDeletedEvent) actual.get()).getUniqueKey(), is("1"));
    }
    
    @Test
    void assertBuildRowDataChangedEventWithAddNullValue() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildWithMissedDatabaseNameEventKey() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildWithMissedSchemaNameEventKey() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildWithMissedTableNameEventKey() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildWithMissedRowEventKey() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
