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

package org.apache.shardingsphere.scaling.postgresql.wal.decode;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.WriteRowEvent;
import org.junit.Test;
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.replication.LogSequenceNumber;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestDecodingPluginTest {
    
    private LogSequenceNumber logSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");
    
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
        AbstractWalEvent actual = new TestDecodingPlugin(null).decode(data, logSequenceNumber);
        assertTrue(actual instanceof PlaceholderEvent);
    }
    
    @Test(expected = SyncTaskExecuteException.class)
    public void assertDecodeUnknownRowEventType() {
        ByteBuffer data = ByteBuffer.wrap("table public.test: UNKNOWN: data[character varying]:'1 2 3'''".getBytes());
        new TestDecodingPlugin(null).decode(data, logSequenceNumber);
    }
    
    @Test(expected = DecodingException.class)
    @SneakyThrows(SQLException.class)
    public void assertDecodeTime() {
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(eq(null), eq("1 2 3'"))).thenThrow(new SQLException());
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[time without time zone]:'1 2 3'''".getBytes());
        new TestDecodingPlugin(timestampUtils).decode(data, logSequenceNumber);
    }
}
