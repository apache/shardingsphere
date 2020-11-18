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

package org.apache.shardingsphere.infra.merge.result.impl.memory;

import org.apache.shardingsphere.infra.executor.sql.query.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MemoryQueryResultRowTest {
    
    private MemoryQueryResultRow memoryResultSetRow;
    
    @Before
    public void setUp() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getColumnCount()).thenReturn(1);
        when(queryResult.getValue(1, Object.class)).thenReturn("value");
        memoryResultSetRow = new MemoryQueryResultRow(queryResult);
    }
    
    @Test
    public void assertGetCell() {
        assertThat(memoryResultSetRow.getCell(1).toString(), is("value"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetCellWithNegativeColumnIndex() {
        memoryResultSetRow.getCell(-1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetCellWithColumnIndexOutOfRange() {
        memoryResultSetRow.getCell(2);
    }
    
    @Test
    public void assertSetCell() {
        memoryResultSetRow.setCell(1, "new");
        assertThat(memoryResultSetRow.getCell(1).toString(), is("new"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetCellWithNegativeColumnIndex() {
        memoryResultSetRow.setCell(-1, "new");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetCellWithColumnIndexOutOfRange() {
        memoryResultSetRow.setCell(2, "new");
    }
}
