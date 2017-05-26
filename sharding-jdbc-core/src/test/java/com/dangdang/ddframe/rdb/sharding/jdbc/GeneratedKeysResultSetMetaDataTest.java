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
import org.mockito.Mockito;

import javax.sql.rowset.RowSetMetaDataImpl;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GeneratedKeysResultSetMetaDataTest {
    
    private ResultSetMetaData actualMetaData;
    
    @Before
    public void init() throws SQLException {
        actualMetaData = new GeneratedKeysResultSet(Arrays.<Number>asList(1L, 2L).iterator(), "order_id", Mockito.mock(Statement.class)).getMetaData();
    }
    
    @Test
    public void getColumnCount() throws Exception {
        assertThat(actualMetaData.getColumnCount(), is(1));
    }
    
    @Test
    public void isAutoIncrement() throws Exception {
        assertTrue(actualMetaData.isAutoIncrement(1));
    }
    
    @Test
    public void isCaseSensitive() throws Exception {
        assertTrue(actualMetaData.isCaseSensitive(1));
    }
    
    @Test
    public void isSearchable() throws Exception {
        assertFalse(actualMetaData.isSearchable(1));
    }
    
    @Test
    public void isCurrency() throws Exception {
        assertFalse(actualMetaData.isCurrency(1));
    }
    
    @Test
    public void isNullable() throws Exception {
        assertThat(actualMetaData.isNullable(1), is(ResultSetMetaData.columnNoNulls));
    }
    
    @Test
    public void isSigned() throws Exception {
        assertTrue(actualMetaData.isSigned(1));
    }
    
    @Test
    public void getColumnDisplaySize() throws Exception {
        assertThat(actualMetaData.getColumnDisplaySize(1), is(0));
    }
    
    @Test
    public void getColumnLabel() throws Exception {
        assertThat(actualMetaData.getColumnLabel(1), is("order_id"));
    }
    
    @Test
    public void getColumnName() throws Exception {
        assertThat(actualMetaData.getColumnName(1), is("order_id"));
    }
    
    @Test
    public void getSchemaName() throws Exception {
        assertThat(actualMetaData.getSchemaName(1), is(""));
    }
    
    @Test
    public void getPrecision() throws Exception {
        assertThat(actualMetaData.getPrecision(1), is(0));
    }
    
    @Test
    public void getScale() throws Exception {
        assertThat(actualMetaData.getScale(1), is(0));
    }
    
    @Test
    public void getTableName() throws Exception {
        assertThat(actualMetaData.getTableName(1), is(""));
    }
    
    @Test
    public void getCatalogName() throws Exception {
        assertThat(actualMetaData.getCatalogName(1), is(""));
    }
    
    @Test
    public void getColumnType() throws Exception {
        assertThat(actualMetaData.getColumnType(1), is(Types.BIGINT));
    }
    
    @Test
    public void getColumnTypeName() throws Exception {
        assertThat(actualMetaData.getColumnTypeName(1), is(""));
    }
    
    @Test
    public void isReadOnly() throws Exception {
        assertTrue(actualMetaData.isReadOnly(1));
    }
    
    @Test
    public void isWritable() throws Exception {
        assertFalse(actualMetaData.isWritable(1));
    }
    
    @Test
    public void isDefinitelyWritable() throws Exception {
        assertFalse(actualMetaData.isDefinitelyWritable(1));
    }
    
    @Test
    public void getColumnClassName() throws Exception {
        assertThat(actualMetaData.getColumnClassName(1), is("java.lang.Number"));
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
