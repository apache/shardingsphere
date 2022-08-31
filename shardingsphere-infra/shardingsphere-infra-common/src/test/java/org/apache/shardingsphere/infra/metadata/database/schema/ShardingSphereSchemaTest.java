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

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereSchemaTest {
    
    @Test
    public void assertGetAllTableNames() {
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.emptyMap()).getAllTableNames(),
                is(new HashSet<>(Collections.singleton("tbl"))));
    }
    
    @Test
    public void assertGetAllViewNames() {
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)),
                Collections.singletonMap("tbl_view", mock(ShardingSphereView.class))).getAllViewNames(), is(new HashSet<>(Collections.singleton("tbl_view"))));
    }
    
    @Test
    public void assertGetTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getTable("tbl"), is(table));
    }
    
    @Test
    public void assertGetView() {
        ShardingSphereView view = mock(ShardingSphereView.class);
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.singletonMap("tbl_view", view)).getView("tbl_view"), is(view));
    }
    
    @Test
    public void assertPutTable() {
        ShardingSphereSchema actual = new ShardingSphereSchema(Collections.emptyMap(), Collections.emptyMap());
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        actual.putTable("tbl", table);
        assertThat(actual.getTable("tbl"), is(table));
    }
    
    @Test
    public void assertRemoveTable() {
        ShardingSphereSchema actual = new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.emptyMap());
        actual.removeTable("tbl");
        assertNull(actual.getTable("tbl"));
    }
    
    @Test
    public void assertContainsTable() {
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(ShardingSphereTable.class)), Collections.emptyMap()).containsTable("tbl"));
    }
    
    @Test
    public void assertContainsColumn() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true)), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).containsColumn("tbl", "col"));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getAllColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl1", table), Collections.emptyMap()).getAllColumnNames("tbl2"), is(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertContainsIndex() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.emptyList(), Collections.singletonList(new ShardingSphereIndex("col_idx")), Collections.emptyList());
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).containsIndex("tbl", "col_idx"));
    }
    
    @Test
    public void assertGetVisibleColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, true)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getVisibleColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    public void assertGetVisibleColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("tbl", Collections.singletonList(
                new ShardingSphereColumn("col", 0, false, false, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap()).getVisibleColumnNames("tbl"), is(Collections.emptyList()));
    }
}
