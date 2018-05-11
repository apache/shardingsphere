/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql.common;

import io.shardingsphere.core.merger.fixture.TestQueryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MemoryQueryResultRowTest {
    
    @Mock
    private ResultSet resultSet;
    
    private MemoryQueryResultRow memoryResultSetRow;
    
    @Before
    public void setUp() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getObject(1)).thenReturn("value");
        memoryResultSetRow = new MemoryQueryResultRow(new TestQueryResult(resultSet));
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
