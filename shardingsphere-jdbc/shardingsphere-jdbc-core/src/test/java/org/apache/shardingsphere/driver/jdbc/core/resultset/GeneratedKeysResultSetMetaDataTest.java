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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.junit.Before;
import org.junit.Test;

import javax.sql.rowset.RowSetMetaDataImpl;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class GeneratedKeysResultSetMetaDataTest {
    
    private ResultSetMetaData actualMetaData;
    
    @Before
    public void init() {
        actualMetaData = new GeneratedKeysResultSet("order_id", Arrays.<Comparable<?>>asList(1L, 2L).iterator(), mock(Statement.class)).getMetaData();
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(1));
    }
    
    @Test
    public void assertIsAutoIncrement() throws SQLException {
        assertTrue(actualMetaData.isAutoIncrement(1));
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        assertTrue(actualMetaData.isCaseSensitive(1));
    }
    
    @Test
    public void assertIsSearchable() throws SQLException {
        assertFalse(actualMetaData.isSearchable(1));
    }
    
    @Test
    public void assertIsCurrency() throws SQLException {
        assertFalse(actualMetaData.isCurrency(1));
    }
    
    @Test
    public void assertIsNullable() throws SQLException {
        assertThat(actualMetaData.isNullable(1), is(ResultSetMetaData.columnNoNulls));
    }
    
    @Test
    public void assertIsSigned() throws SQLException {
        assertTrue(actualMetaData.isSigned(1));
    }
    
    @Test
    public void assertGetColumnDisplaySize() throws SQLException {
        assertThat(actualMetaData.getColumnDisplaySize(1), is(0));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        assertThat(actualMetaData.getColumnLabel(1), is("order_id"));
    }
    
    @Test
    public void assertGetColumnName() throws SQLException {
        assertThat(actualMetaData.getColumnName(1), is("order_id"));
    }
    
    @Test
    public void getSchemaName() throws SQLException {
        assertThat(actualMetaData.getSchemaName(1), is(""));
    }
    
    @Test
    public void assertGetPrecision() throws SQLException {
        assertThat(actualMetaData.getPrecision(1), is(0));
    }
    
    @Test
    public void assertGetScale() throws SQLException {
        assertThat(actualMetaData.getScale(1), is(0));
    }
    
    @Test
    public void assertGetTableName() throws SQLException {
        assertThat(actualMetaData.getTableName(1), is(""));
    }
    
    @Test
    public void assertGetCatalogName() throws SQLException {
        assertThat(actualMetaData.getCatalogName(1), is(""));
    }
    
    @Test
    public void assertGetColumnType() throws SQLException {
        assertThat(actualMetaData.getColumnType(1), is(Types.BIGINT));
    }
    
    @Test
    public void assertGetColumnTypeName() throws SQLException {
        assertThat(actualMetaData.getColumnTypeName(1), is(""));
    }
    
    @Test
    public void assertIsReadOnly() throws SQLException {
        assertTrue(actualMetaData.isReadOnly(1));
    }
    
    @Test
    public void assertIsWritable() throws SQLException {
        assertFalse(actualMetaData.isWritable(1));
    }
    
    @Test
    public void assertIsDefinitelyWritable() throws SQLException {
        assertFalse(actualMetaData.isDefinitelyWritable(1));
    }
    
    @Test
    public void assertGetColumnClassName() throws SQLException {
        assertThat(actualMetaData.getColumnClassName(1), is("java.lang.Number"));
    }
    
    @Test
    public void assertUnwrap() throws SQLException {
        assertThat(actualMetaData.unwrap(GeneratedKeysResultSetMetaData.class), is((GeneratedKeysResultSetMetaData) actualMetaData));
    }
    
    @Test(expected = SQLException.class)
    public void assertUnwrapError() throws SQLException {
        actualMetaData.unwrap(RowSetMetaDataImpl.class);
    }
    
    @Test
    public void assertIsWrapperFor() throws SQLException {
        assertTrue(actualMetaData.isWrapperFor(GeneratedKeysResultSetMetaData.class));
    }
}
