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

package org.apache.shardingsphere.infra.metadata.database.schema.model;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereSchemaTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertGetAllTables() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertThat(new HashSet<>(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList())
                .getAllTables()), is(Collections.singleton(table)));
    }
    
    @Test
    void assertContainsTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).getTable("foo_tbl"), is(table));
    }
    
    @Test
    void assertPutTable() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        schema.putTable(table);
        assertThat(schema.getTable("foo_tbl"), is(table));
    }
    
    @Test
    void assertRemoveTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        schema.removeTable("foo_tbl");
        assertNull(schema.getTable("foo_tbl"));
    }
    
    @Test
    void assertGetAllViews() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        assertThat(new HashSet<>(new ShardingSphereSchema("foo_db", databaseType, Collections.emptyList(), Collections.singleton(view))
                .getAllViews()), is(Collections.singleton(view)));
    }
    
    @Test
    void assertContainsView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        assertTrue(new ShardingSphereSchema("foo_db", databaseType, Collections.emptyList(), Collections.singleton(view)).containsView("foo_view"));
    }
    
    @Test
    void assertGetView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        assertThat(new ShardingSphereSchema("foo_db", databaseType, Collections.emptyList(), Collections.singleton(view)).getView("foo_view"), is(view));
    }
    
    @Test
    void assertPutView() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.emptyList(), Collections.emptyList());
        schema.putView(new ShardingSphereView("foo_view", "SELECT * FROM test_table"));
        assertTrue(schema.containsView("foo_view"));
    }
    
    @Test
    void assertRemoveView() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.emptyList(),
                Collections.singleton(new ShardingSphereView("foo_view", "SELECT * FROM test_table")));
        schema.removeView("foo_view");
        assertFalse(schema.containsView("foo_view"));
    }
    
    @Test
    void assertContainsIndex() {
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("col_idx", Collections.emptyList(), false)), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).containsIndex("foo_tbl", "col_idx"));
    }
    
    @Test
    void assertContainsIndexWithIndexNotExists() {
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("col_idx", Collections.emptyList(), false)), Collections.emptyList());
        assertFalse(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).containsIndex("foo_tbl", "foo_idx"));
    }
    
    @Test
    void assertContainsIndexWithTableNotExists() {
        assertFalse(new ShardingSphereSchema("foo_db", databaseType).containsIndex("nonexistent_tbl", "nonexistent_idx"));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenTableNotExists() {
        assertTrue(new ShardingSphereSchema("foo_tbl", databaseType, Collections.emptyList(), Collections.emptyList()).getVisibleColumnNames("nonexistent_tbl").isEmpty());
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(
                new ShardingSphereColumn("foo_col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).getVisibleColumnNames("foo_tbl"), is(Collections.singletonList("foo_col")));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(
                new ShardingSphereColumn("foo_col", 0, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).getVisibleColumnNames("foo_tbl").isEmpty());
    }
    
    @Test
    void assertGetVisibleColumnAndIndexMapWhenContainsTable() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", 0, false, false, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(column), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        Map<String, Integer> actual = schema.getVisibleColumnAndIndexMap("foo_tbl");
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("foo_col"));
    }
    
    @Test
    void assertGetVisibleColumnAndIndexMapWhenNotContainsTable() {
        assertTrue(new ShardingSphereSchema("foo_db", databaseType).getVisibleColumnAndIndexMap("nonexistent_tbl").isEmpty());
    }
    
    @Test
    void assertIsEmptyWithEmptyTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList()).isEmpty());
    }
    
    @Test
    void assertIsEmptyWithEmptyView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "");
        assertFalse(new ShardingSphereSchema("foo_db", databaseType, Collections.emptyList(), Collections.singleton(view)).isEmpty());
    }
    
    @Test
    void assertIsEmpty() {
        assertTrue(new ShardingSphereSchema("foo_db", databaseType).isEmpty());
    }
}
