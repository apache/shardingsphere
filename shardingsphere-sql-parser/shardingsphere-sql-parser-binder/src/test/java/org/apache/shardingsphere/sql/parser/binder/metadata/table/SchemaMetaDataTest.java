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

package org.apache.shardingsphere.sql.parser.binder.metadata.table;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SchemaMetaDataTest {
    
    @Test
    public void assertGet() {
        TableMetaData tableMetaData = mock(TableMetaData.class);
        SchemaMetaData schemaMetaData = new SchemaMetaData(ImmutableMap.of("tbl", tableMetaData));
        assertThat(schemaMetaData.get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertPut() {
        SchemaMetaData schemaMetaData = new SchemaMetaData(Collections.emptyMap());
        TableMetaData tableMetaData = mock(TableMetaData.class);
        schemaMetaData.put("tbl", tableMetaData);
        assertThat(schemaMetaData.get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertRemove() {
        SchemaMetaData schemaMetaData = new SchemaMetaData(ImmutableMap.of("tbl", mock(TableMetaData.class)));
        schemaMetaData.remove("tbl");
        assertNull(schemaMetaData.get("tbl"));
    }
    
    @Test
    public void assertContainsTable() {
        assertTrue(new SchemaMetaData(ImmutableMap.of("tbl", mock(TableMetaData.class))).containsTable("tbl"));
    }
    
    @Test
    public void assertContainsColumn() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("col", "dataType", false)), Collections.emptyList());
        assertTrue(new SchemaMetaData(ImmutableMap.of("tbl", tableMetaData)).containsColumn("tbl", "col"));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenContainsKey() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("col", "dataType", false)), Collections.emptyList());
        assertThat(new SchemaMetaData(ImmutableMap.of("tbl", tableMetaData)).getAllColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenNotContainsKey() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("col", "dataType", false)), Collections.emptyList());
        assertThat(new SchemaMetaData(ImmutableMap.of("tbl1", tableMetaData)).getAllColumnNames("tbl2"), is(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertGetAllTableNames() {
        assertThat(new SchemaMetaData(ImmutableMap.of("tbl", mock(TableMetaData.class))).getAllTableNames(), is(Sets.newHashSet("tbl")));
    }
}
