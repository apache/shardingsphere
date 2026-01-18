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

package org.apache.shardingsphere.driver.executor.engine.distsql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DistSQLResultSetMetaDataTest {
    
    private DistSQLResultSetMetaData metaData;
    
    @BeforeEach
    void setUp() {
        List<String> columnNames = Arrays.asList("name", "value", "description");
        metaData = new DistSQLResultSetMetaData(columnNames);
    }
    
    @Test
    void assertGetColumnCount() {
        assertThat(metaData.getColumnCount(), is(3));
    }
    
    @Test
    void assertIsAutoIncrement() {
        assertThat(metaData.isAutoIncrement(1), is(false));
    }
    
    @Test
    void assertIsCaseSensitive() {
        assertThat(metaData.isCaseSensitive(1), is(true));
    }
    
    @Test
    void assertIsSearchable() {
        assertThat(metaData.isSearchable(1), is(false));
    }
    
    @Test
    void assertIsCurrency() {
        assertThat(metaData.isCurrency(1), is(false));
    }
    
    @Test
    void assertIsNullable() {
        assertThat(metaData.isNullable(1), is(ResultSetMetaData.columnNullable));
    }
    
    @Test
    void assertIsSigned() {
        assertThat(metaData.isSigned(1), is(false));
    }
    
    @Test
    void assertGetColumnDisplaySize() {
        assertThat(metaData.getColumnDisplaySize(1), is(255));
    }
    
    @Test
    void assertGetColumnLabel() {
        assertThat(metaData.getColumnLabel(1), is("name"));
        assertThat(metaData.getColumnLabel(2), is("value"));
        assertThat(metaData.getColumnLabel(3), is("description"));
    }
    
    @Test
    void assertGetColumnName() {
        assertThat(metaData.getColumnName(1), is("name"));
        assertThat(metaData.getColumnName(2), is("value"));
        assertThat(metaData.getColumnName(3), is("description"));
    }
    
    @Test
    void assertGetSchemaName() {
        assertThat(metaData.getSchemaName(1), is(""));
    }
    
    @Test
    void assertGetPrecision() {
        assertThat(metaData.getPrecision(1), is(0));
    }
    
    @Test
    void assertGetScale() {
        assertThat(metaData.getScale(1), is(0));
    }
    
    @Test
    void assertGetTableName() {
        assertThat(metaData.getTableName(1), is(""));
    }
    
    @Test
    void assertGetCatalogName() {
        assertThat(metaData.getCatalogName(1), is(""));
    }
    
    @Test
    void assertGetColumnType() {
        assertThat(metaData.getColumnType(1), is(Types.CHAR));
    }
    
    @Test
    void assertGetColumnTypeName() {
        assertThat(metaData.getColumnTypeName(1), is("CHAR"));
    }
    
    @Test
    void assertIsReadOnly() {
        assertThat(metaData.isReadOnly(1), is(true));
    }
    
    @Test
    void assertIsWritable() {
        assertThat(metaData.isWritable(1), is(false));
    }
    
    @Test
    void assertIsDefinitelyWritable() {
        assertThat(metaData.isDefinitelyWritable(1), is(false));
    }
    
    @Test
    void assertGetColumnClassName() {
        assertThat(metaData.getColumnClassName(1), is(String.class.getName()));
    }
    
    @Test
    void assertColumnIndexOutOfRangeLow() {
        assertThrows(IllegalArgumentException.class, () -> metaData.getColumnName(0));
    }
    
    @Test
    void assertColumnIndexOutOfRangeHigh() {
        assertThrows(IllegalArgumentException.class, () -> metaData.getColumnName(4));
    }
    
    @Test
    void assertUnwrap() throws SQLException {
        assertThat(metaData.unwrap(DistSQLResultSetMetaData.class), is(metaData));
    }
    
    @Test
    void assertIsWrapperFor() {
        assertThat(metaData.isWrapperFor(DistSQLResultSetMetaData.class), is(true));
    }
}
