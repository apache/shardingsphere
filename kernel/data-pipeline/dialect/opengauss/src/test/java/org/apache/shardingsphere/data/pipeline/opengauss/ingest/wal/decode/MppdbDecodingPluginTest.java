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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.junit.Test;
import org.opengauss.jdbc.TimestampUtils;
import org.opengauss.replication.LogSequenceNumber;
import org.opengauss.util.PGobject;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MppdbDecodingPluginTest {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final LogSequenceNumber pgSequenceNumber = LogSequenceNumber.valueOf("0/14EFDB8");
    
    private final OpenGaussLogSequenceNumber logSequenceNumber = new OpenGaussLogSequenceNumber(pgSequenceNumber);
    
    @Test
    public void assertDecodeWriteRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        String[] insertTypes = new String[]{"character varying", "text", "char", "character", "nchar", "varchar2", "nvarchar2", "clob"};
        tableData.setColumnsType(insertTypes);
        tableData.setColumnsName(IntStream.range(0, insertTypes.length).mapToObj(idx -> "data" + idx).toArray(String[]::new));
        tableData.setColumnsVal(IntStream.range(0, insertTypes.length).mapToObj(idx -> "'1 2 3'").toArray(String[]::new));
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        IntStream.range(0, insertTypes.length).forEach(each -> assertThat(actual.getAfterRow().get(each), is("1 2 3")));
    }
    
    @SneakyThrows
    private String toJSON(final MppTableData tableData) {
        return OBJECT_MAPPER.writeValueAsString(tableData);
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("UPDATE");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varying"});
        tableData.setColumnsVal(new String[]{"'1 2 3'"});
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
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
        String[] deleteTypes = new String[]{"tinyint", "smallint", "integer", "binary_integer", "bigint"};
        String[] deleteValues = new String[]{"46", "30000", "2147483645", "2147483646", "9223372036854775806"};
        tableData.setOldKeysType(deleteTypes);
        tableData.setOldKeysName(IntStream.range(0, deleteTypes.length).mapToObj(idx -> "data" + idx).toArray(String[]::new));
        tableData.setOldKeysVal(deleteValues);
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        DeleteRowEvent actual = (DeleteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        IntStream.range(0, deleteTypes.length).forEach(each -> assertThat(actual.getPrimaryKeys().get(each).toString(), is(deleteValues[each])));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithMoney() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"money"});
        tableData.setColumnsVal(new String[]{"'$1.08'"});
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj.toString(), is("1.08"));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithBoolean() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"boolean"});
        tableData.setColumnsVal(new String[]{Boolean.TRUE.toString()});
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj.toString(), is(Boolean.TRUE.toString()));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithDateAndTime() throws SQLException {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        String[] insertTypes = new String[]{
                "time without time zone", "time with time zone", "timestamp without time zone", "timestamp with time zone", "smalldatetime", "date", "interval", "reltime"};
        String[] insertValues = new String[]{"'21:21:21'", "'21:21:21 pst'", "'2010-12-12'", "'2013-12-11 pst'", "'2003-04-12 04:05:06'", "'2021-10-10'", "'3 days'", "'2 mons'"};
        final String[] compareValues = {
                "21:21:21", "13:21:21", "2010-12-12 00:00:00.0", "2013-12-11 16:00:00.0", "2003-04-12 04:05:00.0", "2021-10-10", "0 years 0 mons 3 days 0 hours 0 mins 0.00 secs", "2 mons"};
        tableData.setColumnsName(IntStream.range(0, insertTypes.length).mapToObj(idx -> "data" + idx).toArray(String[]::new));
        tableData.setColumnsType(insertTypes);
        tableData.setColumnsVal(insertValues);
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "21:21:21")).thenReturn(Time.valueOf("21:21:21"));
        when(timestampUtils.toTime(null, "21:21:21 pst")).thenReturn(Time.valueOf("13:21:21"));
        when(timestampUtils.toTimestamp(null, "2010-12-12")).thenReturn(Timestamp.valueOf("2010-12-12 00:00:00.0"));
        when(timestampUtils.toTimestamp(null, "2013-12-11 pst")).thenReturn(Timestamp.valueOf("2013-12-11 16:00:00.0"));
        when(timestampUtils.toTimestamp(null, "2003-04-12 04:05:06")).thenReturn(Timestamp.valueOf("2003-04-12 04:05:00.0"));
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(new OpenGaussTimestampUtils(timestampUtils)).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        IntStream.range(0, insertTypes.length).forEach(each -> assertThat(actual.getAfterRow().get(each).toString(), is(compareValues[each])));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithByteA() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"bytea"});
        tableData.setColumnsVal(new String[]{"'\\xff00ab'"});
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, instanceOf(PGobject.class));
        assertThat(byteaObj.toString(), is(new String(new byte[]{(byte) 0xff, (byte) 0, (byte) 0xab})));
    }
    
    @Test
    public void assertDecodeWriteRowEventWithRaw() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"raw"});
        tableData.setColumnsVal(new String[]{"'7D'"});
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, instanceOf(PGobject.class));
        assertThat(byteaObj.toString(), is("7D"));
    }
    
    @Test
    public void assertDecodeUnknownTableType() {
        ByteBuffer data = ByteBuffer.wrap("unknown".getBytes());
        assertThat(new MppdbDecodingPlugin(null).decode(data, logSequenceNumber), instanceOf(PlaceholderEvent.class));
    }
    
    @Test(expected = IngestException.class)
    public void assertDecodeUnknownRowEventType() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("UNKNOWN");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varying"});
        tableData.setColumnsVal(new String[]{"1 2 3"});
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        new MppdbDecodingPlugin(null).decode(data, logSequenceNumber);
    }
    
    @Test(expected = DecodingException.class)
    public void assertDecodeTime() throws SQLException {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"time without time zone"});
        tableData.setColumnsVal(new String[]{"'1 2 3'"});
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "1 2 3")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap(toJSON(tableData).getBytes());
        new MppdbDecodingPlugin(new OpenGaussTimestampUtils(timestampUtils)).decode(data, logSequenceNumber);
    }
}
