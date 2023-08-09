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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereTableTest {
    
    private ShardingSphereTable shardingSphereTable;
    
    @BeforeEach
    void setUp() {
        shardingSphereTable = new ShardingSphereTable();
    }
    
    @Test
    void assertPutColumn() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("foo_column_1", Types.INTEGER, true, true, false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("foo_column_2", Types.INTEGER, false, true, false, true, false, false);
        shardingSphereTable.putColumn(column1);
        shardingSphereTable.putColumn(column2);
        assertThat(shardingSphereTable.getColumn("foo_column_1"), is(column1));
        assertThat(shardingSphereTable.getColumn("foo_column_2"), is(column2));
        assertThat(shardingSphereTable.getColumnValues(), hasSize(2));
    }
    
    @Test
    void assertGetColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_column", Types.INTEGER, true, true, false, true, false, false);
        shardingSphereTable.putColumn(column);
        assertThat(shardingSphereTable.getColumn("foo_column"), is(column));
        assertThat(shardingSphereTable.getColumn("FOO_COLUMN"), is(column));
        assertNull(shardingSphereTable.getColumn("invalid"));
    }
    
    @Test
    void assertGetColumns() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("foo_column_1", Types.INTEGER, true, true, false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("foo_column_2", Types.INTEGER, false, true, false, true, false, false);
        shardingSphereTable.putColumn(column1);
        shardingSphereTable.putColumn(column2);
        assertThat(shardingSphereTable.getColumnValues(), hasItems(column1, column2));
        assertThat(shardingSphereTable.getColumnValues(), hasSize(2));
    }
    
    @Test
    void assertContainsColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_column", Types.INTEGER, true, true, false, true, false, false);
        shardingSphereTable.putColumn(column);
        assertTrue(shardingSphereTable.containsColumn("foo_column"));
        assertFalse(shardingSphereTable.containsColumn("invalid"));
    }
    
    @Test
    void assertPutIndex() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_index_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_index_2");
        shardingSphereTable.putIndex(index1);
        shardingSphereTable.putIndex(index2);
        assertThat(shardingSphereTable.getIndex("foo_index_1"), is(index1));
        assertThat(shardingSphereTable.getIndex("foo_index_2"), is(index2));
        assertNull(shardingSphereTable.getIndex("invalid"));
        assertThat(shardingSphereTable.getIndexValues(), hasSize(2));
    }
    
    @Test
    void assertGetIndex() {
        ShardingSphereIndex index = new ShardingSphereIndex("foo_index");
        shardingSphereTable.putIndex(index);
        assertThat(shardingSphereTable.getIndex("foo_index"), is(index));
        assertThat(shardingSphereTable.getIndex("FOO_INDEX"), is(index));
        assertNull(shardingSphereTable.getIndex("invalid"));
    }
    
    @Test
    void assertRemoveIndex() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_index_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_index_2");
        shardingSphereTable.putIndex(index1);
        shardingSphereTable.putIndex(index2);
        shardingSphereTable.removeIndex("foo_index_1");
        assertNull(shardingSphereTable.getIndex("foo_index_1"));
        shardingSphereTable.removeIndex("invalid");
        assertThat(shardingSphereTable.getIndex("foo_index_2"), is(index2));
        assertThat(shardingSphereTable.getIndexValues(), hasSize(1));
    }
    
    @Test
    void assertGetIndexes() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_index_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_index_2");
        shardingSphereTable.putIndex(index1);
        shardingSphereTable.putIndex(index2);
        assertThat(shardingSphereTable.getIndexValues(), hasItems(index1, index2));
        assertThat(shardingSphereTable.getIndexValues(), hasSize(2));
    }
    
    @Test
    void assertContainsIndex() {
        ShardingSphereIndex index1 = new ShardingSphereIndex("foo_index_1");
        ShardingSphereIndex index2 = new ShardingSphereIndex("foo_index_2");
        shardingSphereTable.putIndex(index1);
        shardingSphereTable.putIndex(index2);
        assertTrue(shardingSphereTable.containsIndex("foo_index_1"));
        assertTrue(shardingSphereTable.containsIndex("foo_index_2"));
        assertFalse(shardingSphereTable.containsIndex("invalid"));
    }
    
    @Test
    void assertGetConstraints() {
        ShardingSphereConstraint constraint = new ShardingSphereConstraint("t_order_foreign_key", "t_user");
        ShardingSphereTable table = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.singletonList(constraint));
        assertThat(table.getConstraintValues(), hasItems(constraint));
        assertThat(table.getConstraintValues(), hasSize(1));
    }
    
    @Test
    void assertTableEquals() {
        shardingSphereTable.putColumn(new ShardingSphereColumn("foo_column_1", Types.INTEGER, true, true, false, true, false, false));
        shardingSphereTable.putIndex(new ShardingSphereIndex("foo_index_1"));
        ShardingSphereTable otherTable = new ShardingSphereTable();
        otherTable.putColumn(new ShardingSphereColumn("foo_column_1", Types.INTEGER, true, true, false, true, false, false));
        otherTable.putIndex(new ShardingSphereIndex("foo_index_1"));
        assertEquals(shardingSphereTable, otherTable);
    }
}
