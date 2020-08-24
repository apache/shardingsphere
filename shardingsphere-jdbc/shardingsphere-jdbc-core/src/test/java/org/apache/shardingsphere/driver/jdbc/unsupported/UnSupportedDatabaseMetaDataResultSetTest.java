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
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBigDecimalWithIndex() throws SQLException {
        databaseMetaDataResultSet.getBigDecimal(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBigDecimalWithLabel() throws SQLException {
        databaseMetaDataResultSet.getBigDecimal("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBigDecimalWithIndexAndScale() throws SQLException {
        databaseMetaDataResultSet.getBigDecimal(1, 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBigDecimalWithLabelAndScale() throws SQLException {
        databaseMetaDataResultSet.getBigDecimal("", 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetAsciiStreamWithIndex() throws SQLException {
        databaseMetaDataResultSet.getAsciiStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetAsciiStreamWithLabel() throws SQLException {
        databaseMetaDataResultSet.getAsciiStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetUnicodeStreamWithIndex() throws SQLException {
        databaseMetaDataResultSet.getUnicodeStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetUnicodeStreamWithLabel() throws SQLException {
        databaseMetaDataResultSet.getUnicodeStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBinaryStreamWithIndex() throws SQLException {
        databaseMetaDataResultSet.getBinaryStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBinaryStreamWithLabel() throws SQLException {
        databaseMetaDataResultSet.getBinaryStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetWarnings() throws SQLException {
        databaseMetaDataResultSet.getWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertClearWarnings() throws SQLException {
        databaseMetaDataResultSet.clearWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCharacterStreamWithIndex() throws SQLException {
        databaseMetaDataResultSet.getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCharacterStreamWithLabel() throws SQLException {
        databaseMetaDataResultSet.getCharacterStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetStatement() throws SQLException {
        databaseMetaDataResultSet.getStatement();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBlobWithIndex() throws SQLException {
        databaseMetaDataResultSet.getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBlobWithLabel() throws SQLException {
        databaseMetaDataResultSet.getBlob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClobWithIndex() throws SQLException {
        databaseMetaDataResultSet.getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClobWithLabel() throws SQLException {
        databaseMetaDataResultSet.getClob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetDateWithIndexAndCalendar() throws SQLException {
        databaseMetaDataResultSet.getDate(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetDateWithLabelAndCalendar() throws SQLException {
        databaseMetaDataResultSet.getDate("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTimeWithIndexAndCalendar() throws SQLException {
        databaseMetaDataResultSet.getTime(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTimeWithLabelAndCalendar() throws SQLException {
        databaseMetaDataResultSet.getTime("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTimestampWithIndexAndCalendar() throws SQLException {
        databaseMetaDataResultSet.getTimestamp(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTimestampWithLabelAndCalendar() throws SQLException {
        databaseMetaDataResultSet.getTimestamp("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetURLWithIndex() throws SQLException {
        databaseMetaDataResultSet.getURL(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetURLWithLabel() throws SQLException {
        databaseMetaDataResultSet.getURL("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSQLXMLWithIndex() throws SQLException {
        databaseMetaDataResultSet.getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSQLXMLWithLabel() throws SQLException {
        databaseMetaDataResultSet.getSQLXML("");
    }
}
