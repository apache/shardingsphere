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

package org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.table;

import org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.column.PhysicalColumnMetaData;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableMetaDataTest {
    
    private PhysicalTableMetaData tableMetaData;
    
    @Before
    public void setUp() {
        tableMetaData = new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("test", Types.INTEGER, "INT", true, false, true)), Collections.emptyList());
    }
    
    @Test
    public void assertGetColumnMetaData() {
        PhysicalColumnMetaData actual = tableMetaData.getColumnMetaData(0);
        assertThat(actual.getName(), is("test"));
        assertThat(actual.getDataType(), is(Types.INTEGER));
        assertThat(actual.getDataTypeName(), is("INT"));
        assertTrue(actual.isPrimaryKey());
        assertFalse(actual.isGenerated());
        assertTrue(actual.isCaseSensitive());
    }
    
    @Test
    public void assertFindColumnIndex() {
        assertThat(tableMetaData.findColumnIndex("test"), is(0));
        assertThat(tableMetaData.findColumnIndex("non_exist"), is(-1));
    }
    
    @Test
    public void assertIsPrimaryKey() {
        assertTrue(tableMetaData.isPrimaryKey(0));
        assertFalse(tableMetaData.isPrimaryKey(1));
    }
    
    @Test
    public void assertEmptyColumnsWithDefaultConstructor() {
        tableMetaData = new PhysicalTableMetaData();
        assertThat(tableMetaData.getColumns(), is(Collections.emptyMap()));
    }
}
