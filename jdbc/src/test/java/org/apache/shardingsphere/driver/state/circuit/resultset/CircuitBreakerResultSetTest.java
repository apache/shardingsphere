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

package org.apache.shardingsphere.driver.state.circuit.resultset;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
class CircuitBreakerResultSetTest {
    
    @Test
    void assertNext() {
        assertFalse(new CircuitBreakerResultSet().next());
    }
    
    @Test
    void assertClose() {
        assertDoesNotThrow(() -> new CircuitBreakerResultSet().close());
    }
    
    @Test
    void assertWasNull() {
        assertTrue(new CircuitBreakerResultSet().wasNull());
    }
    
    @Test
    void assertGetStringByIndex() {
        assertThat(new CircuitBreakerResultSet().getString(1), is(""));
    }
    
    @Test
    void assertGetStringByLabel() {
        assertThat(new CircuitBreakerResultSet().getString("foo"), is(""));
    }
    
    @Test
    void assertGetNStringByIndex() {
        assertThat(new CircuitBreakerResultSet().getNString(1), is(""));
    }
    
    @Test
    void assertGetNStringByLabel() {
        assertThat(new CircuitBreakerResultSet().getNString("foo"), is(""));
    }
    
    @Test
    void assertGetBooleanByIndex() {
        assertFalse(new CircuitBreakerResultSet().getBoolean(1));
    }
    
    @Test
    void assertGetBooleanByLabel() {
        assertFalse(new CircuitBreakerResultSet().getBoolean("foo"));
    }
    
    @Test
    void assertGetByteByIndex() {
        assertThat(new CircuitBreakerResultSet().getByte(1), is((byte) 0));
    }
    
    @Test
    void assertGetByteByLabel() {
        assertThat(new CircuitBreakerResultSet().getByte("foo"), is((byte) 0));
    }
    
    @Test
    void assertGetShortByIndex() {
        assertThat(new CircuitBreakerResultSet().getShort(1), is((short) 0));
    }
    
    @Test
    void assertGetShortByLabel() {
        assertThat(new CircuitBreakerResultSet().getShort("foo"), is((short) 0));
    }
    
    @Test
    void assertGetIntByIndex() {
        assertThat(new CircuitBreakerResultSet().getInt(1), is(0));
    }
    
    @Test
    void assertGetIntByLabel() {
        assertThat(new CircuitBreakerResultSet().getInt("foo"), is(0));
    }
    
    @Test
    void assertGetLongByIndex() {
        assertThat(new CircuitBreakerResultSet().getLong(1), is(0L));
    }
    
    @Test
    void assertGetLongByLabel() {
        assertThat(new CircuitBreakerResultSet().getLong("foo"), is(0L));
    }
    
    @Test
    void assertGetFloatByIndex() {
        assertThat(new CircuitBreakerResultSet().getFloat(1), is(0F));
    }
    
    @Test
    void assertGetFloatByLabel() {
        assertThat(new CircuitBreakerResultSet().getFloat("foo"), is(0F));
    }
    
    @Test
    void assertGetDoubleByIndex() {
        assertThat(new CircuitBreakerResultSet().getDouble(1), is(0D));
    }
    
    @Test
    void assertGetDoubleByLabel() {
        assertThat(new CircuitBreakerResultSet().getDouble("foo"), is(0D));
    }
    
    @Test
    void assertGetBytesByIndex() {
        assertThat(new CircuitBreakerResultSet().getBytes(1).length, is(0));
    }
    
    @Test
    void assertGetBytesByLabel() {
        assertThat(new CircuitBreakerResultSet().getBytes("foo").length, is(0));
    }
    
    @Test
    void assertGetDateByIndex() {
        assertNull(new CircuitBreakerResultSet().getDate(1));
    }
    
    @Test
    void assertGetDateByLabel() {
        assertNull(new CircuitBreakerResultSet().getDate("foo"));
    }
    
    @Test
    void assertGetDateWithCalendarByIndex() {
        assertNull(new CircuitBreakerResultSet().getDate(1, Calendar.getInstance()));
    }
    
    @Test
    void assertGetDateWithCalendarByLabel() {
        assertNull(new CircuitBreakerResultSet().getDate("foo", Calendar.getInstance()));
    }
    
    @Test
    void assertGetTimeByIndex() {
        assertNull(new CircuitBreakerResultSet().getTime(1));
    }
    
    @Test
    void assertGetTimeByLabel() {
        assertNull(new CircuitBreakerResultSet().getTime("foo"));
    }
    
    @Test
    void assertGetTimeWithCalendarByIndex() {
        assertNull(new CircuitBreakerResultSet().getTime(1, Calendar.getInstance()));
    }
    
    @Test
    void assertGetTimeWithCalendarByLabel() {
        assertNull(new CircuitBreakerResultSet().getTime("foo", Calendar.getInstance()));
    }
    
    @Test
    void assertGetTimestampByIndex() {
        assertNull(new CircuitBreakerResultSet().getTimestamp(1));
    }
    
    @Test
    void assertGetTimestampByLabel() {
        assertNull(new CircuitBreakerResultSet().getTimestamp("foo"));
    }
    
    @Test
    void assertGetTimestampWithCalendarByIndex() {
        assertNull(new CircuitBreakerResultSet().getTimestamp(1, Calendar.getInstance()));
    }
    
    @Test
    void assertGetTimestampWithCalendarByLabel() {
        assertNull(new CircuitBreakerResultSet().getTimestamp("foo", Calendar.getInstance()));
    }
    
    @Test
    void assertGetAsciiStreamByIndex() {
        assertNull(new CircuitBreakerResultSet().getAsciiStream(1));
    }
    
    @Test
    void assertGetAsciiStreamByLabel() {
        assertNull(new CircuitBreakerResultSet().getAsciiStream("foo"));
    }
    
    @Test
    void assertGetUnicodeStreamByIndex() {
        assertNull(new CircuitBreakerResultSet().getUnicodeStream(1));
    }
    
    @Test
    void assertGetUnicodeStreamByLabel() {
        assertNull(new CircuitBreakerResultSet().getUnicodeStream("foo"));
    }
    
    @Test
    void assertGetBinaryStreamByIndex() {
        assertNull(new CircuitBreakerResultSet().getBinaryStream(1));
    }
    
    @Test
    void assertGetBinaryStreamByLabel() {
        assertNull(new CircuitBreakerResultSet().getBinaryStream("foo"));
    }
    
    @Test
    void assertGetWarnings() {
        assertNull(new CircuitBreakerResultSet().getWarnings());
    }
    
    @Test
    void assertClearWarnings() {
        assertDoesNotThrow(() -> new CircuitBreakerResultSet().clearWarnings());
    }
    
    @Test
    void assertGetMetaData() {
        assertThat(new CircuitBreakerResultSet().getMetaData(), isA(CircuitBreakerResultSetMetaData.class));
    }
    
    @Test
    void assertGetObjectByIndex() {
        assertNull(new CircuitBreakerResultSet().getObject(1));
    }
    
    @Test
    void assertGetObjectByLabel() {
        assertNull(new CircuitBreakerResultSet().getObject("foo"));
    }
    
    @Test
    void assertFindColumn() {
        assertThat(new CircuitBreakerResultSet().findColumn("foo"), is(0));
    }
    
    @Test
    void assertGetCharacterStreamByIndex() {
        assertNull(new CircuitBreakerResultSet().getCharacterStream(1));
    }
    
    @Test
    void assertGetCharacterStreamByLabel() {
        assertNull(new CircuitBreakerResultSet().getCharacterStream("foo"));
    }
    
    @Test
    void assertGetBigDecimalByIndex() {
        assertNull(new CircuitBreakerResultSet().getBigDecimal(1));
    }
    
    @Test
    void assertGetBigDecimalByLabel() {
        assertNull(new CircuitBreakerResultSet().getBigDecimal("foo"));
    }
    
    @Test
    void assertGetBigDecimalWithScaleByIndex() {
        assertNull(new CircuitBreakerResultSet().getBigDecimal(1, 1));
    }
    
    @Test
    void assertGetBigDecimalWithScaleByLabel() {
        assertNull(new CircuitBreakerResultSet().getBigDecimal("foo", 1));
    }
    
    @Test
    void assertSetFetchDirection() {
        assertDoesNotThrow(() -> new CircuitBreakerResultSet().setFetchDirection(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void assertGetFetchDirection() {
        assertThat(new CircuitBreakerResultSet().getFetchDirection(), is(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void assertSetFetchSize() {
        assertDoesNotThrow(() -> new CircuitBreakerResultSet().setFetchSize(10));
    }
    
    @Test
    void assertGetFetchSize() {
        assertThat(new CircuitBreakerResultSet().getFetchSize(), is(0));
    }
    
    @Test
    void assertGetType() {
        assertThat(new CircuitBreakerResultSet().getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertGetConcurrency() {
        assertThat(new CircuitBreakerResultSet().getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    void assertGetStatement() {
        assertNull(new CircuitBreakerResultSet().getStatement());
    }
    
    @Test
    void assertGetArrayByIndex() {
        assertNull(new CircuitBreakerResultSet().getArray(1));
    }
    
    @Test
    void assertGetArrayByLabel() {
        assertNull(new CircuitBreakerResultSet().getArray("foo"));
    }
    
    @Test
    void assertGetBlobByIndex() {
        assertNull(new CircuitBreakerResultSet().getBlob(1));
    }
    
    @Test
    void assertGetBlobByLabel() {
        assertNull(new CircuitBreakerResultSet().getBlob("foo"));
    }
    
    @Test
    void assertGetClobByIndex() {
        assertNull(new CircuitBreakerResultSet().getClob(1));
    }
    
    @Test
    void assertGetClobByLabel() {
        assertNull(new CircuitBreakerResultSet().getClob("foo"));
    }
    
    @Test
    void assertGetURLByIndex() {
        assertNull(new CircuitBreakerResultSet().getURL(1));
    }
    
    @Test
    void assertGetURLByLabel() {
        assertNull(new CircuitBreakerResultSet().getURL("foo"));
    }
    
    @Test
    void assertIsClosed() {
        assertFalse(new CircuitBreakerResultSet().isClosed());
    }
    
    @Test
    void assertGetSQLXMLByIndex() {
        assertNull(new CircuitBreakerResultSet().getSQLXML(1));
    }
    
    @Test
    void assertGetSQLXMLByLabel() {
        assertNull(new CircuitBreakerResultSet().getSQLXML("foo"));
    }
}
