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
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UnsupportedGeneratedKeysResultSetTest {
    
    @Test
    void getBooleanWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBoolean(1));
    }
    
    @Test
    void getBooleanWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBoolean(""));
    }
    
    @Test
    void getDateWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate(1));
    }
    
    @Test
    void getDateWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate(1, null));
    }
    
    @Test
    void getDateWithWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate(""));
    }
    
    @Test
    void getDateWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getDate("", null));
    }
    
    @Test
    void getTimeWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime(1));
    }
    
    @Test
    void getTimeWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime(1, null));
    }
    
    @Test
    void getTimeWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime(""));
    }
    
    @Test
    void getTimeWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTime("", null));
    }
    
    @Test
    void getTimestampWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp(1));
    }
    
    @Test
    void getTimestampWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp(1, null));
    }
    
    @Test
    void getTimestampWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp(""));
    }
    
    @Test
    void getTimestampWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getTimestamp("", null));
    }
    
    @Test
    void getAsciiStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getAsciiStream(1));
    }
    
    @Test
    void getAsciiStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getAsciiStream(""));
    }
    
    @Test
    void getUnicodeStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getUnicodeStream(1));
    }
    
    @Test
    void getUnicodeStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getUnicodeStream(""));
    }
    
    @Test
    void getBinaryStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBinaryStream(1));
    }
    
    @Test
    void getBinaryStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBinaryStream(""));
    }
    
    @Test
    void getCharacterStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getCharacterStream(1));
    }
    
    @Test
    void getCharacterStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getCharacterStream(""));
    }
    
    @Test
    void getBlobWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBlob(1));
    }
    
    @Test
    void getBlobWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getBlob(""));
    }
    
    @Test
    void getClobWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getClob(1));
    }
    
    @Test
    void getClobWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getClob(""));
    }
    
    @Test
    void getURLWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getURL(1));
    }
    
    @Test
    void getURLWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getURL(""));
    }
    
    @Test
    void getSQLXMLWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getSQLXML(1));
    }
    
    @Test
    void getSQLXMLWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getSQLXML(""));
    }
    
    @Test
    void getObjectWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getObject(1, Collections.emptyMap()));
    }
    
    @Test
    void getObjectWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getObject("", Collections.emptyMap()));
    }
    
    @Test
    void setFetchDirection() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().setFetchDirection(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void getFetchDirection() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getFetchDirection());
    }
    
    @Test
    void setFetchSize() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().setFetchSize(1));
    }
    
    @Test
    void getFetchSize() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getFetchSize());
    }
    
    @Test
    void getWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().getWarnings());
    }
    
    @Test
    void clearWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new GeneratedKeysResultSet().clearWarnings());
    }
}
