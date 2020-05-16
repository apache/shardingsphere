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

import org.apache.shardingsphere.scaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.WriteRowEvent;
import org.junit.Test;
import org.postgresql.replication.LogSequenceNumber;
import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TestDecodingPluginTest {
    
    @Test
    public void assertDecodeWriteRowEvent() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[character varying]:'1 2 3'''".getBytes());
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, lsn);
        assertThat(actual.getLogSequenceNumber(), is(lsn));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3'"));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: UPDATE: data[character varying]:'1 2 3'''".getBytes());
        UpdateRowEvent actual = (UpdateRowEvent) new TestDecodingPlugin(null).decode(data, lsn);
        assertThat(actual.getLogSequenceNumber(), is(lsn));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3'"));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: DELETE: data[integer]:1".getBytes());
        DeleteRowEvent actual = (DeleteRowEvent) new TestDecodingPlugin(null).decode(data, lsn);
        assertThat(actual.getLogSequenceNumber(), is(lsn));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getPrimaryKeys().get(0), is(1));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithByteA() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[bytea]:'\\xff00ab'".getBytes());
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin(null).decode(data, lsn);
        assertThat(actual.getLogSequenceNumber(), is(lsn));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is(new byte[] {(byte) 0xff, (byte) 0, (byte) 0xab}));
    }
}
