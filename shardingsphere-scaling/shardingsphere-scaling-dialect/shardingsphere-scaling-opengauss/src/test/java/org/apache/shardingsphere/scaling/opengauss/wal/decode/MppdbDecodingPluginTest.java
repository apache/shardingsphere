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

package org.apache.shardingsphere.scaling.opengauss.wal.decode;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.DecodingException;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.WriteRowEvent;
import org.junit.Test;
import org.opengauss.jdbc.TimestampUtils;
import org.opengauss.replication.LogSequenceNumber;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MppdbDecodingPluginTest {

    private final LogSequenceNumber pgSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");

    private final OpenGaussLogSequenceNumber logSequenceNumber = new OpenGaussLogSequenceNumber(pgSequenceNumber);
    
    @Test
    public void assertDecodeWriteRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varyint"});
        tableData.setColumnsVal(new String[]{"1 2 3"});
        ByteBuffer data = ByteBuffer.wrap(new Gson().toJson(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3"));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("UPDATE");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varyint"});
        tableData.setColumnsVal(new String[]{"1 2 3"});
        ByteBuffer data = ByteBuffer.wrap(new Gson().toJson(tableData).getBytes());
        UpdateRowEvent actual = (UpdateRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3"));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("DELETE");
        tableData.setOldKeysName(new String[]{"data"});
        tableData.setOldKeysType(new String[]{"integer"});
        tableData.setOldKeysVal(new String[]{"1"});
        ByteBuffer data = ByteBuffer.wrap(new Gson().toJson(tableData).getBytes());
        DeleteRowEvent actual = (DeleteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getPrimaryKeys().get(0), is(1));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithByteA() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"bytea"});
        tableData.setColumnsVal(new String[]{"\\xff00ab"});
        ByteBuffer data = ByteBuffer.wrap(new Gson().toJson(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is(new byte[]{(byte) 0xff, (byte) 0, (byte) 0xab}));
    }
    
    @Test
    public void assertDecodeUnknownTableType() {
        ByteBuffer data = ByteBuffer.wrap("unknown".getBytes());
        AbstractWalEvent actual = new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertTrue(actual instanceof PlaceholderEvent);
    }
    
    @Test(expected = ScalingTaskExecuteException.class)
    public void assertDecodeUnknownRowEventType() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("UNKNOWN");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varying"});
        tableData.setColumnsVal(new String[]{"1 2 3"});
        ByteBuffer data = ByteBuffer.wrap(new Gson().toJson(tableData).getBytes());
        new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
    }
    
    @Test(expected = DecodingException.class)
    @SneakyThrows(SQLException.class)
    public void assertDecodeTime() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"time without time zone"});
        tableData.setColumnsVal(new String[]{"1 2 3'"});
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "1 2 3'")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap(new Gson().toJson(tableData).getBytes());
        new MppdbDecodingPlugin(new OpenGaussTimestampUtils(timestampUtils)).decode(data, logSequenceNumber);
    }
}
