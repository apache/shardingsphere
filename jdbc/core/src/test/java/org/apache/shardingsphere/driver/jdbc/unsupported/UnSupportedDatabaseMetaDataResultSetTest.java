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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnSupportedDatabaseMetaDataResultSetTest {
    
    private DatabaseMetaDataResultSet databaseMetaDataResultSet;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        databaseMetaDataResultSet = new DatabaseMetaDataResultSet(resultSet, null);
    }
    
    @Test
    public void assertGetAsciiStreamWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getAsciiStream(1));
    }
    
    @Test
    public void assertGetAsciiStreamWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getAsciiStream(""));
    }
    
    @Test
    public void assertGetUnicodeStreamWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getUnicodeStream(1));
    }
    
    @Test
    public void assertGetUnicodeStreamWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getUnicodeStream(""));
    }
    
    @Test
    public void assertGetBinaryStreamWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getBinaryStream(1));
    }
    
    @Test
    public void assertGetBinaryStreamWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getBinaryStream(""));
    }
    
    @Test
    public void assertGetWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getWarnings());
    }
    
    @Test
    public void assertClearWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.clearWarnings());
    }
    
    @Test
    public void assertGetCharacterStreamWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getCharacterStream(1));
    }
    
    @Test
    public void assertGetCharacterStreamWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getCharacterStream(""));
    }
    
    @Test
    public void assertGetBlobWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getBlob(1));
    }
    
    @Test
    public void assertGetBlobWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getBlob(""));
    }
    
    @Test
    public void assertGetClobWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getClob(1));
    }
    
    @Test
    public void assertGetClobWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getClob(""));
    }
    
    @Test
    public void assertGetDateWithIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getDate(1, null));
    }
    
    @Test
    public void assertGetDateWithLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getDate("", null));
    }
    
    @Test
    public void assertGetTimeWithIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getTime(1, null));
    }
    
    @Test
    public void assertGetTimeWithLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getTime("", null));
    }
    
    @Test
    public void assertGetTimestampWithIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getTimestamp(1, null));
    }
    
    @Test
    public void assertGetTimestampWithLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getTimestamp("", null));
    }
    
    @Test
    public void assertGetSQLXMLWithIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getSQLXML(1));
    }
    
    @Test
    public void assertGetSQLXMLWithLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> databaseMetaDataResultSet.getSQLXML(""));
    }
}
