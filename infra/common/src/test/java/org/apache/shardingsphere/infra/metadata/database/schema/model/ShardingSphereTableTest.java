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

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereTableTest {
    
    @Test
    void assertGetColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", Types.INTEGER, true, true, false, true, false, false);
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        assertThat(shardingSphereTable.getColumn("foo_col"), is(column));
        assertThat(shardingSphereTable.getColumn("FOO_COL"), is(column));
        assertNull(shardingSphereTable.getColumn("invalid"));
    }
    
    @Test
    void assertGetAllColumns() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("foo_col_1", Types.INTEGER, true, true, false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("foo_col_2", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Arrays.asList(column1, column2), Collections.emptyList(), Collections.emptyList());
        assertThat(shardingSphereTable.getAllColumns(), hasItems(column1, column2));
        assertThat(shardingSphereTable.getAllColumns(), hasSize(2));
    }
    
    @Test
    void assertContainsColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", Types.INTEGER, true, true, false, true, false, false);
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        assertTrue(shardingSphereTable.containsColumn("foo_col"));
        assertFalse(shardingSphereTable.containsColumn("invalid"));
    }
    
    @Test
    void assertPutIndex() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_idx_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_idx_2");
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Arrays.asList(index1, index2), Collections.emptyList());
        assertThat(shardingSphereTable.getIndex("foo_idx_1"), is(index1));
        assertThat(shardingSphereTable.getIndex("foo_idx_2"), is(index2));
        assertNull(shardingSphereTable.getIndex("invalid"));
        assertThat(shardingSphereTable.getAllIndexes(), hasSize(2));
    }
    
    @Test
    void assertGetIndex() {
        ShardingSphereIndex index = new ShardingSphereIndex("foo_idx");
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.singleton(index), Collections.emptyList());
        assertThat(shardingSphereTable.getIndex("foo_idx"), is(index));
        assertThat(shardingSphereTable.getIndex("FOO_IDX"), is(index));
        assertNull(shardingSphereTable.getIndex("invalid"));
    }
    
    @Test
    void assertRemoveIndex() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_idx_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_idx_2");
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Arrays.asList(index1, index2), Collections.emptyList());
        shardingSphereTable.removeIndex("foo_idx_1");
        assertNull(shardingSphereTable.getIndex("foo_idx_1"));
        shardingSphereTable.removeIndex("invalid");
        assertThat(shardingSphereTable.getIndex("foo_idx_2"), is(index2));
        assertThat(shardingSphereTable.getAllIndexes(), hasSize(1));
    }
    
    @Test
    void assertGetIndexes() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_idx_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_idx_2");
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Arrays.asList(index1, index2), Collections.emptyList());
        assertThat(shardingSphereTable.getAllIndexes(), hasItems(index1, index2));
        assertThat(shardingSphereTable.getAllIndexes(), hasSize(2));
    }
    
    @Test
    void assertContainsIndex() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_idx_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_idx_2");
        ShardingSphereTable shardingSphereTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Arrays.asList(index1, index2), Collections.emptyList());
        assertTrue(shardingSphereTable.containsIndex("foo_idx_1"));
        assertTrue(shardingSphereTable.containsIndex("foo_idx_2"));
        assertFalse(shardingSphereTable.containsIndex("invalid"));
    }
    
    @Test
    void assertGetConstraints() {
        ShardingSphereConstraint constraint = new ShardingSphereConstraint("foo_tbl_foreign_key", "foo_tbl");
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.singletonList(constraint));
        assertThat(table.getAllConstraints(), hasItems(constraint));
        assertThat(table.getAllConstraints(), hasSize(1));
    }
    
    @Test
    void assertEquals() {
        ShardingSphereTable table1 = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, true, true, false, true, false, false)),
                Collections.singleton(new ShardingSphereIndex("foo_idx")), Collections.emptyList());
        ShardingSphereTable table2 = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, true, true, false, true, false, false)),
                Collections.singleton(new ShardingSphereIndex("foo_idx")), Collections.emptyList());
        assertThat(table1, is(table2));
    }
}
