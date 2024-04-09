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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShardingSphereSchemaTest {
    
    @Test
    void assertGetAllTableNames() {
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.emptyMap()).getAllTableNames(),
                is(new HashSet<>(Collections.singleton("tbl"))));
    }
    
    @Test
    void assertGetAllViewNames() {
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)),
                Collections.singletonMap("tbl_view", mock(ShardingSphereView.class))).getAllViewNames(), is(new HashSet<>(Collections.singleton("tbl_view"))));
    }
    
    @Test
    void assertGetTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getTable("tbl"), is(table));
    }
    
    @Test
    void assertGetView() {
        ShardingSphereView view = mock(ShardingSphereView.class);
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.singletonMap("tbl_view", view)).getView("tbl_view"), is(view));
    }
    
    @Test
    void assertPutTable() {
        ShardingSphereSchema actual = new ShardingSphereSchema(Collections.emptyMap(), Collections.emptyMap());
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        actual.putTable("tbl", table);
        assertThat(actual.getTable("tbl"), is(table));
    }
    
    @Test
    void assertRemoveTable() {
        ShardingSphereSchema actual = new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.emptyMap());
        actual.removeTable("tbl");
        assertNull(actual.getTable("tbl"));
    }
    
    @Test
    void assertContainsTable() {
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.emptyMap()).containsTable("tbl"));
    }
    
    @Test
    void assertContainsColumn() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).containsColumn("tbl", "col"));
    }
    
    @Test
    void assertGetAllColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getAllColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    void assertGetAllColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl1", table), Collections.emptyMap()).getAllColumnNames("tbl2"), is(Collections.<String>emptyList()));
    }
    
    @Test
    void assertContainsIndex() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.emptyList(), Collections.singletonList(new ShardingSphereIndex("col_idx")), Collections.emptyList());
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).containsIndex("tbl", "col_idx"));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getVisibleColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getVisibleColumnNames("tbl"), is(Collections.emptyList()));
    }
}
