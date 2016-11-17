/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc;

import org.junit.Before;
import org.junit.Test;

import javax.sql.rowset.RowSetMetaDataImpl;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GeneratedKeysResultSetMetaDataTest {
    
    private ResultSetMetaData actualMetaData;
    
    @Before
    public void init() throws SQLException {
        actualMetaData = GeneratedKeysResultSetTest.createMock().getMetaData();
    }
    
    @Test
    public void getColumnCount() throws Exception {
        assertThat(actualMetaData.getColumnCount(), is(2));
    }
    
    @Test
    public void isAutoIncrement() throws Exception {
        assertTrue(actualMetaData.isAutoIncrement(1));
        assertTrue(actualMetaData.isAutoIncrement(2));
    }
    
    @Test
    public void isCaseSensitive() throws Exception {
        assertTrue(actualMetaData.isCaseSensitive(1));
        assertTrue(actualMetaData.isCaseSensitive(2));
    }
    
    @Test
    public void isSearchable() throws Exception {
        assertFalse(actualMetaData.isSearchable(1));
        assertFalse(actualMetaData.isSearchable(2));
    }
    
    @Test
    public void isCurrency() throws Exception {
        assertFalse(actualMetaData.isCurrency(1));
        assertFalse(actualMetaData.isCurrency(2));
    }
    
    @Test
    public void isNullable() throws Exception {
        assertEquals(actualMetaData.isNullable(1), ResultSetMetaData.columnNoNulls);
        assertEquals(actualMetaData.isNullable(2), ResultSetMetaData.columnNoNulls);
    }
    
    @Test
    public void isSigned() throws Exception {
        assertTrue(actualMetaData.isSigned(1));
        assertTrue(actualMetaData.isSigned(2));
    }
    
    @Test
    public void getColumnDisplaySize() throws Exception {
        assertEquals(actualMetaData.getColumnDisplaySize(1), 0);
        assertEquals(actualMetaData.getColumnDisplaySize(2), 0);
    }
    
    @Test
    public void getColumnLabel() throws Exception {
        assertThat(actualMetaData.getColumnLabel(1), is("order_id"));
        assertThat(actualMetaData.getColumnLabel(2), is("order_no"));
    }
    
    @Test
    public void getColumnName() throws Exception {
        assertThat(actualMetaData.getColumnName(1), is("order_id"));
        assertThat(actualMetaData.getColumnName(2), is("order_no"));
    }
    
    @Test
    public void getSchemaName() throws Exception {
        assertThat(actualMetaData.getSchemaName(1), is(""));
        assertThat(actualMetaData.getSchemaName(2), is(""));
    }
    
    @Test
    public void getPrecision() throws Exception {
        assertEquals(actualMetaData.getPrecision(1), 0);
        assertEquals(actualMetaData.getPrecision(2), 0);
    }
    
    @Test
    public void getScale() throws Exception {
        assertEquals(actualMetaData.getScale(1), 0);
        assertEquals(actualMetaData.getScale(2), 0);
    }
    
    @Test
    public void getTableName() throws Exception {
        assertThat(actualMetaData.getTableName(1), is(""));
        assertThat(actualMetaData.getTableName(2), is(""));
    }
    
    @Test
    public void getCatalogName() throws Exception {
        assertThat(actualMetaData.getCatalogName(1), is(""));
        assertThat(actualMetaData.getCatalogName(2), is(""));
    }
    
    @Test
    public void getColumnType() throws Exception {
        assertEquals(actualMetaData.getColumnType(1), Types.BIGINT);
        assertEquals(actualMetaData.getColumnType(2), Types.VARCHAR);
    }
    
    @Test
    public void getColumnTypeName() throws Exception {
        assertThat(actualMetaData.getColumnTypeName(1), is(""));
        assertThat(actualMetaData.getColumnTypeName(2), is(""));
    }
    
    @Test
    public void isReadOnly() throws Exception {
        assertTrue(actualMetaData.isReadOnly(1));
        assertTrue(actualMetaData.isReadOnly(2));
    }
    
    @Test
    public void isWritable() throws Exception {
        assertFalse(actualMetaData.isWritable(1));
        assertFalse(actualMetaData.isWritable(2));
    }
    
    @Test
    public void isDefinitelyWritable() throws Exception {
        assertFalse(actualMetaData.isDefinitelyWritable(1));
        assertFalse(actualMetaData.isDefinitelyWritable(2));
    }
    
    @Test
    public void getColumnClassName() throws Exception {
        assertThat(actualMetaData.getColumnClassName(1), is("java.lang.Long"));
        assertThat(actualMetaData.getColumnClassName(2), is("java.lang.String"));
    }
    
    @Test
    public void unwrap() throws Exception {
        assertThat(actualMetaData.unwrap(GeneratedKeysResultSetMetaData.class), is((GeneratedKeysResultSetMetaData) actualMetaData));
    }
    
    @Test(expected = SQLException.class)
    public void unwrapError() throws Exception {
        actualMetaData.unwrap(RowSetMetaDataImpl.class);
    }
    
    @Test
    public void isWrapperFor() throws Exception {
        assertTrue(actualMetaData.isWrapperFor(GeneratedKeysResultSetMetaData.class));
    }
    
}
