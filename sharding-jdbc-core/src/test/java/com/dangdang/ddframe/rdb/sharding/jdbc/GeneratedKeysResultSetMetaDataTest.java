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
    
    private ResultSetMetaData acutalMetaData;
    
    @Before
    public void init() throws SQLException {
        acutalMetaData = GeneratedKeysResultSetTest.createMock().getMetaData();
    }
    
    @Test
    public void getColumnCount() throws Exception {
        assertThat(acutalMetaData.getColumnCount(), is(2));
    }
    
    @Test
    public void isAutoIncrement() throws Exception {
        assertTrue(acutalMetaData.isAutoIncrement(1));
        assertTrue(acutalMetaData.isAutoIncrement(2));
    }
    
    @Test
    public void isCaseSensitive() throws Exception {
        assertTrue(acutalMetaData.isCaseSensitive(1));
        assertTrue(acutalMetaData.isCaseSensitive(2));
    }
    
    @Test
    public void isSearchable() throws Exception {
        assertFalse(acutalMetaData.isSearchable(1));
        assertFalse(acutalMetaData.isSearchable(2));
    }
    
    @Test
    public void isCurrency() throws Exception {
        assertFalse(acutalMetaData.isCurrency(1));
        assertFalse(acutalMetaData.isCurrency(2));
    }
    
    @Test
    public void isNullable() throws Exception {
        assertEquals(acutalMetaData.isNullable(1), ResultSetMetaData.columnNoNulls);
        assertEquals(acutalMetaData.isNullable(2), ResultSetMetaData.columnNoNulls);
    }
    
    @Test
    public void isSigned() throws Exception {
        assertTrue(acutalMetaData.isSigned(1));
        assertTrue(acutalMetaData.isSigned(2));
    }
    
    @Test
    public void getColumnDisplaySize() throws Exception {
        assertEquals(acutalMetaData.getColumnDisplaySize(1), 0);
        assertEquals(acutalMetaData.getColumnDisplaySize(2), 0);
    }
    
    @Test
    public void getColumnLabel() throws Exception {
        assertThat(acutalMetaData.getColumnLabel(1), is("order_id"));
        assertThat(acutalMetaData.getColumnLabel(2), is("order_no"));
    }
    
    @Test
    public void getColumnName() throws Exception {
        assertThat(acutalMetaData.getColumnName(1), is("order_id"));
        assertThat(acutalMetaData.getColumnName(2), is("order_no"));
    }
    
    @Test
    public void getSchemaName() throws Exception {
        assertThat(acutalMetaData.getSchemaName(1), is(""));
        assertThat(acutalMetaData.getSchemaName(2), is(""));
    }
    
    @Test
    public void getPrecision() throws Exception {
        assertEquals(acutalMetaData.getPrecision(1), 0);
        assertEquals(acutalMetaData.getPrecision(2), 0);
    }
    
    @Test
    public void getScale() throws Exception {
        assertEquals(acutalMetaData.getScale(1), 0);
        assertEquals(acutalMetaData.getScale(2), 0);
    }
    
    @Test
    public void getTableName() throws Exception {
        assertThat(acutalMetaData.getTableName(1), is(""));
        assertThat(acutalMetaData.getTableName(2), is(""));
    }
    
    @Test
    public void getCatalogName() throws Exception {
        assertThat(acutalMetaData.getCatalogName(1), is(""));
        assertThat(acutalMetaData.getCatalogName(2), is(""));
    }
    
    @Test
    public void getColumnType() throws Exception {
        assertEquals(acutalMetaData.getColumnType(1), Types.BIGINT);
        assertEquals(acutalMetaData.getColumnType(2), Types.VARCHAR);
    }
    
    @Test
    public void getColumnTypeName() throws Exception {
        assertThat(acutalMetaData.getColumnTypeName(1), is(""));
        assertThat(acutalMetaData.getColumnTypeName(2), is(""));
    }
    
    @Test
    public void isReadOnly() throws Exception {
        assertTrue(acutalMetaData.isReadOnly(1));
        assertTrue(acutalMetaData.isReadOnly(2));
    }
    
    @Test
    public void isWritable() throws Exception {
        assertFalse(acutalMetaData.isWritable(1));
        assertFalse(acutalMetaData.isWritable(2));
    }
    
    @Test
    public void isDefinitelyWritable() throws Exception {
        assertFalse(acutalMetaData.isDefinitelyWritable(1));
        assertFalse(acutalMetaData.isDefinitelyWritable(2));
    }
    
    @Test
    public void getColumnClassName() throws Exception {
        assertThat(acutalMetaData.getColumnClassName(1), is("java.lang.Long"));
        assertThat(acutalMetaData.getColumnClassName(2), is("java.lang.String"));
    }
    
    @Test
    public void unwrap() throws Exception {
        assertThat(acutalMetaData.unwrap(GeneratedKeysResultSetMetaData.class), is(((GeneratedKeysResultSetMetaData) acutalMetaData)));
    }
    
    @Test(expected = SQLException.class)
    public void unwrapError() throws Exception {
        acutalMetaData.unwrap(RowSetMetaDataImpl.class);
    }
    
    @Test
    public void isWrapperFor() throws Exception {
        assertTrue(acutalMetaData.isWrapperFor(GeneratedKeysResultSetMetaData.class));
    }
    
}