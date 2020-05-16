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

import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;

public final class UnsupportedGeneratedKeysResultSetTest {
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBooleanWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getBoolean(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBooleanWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getBoolean("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDateWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getDate(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDateWithColumnIndexAndCalendar() throws SQLException {
        new GeneratedKeysResultSet().getDate(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDateWithWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getDate("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDateWithColumnLabelAndCalendar() throws SQLException {
        new GeneratedKeysResultSet().getDate("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimeWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getTime(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimeWithColumnIndexAndCalendar() throws SQLException {
        new GeneratedKeysResultSet().getTime(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimeWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getTime("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimeWithColumnLabelAndCalendar() throws SQLException {
        new GeneratedKeysResultSet().getTime("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestampWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestampWithColumnIndexAndCalendar() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestampWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestampWithColumnLabelAndCalendar() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getAsciiStreamWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getAsciiStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getAsciiStreamWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getAsciiStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getUnicodeStreamWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getUnicodeStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getUnicodeStreamWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getUnicodeStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBinaryStreamWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getBinaryStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBinaryStreamWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getBinaryStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getCharacterStreamWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getCharacterStreamWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getCharacterStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBlobWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBlobWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getBlob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getClobWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getClobWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getClob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getURLWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getURL(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getURLWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getURL("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXMLWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXMLWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getSQLXML("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObjectWithColumnIndex() throws SQLException {
        new GeneratedKeysResultSet().getObject(1, new HashMap<>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObjectWithColumnLabel() throws SQLException {
        new GeneratedKeysResultSet().getObject("", new HashMap<>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setFetchDirection() throws SQLException {
        new GeneratedKeysResultSet().setFetchDirection(ResultSet.FETCH_FORWARD);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getFetchDirection() throws SQLException {
        new GeneratedKeysResultSet().getFetchDirection();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setFetchSize() throws SQLException {
        new GeneratedKeysResultSet().setFetchSize(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getFetchSize() throws SQLException {
        new GeneratedKeysResultSet().getFetchSize();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getWarnings() throws SQLException {
        new GeneratedKeysResultSet().getWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void clearWarnings() throws SQLException {
        new GeneratedKeysResultSet().clearWarnings();
    }
}
