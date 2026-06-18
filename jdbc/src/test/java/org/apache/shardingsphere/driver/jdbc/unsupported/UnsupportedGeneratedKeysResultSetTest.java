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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UnsupportedGeneratedKeysResultSetTest {
    
    private GeneratedKeysResultSet actual;
    
    @BeforeEach
    void setUp() {
        actual = new GeneratedKeysResultSet();
    }
    
    @AfterEach
    void tearDown() {
        actual.close();
    }
    
    @Test
    void assertGetBooleanWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getBoolean(1));
    }
    
    @Test
    void assertGetBooleanWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getBoolean(""));
    }
    
    @Test
    void assertGetDateWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getDate(1));
    }
    
    @Test
    void assertGetDateWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getDate(1, null));
    }
    
    @Test
    void assertGetDateWithWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getDate(""));
    }
    
    @Test
    void assertGetDateWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getDate("", null));
    }
    
    @Test
    void assertGetTimeWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTime(1));
    }
    
    @Test
    void assertGetTimeWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTime(1, null));
    }
    
    @Test
    void assertGetTimeWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTime(""));
    }
    
    @Test
    void assertGetTimeWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTime("", null));
    }
    
    @Test
    void assertGetTimestampWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTimestamp(1));
    }
    
    @Test
    void assertGetTimestampWithColumnIndexAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTimestamp(1, null));
    }
    
    @Test
    void assertGetTimestampWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTimestamp(""));
    }
    
    @Test
    void assertGetTimestampWithColumnLabelAndCalendar() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getTimestamp("", null));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getAsciiStream(1));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getAsciiStream(""));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getUnicodeStream(1));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getUnicodeStream(""));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getBinaryStream(1));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getBinaryStream(""));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getCharacterStream(1));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getCharacterStream(""));
    }
    
    @Test
    void assertGetBlobWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getBlob(1));
    }
    
    @Test
    void assertGetBlobWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getBlob(""));
    }
    
    @Test
    void assertGetClobWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getClob(1));
    }
    
    @Test
    void assertGetClobWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getClob(""));
    }
    
    @Test
    void assertGetURLWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getURL(1));
    }
    
    @Test
    void assertGetURLWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getURL(""));
    }
    
    @Test
    void assertGetSQLXMLWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getSQLXML(1));
    }
    
    @Test
    void assertGetSQLXMLWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getSQLXML(""));
    }
    
    @Test
    void assertGetObjectWithColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getObject(1, Collections.emptyMap()));
    }
    
    @Test
    void assertGetObjectWithColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getObject("", Collections.emptyMap()));
    }
    
    @Test
    void assertSetFetchDirection() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.setFetchDirection(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void assertGetFetchDirection() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getFetchDirection());
    }
    
    @Test
    void assertSetFetchSize() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.setFetchSize(1));
    }
    
    @Test
    void assertGetFetchSize() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getFetchSize());
    }
    
    @Test
    void assertGetWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.getWarnings());
    }
    
    @Test
    void assertClearWarnings() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> actual.clearWarnings());
    }
}
