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

package org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.table.PhysicalTableMetaData;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SchemaMetaDataTest {
    
    @Test
    public void assertGetAllTableNames() {
        assertThat(new PhysicalSchemaMetaData(ImmutableMap.of("tbl", mock(PhysicalTableMetaData.class))).getAllTableNames(), is(Sets.newHashSet("tbl")));
    }
    
    @Test
    public void assertGet() {
        PhysicalTableMetaData tableMetaData = mock(PhysicalTableMetaData.class);
        assertThat(new PhysicalSchemaMetaData(ImmutableMap.of("tbl", tableMetaData)).get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertMerge() {
        PhysicalSchemaMetaData actual = new PhysicalSchemaMetaData(Collections.emptyMap());
        PhysicalTableMetaData tableMetaData = mock(PhysicalTableMetaData.class);
        actual.merge(new PhysicalSchemaMetaData(ImmutableMap.of("tbl", tableMetaData)));
        assertThat(actual.get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertPut() {
        PhysicalSchemaMetaData actual = new PhysicalSchemaMetaData(Collections.emptyMap());
        PhysicalTableMetaData tableMetaData = mock(PhysicalTableMetaData.class);
        actual.put("tbl", tableMetaData);
        assertThat(actual.get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertRemove() {
        PhysicalSchemaMetaData actual = new PhysicalSchemaMetaData(ImmutableMap.of("tbl", mock(PhysicalTableMetaData.class)));
        actual.remove("tbl");
        assertNull(actual.get("tbl"));
    }
    
    @Test
    public void assertContainsTable() {
        assertTrue(new PhysicalSchemaMetaData(ImmutableMap.of("tbl", mock(PhysicalTableMetaData.class))).containsTable("tbl"));
    }
    
    @Test
    public void assertContainsColumn() {
        PhysicalTableMetaData tableMetaData = new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("col", 0, "dataType", false, false, false)), Collections.emptyList());
        assertTrue(new PhysicalSchemaMetaData(ImmutableMap.of("tbl", tableMetaData)).containsColumn("tbl", "col"));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenContainsKey() {
        PhysicalTableMetaData tableMetaData = new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("col", 0, "dataType", false, false, false)), Collections.emptyList());
        assertThat(new PhysicalSchemaMetaData(ImmutableMap.of("tbl", tableMetaData)).getAllColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenNotContainsKey() {
        PhysicalTableMetaData tableMetaData = new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("col", 0, "dataType", false, false, false)), Collections.emptyList());
        assertThat(new PhysicalSchemaMetaData(ImmutableMap.of("tbl1", tableMetaData)).getAllColumnNames("tbl2"), is(Collections.<String>emptyList()));
    }
}
