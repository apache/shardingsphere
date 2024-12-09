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

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereSchemaTest {
    
    @Test
    void assertGetTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("tbl");
        assertThat(new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.emptyList()).getTable("tbl"), is(table));
    }
    
    @Test
    void assertGetView() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("tbl");
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(view.getName()).thenReturn("tbl_view");
        assertThat(new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.singleton(view)).getView("tbl_view"), is(view));
    }
    
    @Test
    void assertPutTable() {
        ShardingSphereSchema actual = new ShardingSphereSchema("foo_db", Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("tbl");
        actual.putTable(table);
        assertThat(actual.getTable("tbl"), is(table));
    }
    
    @Test
    void assertRemoveTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("tbl");
        ShardingSphereSchema actual = new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.emptyList());
        actual.removeTable("tbl");
        assertNull(actual.getTable("tbl"));
    }
    
    @Test
    void assertContainsTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("tbl");
        assertTrue(new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.emptyList()).containsTable("tbl"));
    }
    
    @Test
    void assertContainsIndex() {
        ShardingSphereTable table = new ShardingSphereTable(
                "tbl", Collections.emptyList(), Collections.singletonList(new ShardingSphereIndex("col_idx", Collections.emptyList(), false)), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.emptyList()).containsIndex("tbl", "col_idx"));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.emptyList()).getVisibleColumnNames("tbl"),
                is(Collections.singletonList("col")));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema("foo_db", Collections.singleton(table), Collections.emptyList()).getVisibleColumnNames("tbl"), is(Collections.emptyList()));
    }
}
