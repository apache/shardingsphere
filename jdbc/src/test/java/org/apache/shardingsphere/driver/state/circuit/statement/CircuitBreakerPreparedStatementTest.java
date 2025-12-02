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

package org.apache.shardingsphere.driver.state.circuit.statement;

import org.apache.shardingsphere.driver.state.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.driver.state.circuit.resultset.CircuitBreakerResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("resource")
class CircuitBreakerPreparedStatementTest {
    
    @Test
    void assertSetNullWithSqlType() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setNull(1, 0));
    }
    
    @Test
    void assertSetNullWithSqlTypeAndName() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setNull(1, 0, "t"));
    }
    
    @Test
    void assertSetBoolean() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBoolean(1, true));
    }
    
    @Test
    void assertSetByte() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setByte(1, (byte) 1));
    }
    
    @Test
    void assertSetShort() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setShort(1, (short) 1));
    }
    
    @Test
    void assertSetInt() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setInt(1, 1));
    }
    
    @Test
    void assertSetLong() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setLong(1, 1L));
    }
    
    @Test
    void assertSetFloat() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setFloat(1, 1F));
    }
    
    @Test
    void assertSetDouble() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setDouble(1, 1D));
    }
    
    @Test
    void assertSetBigDecimal() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBigDecimal(1, BigDecimal.ONE));
    }
    
    @Test
    void assertSetString() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setString(1, "x"));
    }
    
    @Test
    void assertSetBytes() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBytes(1, new byte[]{1}));
    }
    
    @Test
    void assertSetDate() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setDate(1, new Date(1L)));
    }
    
    @Test
    void assertSetDateWithCalendar() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setDate(1, new Date(1L), Calendar.getInstance()));
    }
    
    @Test
    void assertSetTime() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setTime(1, new Time(1L)));
    }
    
    @Test
    void assertSetTimeWithCalendar() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setTime(1, new Time(1L), Calendar.getInstance()));
    }
    
    @Test
    void assertSetTimestamp() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setTimestamp(1, new Timestamp(1L)));
    }
    
    @Test
    void assertSetTimestampWithCalendar() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setTimestamp(1, new Timestamp(1L), Calendar.getInstance()));
    }
    
    @Test
    void assertSetAsciiStreamWithLength() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setAsciiStream(1, new ByteArrayInputStream(new byte[0]), 0));
    }
    
    @Test
    void assertSetAsciiStream() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setAsciiStream(1, new ByteArrayInputStream(new byte[0])));
    }
    
    @Test
    void assertSetAsciiStreamWithLong() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setAsciiStream(1, new ByteArrayInputStream(new byte[0]), 0L));
    }
    
    @Test
    void assertSetUnicodeStream() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setUnicodeStream(1, new ByteArrayInputStream(new byte[0]), 0));
    }
    
    @Test
    void assertSetBinaryStreamWithLength() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBinaryStream(1, new ByteArrayInputStream(new byte[0]), 0));
    }
    
    @Test
    void assertSetBinaryStreamWithLong() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBinaryStream(1, new ByteArrayInputStream(new byte[0]), 0L));
    }
    
    @Test
    void assertSetBinaryStream() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBinaryStream(1, new ByteArrayInputStream(new byte[0])));
    }
    
    @Test
    void assertClearParameters() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().clearParameters());
    }
    
    @Test
    void assertSetObject() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setObject(1, new Object()));
    }
    
    @Test
    void assertSetObjectWithTargetSqlType() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setObject(1, new Object(), 0));
    }
    
    @Test
    void assertSetObjectWithScale() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setObject(1, new Object(), 0, 0));
    }
    
    @Test
    void assertExecute() {
        assertThat(new CircuitBreakerPreparedStatement().execute(), is(false));
    }
    
    @Test
    void assertClearBatch() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().clearBatch());
    }
    
    @Test
    void assertAddBatch() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().addBatch());
    }
    
    @Test
    void assertSetCharacterStreamWithLength() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setCharacterStream(1, new StringReader(""), 0));
    }
    
    @Test
    void assertSetCharacterStreamWithLong() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setCharacterStream(1, new StringReader(""), 0L));
    }
    
    @Test
    void assertSetCharacterStream() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setCharacterStream(1, new StringReader("")));
    }
    
    @Test
    void assertSetBlob() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBlob(1, (Blob) null));
    }
    
    @Test
    void assertSetBlobWithLength() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBlob(1, new ByteArrayInputStream(new byte[0]), 0L));
    }
    
    @Test
    void assertSetBlobWithStream() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setBlob(1, new ByteArrayInputStream(new byte[0])));
    }
    
    @Test
    void assertSetClob() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setClob(1, (Clob) null));
    }
    
    @Test
    void assertSetClobWithLength() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setClob(1, new StringReader(""), 0L));
    }
    
    @Test
    void assertSetClobWithReader() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setClob(1, new StringReader("")));
    }
    
    @Test
    void assertSetArray() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setArray(1, null));
    }
    
    @Test
    void assertSetURL() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setURL(1, new URL("http://localhost")));
    }
    
    @Test
    void assertGetParameterMetaData() {
        Assertions.assertNull(new CircuitBreakerPreparedStatement().getParameterMetaData());
    }
    
    @Test
    void assertSetSQLXML() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().setSQLXML(1, null));
    }
    
    @Test
    void assertExecuteBatch() {
        assertThat(new CircuitBreakerPreparedStatement().executeBatch(), is(new int[]{-1}));
    }
    
    @Test
    void assertGetConnection() {
        assertThat(new CircuitBreakerPreparedStatement().getConnection(), instanceOf(CircuitBreakerConnection.class));
    }
    
    @Test
    void assertGetGeneratedKeys() {
        assertThat(new CircuitBreakerPreparedStatement().getGeneratedKeys(), instanceOf(CircuitBreakerResultSet.class));
    }
    
    @Test
    void assertGetResultSetHoldability() {
        assertThat(new CircuitBreakerPreparedStatement().getResultSetHoldability(), is(0));
    }
    
    @Test
    void assertGetResultSet() {
        assertThat(new CircuitBreakerPreparedStatement().getResultSet(), instanceOf(CircuitBreakerResultSet.class));
    }
    
    @Test
    void assertGetResultSetConcurrency() {
        Assertions.assertEquals(ResultSet.CONCUR_READ_ONLY, new CircuitBreakerPreparedStatement().getResultSetConcurrency());
    }
    
    @Test
    void assertGetResultSetType() {
        Assertions.assertEquals(ResultSet.TYPE_FORWARD_ONLY, new CircuitBreakerPreparedStatement().getResultSetType());
    }
    
    @Test
    void assertIsAccumulate() {
        assertThat(new CircuitBreakerPreparedStatement().isAccumulate(), is(false));
    }
    
    @Test
    void assertGetRoutedStatements() {
        assertThat(new CircuitBreakerPreparedStatement().getRoutedStatements(), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetStatementManager() {
        assertThat(new CircuitBreakerPreparedStatement().getStatementManager(), is(nullValue()));
    }
    
    @Test
    void assertExecuteQuery() {
        assertThat(new CircuitBreakerPreparedStatement().executeQuery(), instanceOf(CircuitBreakerResultSet.class));
    }
    
    @Test
    void assertExecuteUpdate() {
        assertThat(new CircuitBreakerPreparedStatement().executeUpdate(), is(-1));
    }
    
    @Test
    void assertCloseExecutor() {
        assertDoesNotThrow(() -> new CircuitBreakerPreparedStatement().closeExecutor());
    }
}
