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
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.replication.LogSequenceNumber;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

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
        ByteBuffer data = ByteBuffer.wrap("table public.test: UPDATE: unicode[character varying]:' 1 2 3'' 😊中 ' t_json_empty[json]:'{}' t_json[json]:'{\"test\":\"中中{中中}' 中\"}'"
                .getBytes(StandardCharsets.UTF_8));
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
    void assertDecodeUnknownRowEventType() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: UNKNOWN: data[character varying]:'1 2 3'''".getBytes(StandardCharsets.UTF_8));
        assertThrows(IngestException.class, () -> new TestDecodingPlugin(null).decode(data, logSequenceNumber));
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
}
