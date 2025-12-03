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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("resource")
class CircuitBreakerResultSetTest {
    
    @Test
    void assertNext() {
        assertThat(new CircuitBreakerResultSet().next(), is(false));
    }
    
    @Test
    void assertClose() {
        assertDoesNotThrow(() -> new CircuitBreakerResultSet().close());
    }
    
    @Test
    void assertWasNull() {
        assertThat(new CircuitBreakerResultSet().wasNull(), is(true));
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
        assertThat(new CircuitBreakerResultSet().getBoolean(1), is(false));
    }
    
    @Test
    void assertGetBooleanByLabel() {
        assertThat(new CircuitBreakerResultSet().getBoolean("foo"), is(false));
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
        assertThat(new CircuitBreakerResultSet().getDate(1), is(nullValue()));
    }
    
    @Test
    void assertGetDateByLabel() {
        assertThat(new CircuitBreakerResultSet().getDate("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetDateWithCalendarByIndex() {
        assertThat(new CircuitBreakerResultSet().getDate(1, Calendar.getInstance()), is(nullValue()));
    }
    
    @Test
    void assertGetDateWithCalendarByLabel() {
        assertThat(new CircuitBreakerResultSet().getDate("foo", Calendar.getInstance()), is(nullValue()));
    }
    
    @Test
    void assertGetTimeByIndex() {
        assertThat(new CircuitBreakerResultSet().getTime(1), is(nullValue()));
    }
    
    @Test
    void assertGetTimeByLabel() {
        assertThat(new CircuitBreakerResultSet().getTime("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetTimeWithCalendarByIndex() {
        assertThat(new CircuitBreakerResultSet().getTime(1, Calendar.getInstance()), is(nullValue()));
    }
    
    @Test
    void assertGetTimeWithCalendarByLabel() {
        assertThat(new CircuitBreakerResultSet().getTime("foo", Calendar.getInstance()), is(nullValue()));
    }
    
    @Test
    void assertGetTimestampByIndex() {
        assertThat(new CircuitBreakerResultSet().getTimestamp(1), is(nullValue()));
    }
    
    @Test
    void assertGetTimestampByLabel() {
        assertThat(new CircuitBreakerResultSet().getTimestamp("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetTimestampWithCalendarByIndex() {
        assertThat(new CircuitBreakerResultSet().getTimestamp(1, Calendar.getInstance()), is(nullValue()));
    }
    
    @Test
    void assertGetTimestampWithCalendarByLabel() {
        assertThat(new CircuitBreakerResultSet().getTimestamp("foo", Calendar.getInstance()), is(nullValue()));
    }
    
    @Test
    void assertGetAsciiStreamByIndex() {
        assertThat(new CircuitBreakerResultSet().getAsciiStream(1), is(nullValue()));
    }
    
    @Test
    void assertGetAsciiStreamByLabel() {
        assertThat(new CircuitBreakerResultSet().getAsciiStream("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetUnicodeStreamByIndex() {
        assertThat(new CircuitBreakerResultSet().getUnicodeStream(1), is(nullValue()));
    }
    
    @Test
    void assertGetUnicodeStreamByLabel() {
        assertThat(new CircuitBreakerResultSet().getUnicodeStream("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetBinaryStreamByIndex() {
        assertThat(new CircuitBreakerResultSet().getBinaryStream(1), is(nullValue()));
    }
    
    @Test
    void assertGetBinaryStreamByLabel() {
        assertThat(new CircuitBreakerResultSet().getBinaryStream("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetWarnings() {
        assertThat(new CircuitBreakerResultSet().getWarnings(), is(nullValue()));
    }
    
    @Test
    void assertClearWarnings() {
        assertDoesNotThrow(() -> new CircuitBreakerResultSet().clearWarnings());
    }
    
    @Test
    void assertGetMetaData() {
        assertThat(new CircuitBreakerResultSet().getMetaData(), instanceOf(CircuitBreakerResultSetMetaData.class));
    }
    
    @Test
    void assertGetObjectByIndex() {
        assertThat(new CircuitBreakerResultSet().getObject(1), is(nullValue()));
    }
    
    @Test
    void assertGetObjectByLabel() {
        assertThat(new CircuitBreakerResultSet().getObject("foo"), is(nullValue()));
    }
    
    @Test
    void assertFindColumn() {
        assertThat(new CircuitBreakerResultSet().findColumn("foo"), is(0));
    }
    
    @Test
    void assertGetCharacterStreamByIndex() {
        assertThat(new CircuitBreakerResultSet().getCharacterStream(1), is(nullValue()));
    }
    
    @Test
    void assertGetCharacterStreamByLabel() {
        assertThat(new CircuitBreakerResultSet().getCharacterStream("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetBigDecimalByIndex() {
        assertThat(new CircuitBreakerResultSet().getBigDecimal(1), is(nullValue()));
    }
    
    @Test
    void assertGetBigDecimalByLabel() {
        assertThat(new CircuitBreakerResultSet().getBigDecimal("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetBigDecimalWithScaleByIndex() {
        assertThat(new CircuitBreakerResultSet().getBigDecimal(1, 1), is(nullValue()));
    }
    
    @Test
    void assertGetBigDecimalWithScaleByLabel() {
        assertThat(new CircuitBreakerResultSet().getBigDecimal("foo", 1), is(nullValue()));
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
        assertThat(new CircuitBreakerResultSet().getStatement(), is(nullValue()));
    }
    
    @Test
    void assertGetArrayByIndex() {
        assertThat(new CircuitBreakerResultSet().getArray(1), is(nullValue()));
    }
    
    @Test
    void assertGetArrayByLabel() {
        assertThat(new CircuitBreakerResultSet().getArray("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetBlobByIndex() {
        assertThat(new CircuitBreakerResultSet().getBlob(1), is(nullValue()));
    }
    
    @Test
    void assertGetBlobByLabel() {
        assertThat(new CircuitBreakerResultSet().getBlob("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetClobByIndex() {
        assertThat(new CircuitBreakerResultSet().getClob(1), is(nullValue()));
    }
    
    @Test
    void assertGetClobByLabel() {
        assertThat(new CircuitBreakerResultSet().getClob("foo"), is(nullValue()));
    }
    
    @Test
    void assertGetURLByIndex() {
        assertThat(new CircuitBreakerResultSet().getURL(1), is(nullValue()));
    }
    
    @Test
    void assertGetURLByLabel() {
        assertThat(new CircuitBreakerResultSet().getURL("foo"), is(nullValue()));
    }
    
    @Test
    void assertIsClosed() {
        assertThat(new CircuitBreakerResultSet().isClosed(), is(false));
    }
    
    @Test
    void assertGetSQLXMLByIndex() {
        assertThat(new CircuitBreakerResultSet().getSQLXML(1), is(nullValue()));
    }
    
    @Test
    void assertGetSQLXMLByLabel() {
        assertThat(new CircuitBreakerResultSet().getSQLXML("foo"), is(nullValue()));
    }
}
