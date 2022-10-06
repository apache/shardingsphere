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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.junit.Test;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.replication.LogSequenceNumber;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestDecodingPluginTest {
    
    private final LogSequenceNumber pgSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");
    
    private final PostgreSQLLogSequenceNumber logSequenceNumber = new PostgreSQLLogSequenceNumber(pgSequenceNumber);
    
    @Test
    public void assertDecodeWriteRowEvent() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[character varying]:'1 2 3'''".getBytes());
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3'"));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: UPDATE: data[character varying]:'1 2 3'''".getBytes());
        UpdateRowEvent actual = (UpdateRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3'"));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: DELETE: data[integer]:1".getBytes());
        DeleteRowEvent actual = (DeleteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getPrimaryKeys().get(0), is(1));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithByteA() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[bytea]:'\\xff00ab'".getBytes());
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is(new byte[]{(byte) 0xff, (byte) 0, (byte) 0xab}));
    }
    
    @Test
    public void assertDecodeUnknownTableType() {
        ByteBuffer data = ByteBuffer.wrap("unknown".getBytes());
        assertThat(new TestDecodingPlugin(null).decode(data, logSequenceNumber), instanceOf(PlaceholderEvent.class));
    }
    
    @Test(expected = IngestException.class)
    public void assertDecodeUnknownRowEventType() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: UNKNOWN: data[character varying]:'1 2 3'''".getBytes());
        new TestDecodingPlugin(null).decode(data, logSequenceNumber);
    }
    
    @Test(expected = DecodingException.class)
    @SneakyThrows(SQLException.class)
    public void assertDecodeTime() {
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "1 2 3'")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[time without time zone]:'1 2 3'''".getBytes());
        new TestDecodingPlugin(new PostgreSQLTimestampUtils(timestampUtils)).decode(data, logSequenceNumber);
    }
    
    @Test
    public void assertDecodeInsertWithNullValue() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: id[integer]:123 col0[integer]:null col1[character varying]:null col2[character varying]:'nonnull'".getBytes());
        AbstractWalEvent actual = new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual, instanceOf(WriteRowEvent.class));
        WriteRowEvent actualWriteRowEvent = (WriteRowEvent) actual;
        assertThat(actualWriteRowEvent.getAfterRow().get(0), is(123));
        assertNull(actualWriteRowEvent.getAfterRow().get(1));
        assertNull(actualWriteRowEvent.getAfterRow().get(2));
        assertThat(actualWriteRowEvent.getAfterRow().get(3), is("nonnull"));
    }
}
