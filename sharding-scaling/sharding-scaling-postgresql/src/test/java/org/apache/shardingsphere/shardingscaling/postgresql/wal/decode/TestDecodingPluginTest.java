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

package org.apache.shardingsphere.shardingscaling.postgresql.wal.decode;

import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.WriteRowEvent;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.postgresql.replication.LogSequenceNumber;

import java.nio.ByteBuffer;

public class TestDecodingPluginTest {
    
    @Test
    public void assertDecodeWriteRowEvent() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: INSERT: data[character varying]:'1 2 3'''".getBytes());
        WriteRowEvent actual = (WriteRowEvent) new TestDecodingPlugin().decode(data, lsn);
        Assert.assertThat(actual.getLogSequenceNumber(), Matchers.is(lsn));
        Assert.assertThat(actual.getTableName(), Matchers.is("test"));
        Assert.assertThat((String) actual.getAfterRow().get(0), Matchers.is("1 2 3'"));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: UPDATE: data[character varying]:'1 2 3'''".getBytes());
        UpdateRowEvent actual = (UpdateRowEvent) new TestDecodingPlugin().decode(data, lsn);
        Assert.assertThat(actual.getLogSequenceNumber(), Matchers.is(lsn));
        Assert.assertThat(actual.getTableName(), Matchers.is("test"));
        Assert.assertThat((String) actual.getAfterRow().get(0), Matchers.is("1 2 3'"));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        LogSequenceNumber lsn = LogSequenceNumber.valueOf("0/14EFDB8");
        ByteBuffer data = ByteBuffer.wrap("table public.test: DELETE: data[integer]:1".getBytes());
        DeleteRowEvent actual = (DeleteRowEvent) new TestDecodingPlugin().decode(data, lsn);
        Assert.assertThat(actual.getLogSequenceNumber(), Matchers.is(lsn));
        Assert.assertThat(actual.getTableName(), Matchers.is("test"));
        Assert.assertThat((Integer) actual.getPrimaryKeys().get(0), Matchers.is(1));
    }
}
