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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode;

import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.WriteRowEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.replication.LogSequenceNumber;

import java.math.BigDecimal;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestDecodingPluginTest {
    
    private final LogSequenceNumber pgSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");
    
    private final PostgreSQLLogSequenceNumber logSequenceNumber = new PostgreSQLLogSequenceNumber(pgSequenceNumber);
    
    @Test
    void assertDecodeBeginTxEvent() {
        ByteBuffer data = ByteBuffer.wrap("BEGIN 616281".getBytes(StandardCharsets.UTF_8));
        BeginTXEvent actual = (BeginTXEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getXid(), is(616281L));
    }
    
    @Test
    void assertDecodeCommitTxEvent() {
        ByteBuffer data = ByteBuffer.wrap("COMMIT 616281".getBytes(StandardCharsets.UTF_8));
        CommitTXEvent actual = (CommitTXEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getXid(), is(616281L));
    }
    
    @Test
    void assertDecodeWriteRowEvent() {
        ByteBuffer data = ByteBuffer.wrap(("table public.test: INSERT: data[character varying]:' 1 2 3'' 😊中' t_json_empty[json]:'{}' t_json[json]:'{\"test\":\"中中{中中}' 中\"}'"
                + " t_jsonb[jsonb]:'{\"test\":\"😊Emoji中\"}'").getBytes(StandardCharsets.UTF_8));
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is(" 1 2 3' 😊中"));
        assertThat(actual.getAfterRow().get(1), is("{}"));
        assertThat(actual.getAfterRow().get(2), is("{\"test\":\"中中{中中}' 中\"}"));
        assertThat(actual.getAfterRow().get(3), is("{\"test\":\"😊Emoji中\"}"));
    }
    
    @Test
    void assertDecodeUpdateRowEvent() {
        ByteBuffer data = ByteBuffer.wrap(
                "table public.test: UPDATE: unicode[character varying]:' 1 2 3'' 😊中 ' t_json_empty[json]:'{}' t_json[json]:'{\"test\":\"中中{中中}' 中\"}'".getBytes(StandardCharsets.UTF_8));
        UpdateRowEvent actual = (UpdateRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is(" 1 2 3' 😊中 "));
        assertThat(actual.getAfterRow().get(1), is("{}"));
        assertThat(actual.getAfterRow().get(2), is("{\"test\":\"中中{中中}' 中\"}"));
    }
    
    @Test
    void assertDecodeDeleteRowEvent() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: DELETE: data[integer]:1".getBytes(StandardCharsets.UTF_8));
        DeleteRowEvent actual = (DeleteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getPrimaryKeys().get(0), is(1));
    }
    
    @Test
    void assertDecodeWriteRowEventWithByteA() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[bytea]:'\\xff00ab'".getBytes(StandardCharsets.UTF_8));
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is(new byte[]{(byte) 0xff, (byte) 0, (byte) 0xab}));
    }
    
    @Test
    void assertDecodeUnknownTableType() {
        ByteBuffer data = ByteBuffer.wrap("unknown".getBytes(StandardCharsets.UTF_8));
        assertThat(new TestDecodingPlugin(null).decode(data, logSequenceNumber), isA(PlaceholderEvent.class));
    }
    
    @Test
    void assertDecodeTime() throws SQLException {
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "1 2 3'")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[time without time zone]:'1 2 3'''".getBytes(StandardCharsets.UTF_8));
        assertThrows(DecodingException.class, () -> new TestDecodingPlugin(new PostgreSQLTimestampUtils(timestampUtils)).decode(data, logSequenceNumber));
    }
    
    @Test
    void assertDecodeInsertWithNullValue() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: id[integer]:123 col0[integer]:null col1[character varying]:null col2[character varying]:'nonnull'"
                .getBytes(StandardCharsets.UTF_8));
        AbstractWALEvent actual = new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual, isA(WriteRowEvent.class));
        WriteRowEvent actualWriteRowEvent = (WriteRowEvent) actual;
        assertThat(actualWriteRowEvent.getAfterRow().get(0), is(123));
        assertNull(actualWriteRowEvent.getAfterRow().get(1));
        assertNull(actualWriteRowEvent.getAfterRow().get(2));
        assertThat(actualWriteRowEvent.getAfterRow().get(3), is("nonnull"));
    }
    
    @Test
    void assertDecodeJsonValue() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: id[integer]:123 ".getBytes(StandardCharsets.UTF_8));
        AbstractWALEvent actual = new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual, isA(WriteRowEvent.class));
    }
    
    @Test
    void assertDecodeColumnWithoutBracketThrowsBufferUnderflow() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: column_without_bracket".getBytes(StandardCharsets.UTF_8));
        assertThrows(BufferUnderflowException.class, () -> new TestDecodingPlugin(null).decode(data, logSequenceNumber));
    }
    
    @Test
    void assertDecodeStringFollowedBySpaceStopsAtSpace() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: col_text[character varying]:'abc' col_int[integer]:1".getBytes(StandardCharsets.UTF_8));
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getAfterRow().get(0), is("abc"));
        assertThat(actual.getAfterRow().get(1), is(1));
    }
    
    @Test
    void assertDecodeVariousColumnTypes() {
        BaseTimestampUtils timestampUtils = new BaseTimestampUtils() {
            
            @Override
            public Time toTime(final Calendar cal, final String input) throws SQLException {
                return Time.valueOf(input);
            }
            
            @Override
            public Timestamp toTimestamp(final Calendar cal, final String input) throws SQLException {
                return Timestamp.valueOf(input);
            }
        };
        String wal = "table public.test: INSERT: foo_numeric[numeric]:123.45 foo_bit[bit]:101 foo_smallint[smallint]:1 foo_bigint[bigint]:1234567890123"
                + " foo_real[real]:1.5 foo_double[double precision]:2.5 foo_boolean[boolean]:true foo_date[date]:'2020-01-02'"
                + " foo_time[time without time zone]:'12:34:56' foo_timestamp[timestamp without time zone]:'2020-01-02 03:04:05'"
                + " foo_bytea[bytea]:'\\x' foo_json[json]:'{\"a\":{\"b\":1}}' foo_text[character varying]:'abc''def' foo_null[integer]:null";
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(timestampUtils).decode(ByteBuffer.wrap(wal.getBytes(StandardCharsets.UTF_8)), logSequenceNumber);
        assertThat(actual.getAfterRow().get(0), is(new BigDecimal("123.45")));
        assertThat(actual.getAfterRow().get(1), is("101"));
        assertThat(actual.getAfterRow().get(2), is((short) 1));
        assertThat(actual.getAfterRow().get(3), is(1234567890123L));
        assertThat(actual.getAfterRow().get(4), is(1.5F));
        assertThat(actual.getAfterRow().get(5), is(2.5D));
        assertThat(actual.getAfterRow().get(6), is(true));
        assertThat(actual.getAfterRow().get(7), is(Date.valueOf("2020-01-02")));
        assertThat(actual.getAfterRow().get(8), is(Time.valueOf("12:34:56")));
        assertThat(actual.getAfterRow().get(9), is(Timestamp.valueOf("2020-01-02 03:04:05")));
        assertThat(actual.getAfterRow().get(10), is(new byte[0]));
        assertThat(actual.getAfterRow().get(11), is("{\"a\":{\"b\":1}}"));
        assertThat(actual.getAfterRow().get(12), is("abc'def"));
        assertNull(actual.getAfterRow().get(13));
    }
    
    @Test
    void assertDecodeUnterminatedString() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: text_col[character varying]:'unterminated".getBytes(StandardCharsets.UTF_8));
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getAfterRow().get(0), is("unterminated"));
    }
    
    @Test
    void assertDecodeJsonWithNoClosingBrace() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[json]:'{'".getBytes(StandardCharsets.UTF_8));
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertNull(actual.getAfterRow().get(0));
    }
    
    @Test
    void assertDecodeTimestampSQLException() throws SQLException {
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTimestamp(null, "2020-01-02 03:04:05")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[timestamp without time zone]:'2020-01-02 03:04:05'".getBytes(StandardCharsets.UTF_8));
        assertThrows(DecodingException.class, () -> new TestDecodingPlugin(new PostgreSQLTimestampUtils(timestampUtils)).decode(data, logSequenceNumber));
    }
    
    @ParameterizedTest
    @MethodSource("provideNullValueWal")
    void assertDecodeNullValue(final String wal, final int expectedSize, final Integer expectedSecond) {
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(ByteBuffer.wrap(wal.getBytes(StandardCharsets.UTF_8)), logSequenceNumber);
        assertThat(actual.getAfterRow().size(), is(expectedSize));
        assertNull(actual.getAfterRow().get(0));
        if (null != expectedSecond) {
            assertThat(actual.getAfterRow().get(1), is(expectedSecond));
        }
    }
    
    @ParameterizedTest
    @MethodSource("provideIllegalByteaWal")
    void assertDecodeByteaIllegalArgument(final String wal) {
        ByteBuffer data = ByteBuffer.wrap(wal.getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> new TestDecodingPlugin(null).decode(data, logSequenceNumber));
    }
    
    @ParameterizedTest
    @MethodSource("provideIngestExceptionWal")
    void assertDecodeIngestException(final String wal) {
        ByteBuffer data = ByteBuffer.wrap(wal.getBytes(StandardCharsets.UTF_8));
        assertThrows(IngestException.class, () -> new TestDecodingPlugin(null).decode(data, logSequenceNumber));
    }
    
    private static Stream<String> provideIllegalByteaWal() {
        return Stream.of(
                "table public.test: INSERT: data[bytea]:'\\xff0'",
                "table public.test: INSERT: data[bytea]:'\\xzz'");
    }
    
    private static Stream<String> provideIngestExceptionWal() {
        return Stream.of(
                "table public.test: UNKNOWN: data[character varying]:'1 2 3'''",
                "table public.test: SELECT: id[integer]:1",
                "table public.test: INSERT: data[json]:'{}x'");
    }
    
    private static Stream<Arguments> provideNullValueWal() {
        return Stream.of(
                Arguments.of("table public.test: INSERT: col_null[integer]:null col_int[integer]:1", 2, 1),
                Arguments.of("table public.test: INSERT: col_null[integer]:null", 1, null));
    }
}
