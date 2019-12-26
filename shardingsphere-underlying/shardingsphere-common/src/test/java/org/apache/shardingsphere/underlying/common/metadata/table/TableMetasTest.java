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

package org.apache.shardingsphere.underlying.common.metadata.table;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class TableMetasTest {
    
    @Test
    public void assertGet() {
        TableMetaData tableMetaData = mock(TableMetaData.class);
        TableMetas tableMetas = new TableMetas(ImmutableMap.of("tableMetaData", tableMetaData));
        assertThat(tableMetas.get("tableMetaData"), is(tableMetaData));
    }
    
    @Test
    public void assertPut() {
        TableMetas tableMetas = new TableMetas(Collections.<String, TableMetaData>emptyMap());
        TableMetaData tableMetaData = mock(TableMetaData.class);
        tableMetas.put("tableMetaData", tableMetaData);
        assertThat(tableMetas.get("tableMetaData"), is(tableMetaData));
    }
    
    @Test
    public void assertRemove() {
        TableMetas tableMetas = new TableMetas(ImmutableMap.of("tableMetaData", mock(TableMetaData.class)));
        tableMetas.remove("tableMetaData");
        assertNull(tableMetas.get("tableMetaData"));
    }
    
    @Test
    public void assertContainsTable() {
        assertTrue(new TableMetas(ImmutableMap.of("tableMetaData", mock(TableMetaData.class))).containsTable("tableMetaData"));
    }
    
    @Test
    public void assertContainsColumn() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("name", "dataType", false)), Collections.<String>emptyList());
        assertTrue(new TableMetas(ImmutableMap.of("tableMetaData", tableMetaData)).containsColumn("tableMetaData", "name"));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenContainsKey() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("name", "dataType", false)), Collections.<String>emptyList());
        assertThat(new TableMetas(ImmutableMap.of("tableMetaData", tableMetaData)).getAllColumnNames("tableMetaData"), is(Collections.singletonList("name")));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenNotContainsKey() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("name", "dataType", false)), Collections.<String>emptyList());
        assertThat(new TableMetas(ImmutableMap.of("tableMetaData", tableMetaData)).getAllColumnNames("other_tableMetaData"), is(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertGetAllTableNames() {
        assertThat(new TableMetas(ImmutableMap.of("tableMetaData", mock(TableMetaData.class))).getAllTableNames(), is((Collection<String>) Sets.newHashSet("tableMetaData")));
    }
}
