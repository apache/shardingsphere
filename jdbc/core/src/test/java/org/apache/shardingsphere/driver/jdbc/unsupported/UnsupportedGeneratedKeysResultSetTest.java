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
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class UnsupportedGeneratedKeysResultSetTest {
    
    @Test
    public void getBooleanWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBoolean(1));
    }
    
    @Test
    public void getBooleanWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBoolean(""));
    }
    
    @Test
    public void getDateWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate(1));
    }
    
    @Test
    public void getDateWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate(1, null));
    }
    
    @Test
    public void getDateWithWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate(""));
    }
    
    @Test
    public void getDateWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate("", null));
    }
    
    @Test
    public void getTimeWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime(1));
    }
    
    @Test
    public void getTimeWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime(1, null));
    }
    
    @Test
    public void getTimeWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime(""));
    }
    
    @Test
    public void getTimeWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime("", null));
    }
    
    @Test
    public void getTimestampWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp(1));
    }
    
    @Test
    public void getTimestampWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp(1, null));
    }
    
    @Test
    public void getTimestampWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp(""));
    }
    
    @Test
    public void getTimestampWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp("", null));
    }
    
    @Test
    public void getAsciiStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getAsciiStream(1));
    }
    
    @Test
    public void getAsciiStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getAsciiStream(""));
    }
    
    @Test
    public void getUnicodeStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getUnicodeStream(1));
    }
    
    @Test
    public void getUnicodeStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getUnicodeStream(""));
    }
    
    @Test
    public void getBinaryStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBinaryStream(1));
    }
    
    @Test
    public void getBinaryStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBinaryStream(""));
    }
    
    @Test
    public void getCharacterStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getCharacterStream(1));
    }
    
    @Test
    public void getCharacterStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getCharacterStream(""));
    }
    
    @Test
    public void getBlobWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBlob(1));
    }
    
    @Test
    public void getBlobWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBlob(""));
    }
    
    @Test
    public void getClobWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getClob(1));
    }
    
    @Test
    public void getClobWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getClob(""));
    }
    
    @Test
    public void getURLWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getURL(1));
    }
    
    @Test
    public void getURLWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getURL(""));
    }
    
    @Test
    public void getSQLXMLWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getSQLXML(1));
    }
    
    @Test
    public void getSQLXMLWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getSQLXML(""));
    }
    
    @Test
    public void getObjectWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getObject(1, Collections.emptyMap()));
    }
    
    @Test
    public void getObjectWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getObject("", Collections.emptyMap()));
    }
    
    @Test
    public void setFetchDirection() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().setFetchDirection(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    public void getFetchDirection() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getFetchDirection());
    }
    
    @Test
    public void setFetchSize() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().setFetchSize(1));
    }
    
    @Test
    public void getFetchSize() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getFetchSize());
    }
    
    @Test
    public void getWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getWarnings());
    }
    
    @Test
    public void clearWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().clearWarnings());
    }
}
