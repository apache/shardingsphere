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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode;

import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.DecodingException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.junit.jupiter.api.Test;
import org.opengauss.jdbc.TimestampUtils;
import org.opengauss.replication.LogSequenceNumber;
import org.opengauss.util.PGobject;
import org.mockito.MockedConstruction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class MppdbDecodingPluginTest {
    
    private final OpenGaussLogSequenceNumber logSequenceNumber = new OpenGaussLogSequenceNumber(LogSequenceNumber.valueOf("0/14EFDB8"));
    
    @Test
    void assertDecodeWriteRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        String[] insertTypes = new String[]{"character varying", "text", "char", "character", "nchar", "varchar2", "nvarchar2", "clob"};
        tableData.setColumnsType(insertTypes);
        tableData.setColumnsName(IntStream.range(0, insertTypes.length).mapToObj(idx -> "data" + idx).toArray(String[]::new));
        tableData.setColumnsVal(IntStream.range(0, insertTypes.length).mapToObj(idx -> "'1 2 3'").toArray(String[]::new));
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        IntStream.range(0, insertTypes.length).forEach(each -> assertThat(actual.getAfterRow().get(each), is("1 2 3")));
    }
    
    @Test
    void assertDecodeUpdateRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("UPDATE");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varying"});
        tableData.setColumnsVal(new String[]{"'1 2 3'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        UpdateRowEvent actual = (UpdateRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getAfterRow().get(0), is("1 2 3"));
    }
    
    @Test
    void assertDecodeDeleteRowEvent() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("DELETE");
        String[] deleteTypes = new String[]{"tinyint", "smallint", "integer", "binary_integer", "bigint"};
        String[] deleteValues = new String[]{"46", "30000", "2147483645", "2147483646", "9223372036854775806"};
        tableData.setOldKeysType(deleteTypes);
        tableData.setOldKeysName(IntStream.range(0, deleteTypes.length).mapToObj(idx -> "data" + idx).toArray(String[]::new));
        tableData.setOldKeysVal(deleteValues);
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        DeleteRowEvent actual = (DeleteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        IntStream.range(0, deleteTypes.length).forEach(each -> assertThat(actual.getPrimaryKeys().get(each).toString(), is(deleteValues[each])));
    }
    
    @Test
    void assertDecodeWriteRowEventWithMoney() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"money"});
        tableData.setColumnsVal(new String[]{"'$1.08'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, is("1.08"));
    }
    
    @Test
    void assertDecodeWriteRowEventWithBoolean() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"boolean"});
        tableData.setColumnsVal(new String[]{Boolean.TRUE.toString()});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj.toString(), is(Boolean.TRUE.toString()));
    }
    
    @Test
    void assertDecodeWriteRowEventWithDateAndTime() throws SQLException {
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
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "21:21:21")).thenReturn(Time.valueOf("21:21:21"));
        when(timestampUtils.toTime(null, "21:21:21 pst")).thenReturn(Time.valueOf("13:21:21"));
        when(timestampUtils.toTimestamp(null, "2010-12-12")).thenReturn(Timestamp.valueOf("2010-12-12 00:00:00.0"));
        when(timestampUtils.toTimestamp(null, "2013-12-11 pst")).thenReturn(Timestamp.valueOf("2013-12-11 16:00:00.0"));
        when(timestampUtils.toTimestamp(null, "2003-04-12 04:05:06")).thenReturn(Timestamp.valueOf("2003-04-12 04:05:00.0"));
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(new OpenGaussTimestampUtils(timestampUtils), false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        IntStream.range(0, insertTypes.length).forEach(each -> assertThat(actual.getAfterRow().get(each).toString(), is(compareValues[each])));
    }
    
    @Test
    void assertDecodeWriteRowEventWithByteA() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"bytea"});
        tableData.setColumnsVal(new String[]{"'\\xff00ab'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, isA(byte[].class));
        assertThat(byteaObj, is(new byte[]{(byte) 0xff, (byte) 0, (byte) 0xab}));
    }
    
    @Test
    void assertDecodeWriteRowEventWithRaw() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"raw"});
        tableData.setColumnsVal(new String[]{"'7D'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        assertThat(actual.getLogSequenceNumber(), is(logSequenceNumber));
        assertThat(actual.getTableName(), is("test"));
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, isA(PGobject.class));
        assertThat(byteaObj.toString(), is("7D"));
    }
    
    @Test
    void assertDecodeUnknownTableType() {
        ByteBuffer data = ByteBuffer.wrap("unknown".getBytes());
        assertThat(new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber), isA(PlaceholderEvent.class));
    }
    
    @Test
    void assertDecodeUnknownRowEventType() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("UNKNOWN");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varying"});
        tableData.setColumnsVal(new String[]{"1 2 3"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        assertThrows(IngestException.class, () -> new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber));
    }
    
    @Test
    void assertDecodeTime() throws SQLException {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"time without time zone"});
        tableData.setColumnsVal(new String[]{"'1 2 3'"});
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTime(null, "1 2 3")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        assertThrows(DecodingException.class, () -> new MppdbDecodingPlugin(new OpenGaussTimestampUtils(timestampUtils), true, false).decode(data, logSequenceNumber));
    }
    
    @Test
    void assertDecodeWithTx() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"raw"});
        tableData.setColumnsVal(new String[]{"'7D'"});
        List<String> dataList = Arrays.asList("BEGIN 1", JsonUtils.toJsonString(tableData), JsonUtils.toJsonString(tableData),
                "COMMIT 1 (at 2022-10-27 04:19:39.476261+00) CSN 3468");
        MppdbDecodingPlugin mppdbDecodingPlugin = new MppdbDecodingPlugin(null, true, false);
        List<AbstractWALEvent> expectedEvent = new LinkedList<>();
        for (String each : dataList) {
            expectedEvent.add(mppdbDecodingPlugin.decode(ByteBuffer.wrap(each.getBytes()), logSequenceNumber));
        }
        assertThat(expectedEvent.size(), is(4));
        AbstractWALEvent actualFirstEvent = expectedEvent.get(0);
        assertThat(actualFirstEvent, isA(BeginTXEvent.class));
        AbstractWALEvent actualLastEvent = expectedEvent.get(expectedEvent.size() - 1);
        assertThat(actualLastEvent, isA(CommitTXEvent.class));
        assertThat(((CommitTXEvent) actualLastEvent).getCsn(), is(3468L));
        assertThat(((CommitTXEvent) actualLastEvent).getXid(), is(1L));
    }
    
    @Test
    void assertParallelDecodeWithTx() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"raw"});
        tableData.setColumnsVal(new String[]{"'7D'"});
        List<String> dataList = Arrays.asList("BEGIN CSN: 951909 first_lsn: 5/59825858", JsonUtils.toJsonString(tableData), JsonUtils.toJsonString(tableData), "commit xid: 1006076");
        MppdbDecodingPlugin mppdbDecodingPlugin = new MppdbDecodingPlugin(null, true, true);
        List<AbstractWALEvent> actual = new LinkedList<>();
        for (String each : dataList) {
            actual.add(mppdbDecodingPlugin.decode(ByteBuffer.wrap(each.getBytes()), logSequenceNumber));
        }
        assertThat(actual.size(), is(4));
        assertThat(actual.get(0), isA(BeginTXEvent.class));
        assertThat(((BeginTXEvent) actual.get(0)).getCsn(), is(951909L));
        assertThat(((WriteRowEvent) actual.get(1)).getAfterRow().get(0).toString(), is("7D"));
        assertThat(((WriteRowEvent) actual.get(2)).getAfterRow().get(0).toString(), is("7D"));
        assertThat(((CommitTXEvent) actual.get(3)).getXid(), is(1006076L));
        assertNull(((CommitTXEvent) actual.get(3)).getCsn());
    }
    
    @Test
    void assertDecodeWithTsrange() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"tsrange"});
        tableData.setColumnsVal(new String[]{"'[\"2020-01-01 00:00:00\",\"2021-01-01 00:00:00\")'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, isA(PGobject.class));
        assertThat(byteaObj.toString(), is("[\"2020-01-01 00:00:00\",\"2021-01-01 00:00:00\")"));
    }
    
    @Test
    void assertDecodeWithDaterange() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"daterange"});
        tableData.setColumnsVal(new String[]{"'[2020-01-02,2021-01-02)'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, isA(PGobject.class));
        assertThat(byteaObj.toString(), is("[2020-01-02,2021-01-02)"));
    }
    
    @Test
    void assertDecodeWithTsquery() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"tsquery"});
        tableData.setColumnsVal(new String[]{"'''fff'' | ''faa'''"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj.toString(), is("'fff' | 'faa'"));
    }
    
    @Test
    void assertDecodeWitTinyint() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"tinyint"});
        tableData.setColumnsVal(new String[]{"255"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
        Object byteaObj = actual.getAfterRow().get(0);
        assertThat(byteaObj, is(255));
    }
    
    @Test
    void assertDecodeWriteRowEventWithVariousTypes() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        String[] columnTypes = new String[]{"numeric(10,2)", "bit(3)", "real", "double precision", "money", "bytea", "blob", "interval", "character", "text", "tstzrange"};
        tableData.setColumnsName(IntStream.range(0, columnTypes.length).mapToObj(idx -> "data" + idx).toArray(String[]::new));
        tableData.setColumnsType(columnTypes);
        String[] columnValues = new String[]{"10.1", "b101", "1.5", "2.5", "'1.08'", "'\\x'", "'\\x01'", "'abc year'", "a", "null", "'[\"2020-01-01 00:00:00+00\",\"2021-01-01 00:00:00+00\"]'"};
        tableData.setColumnsVal(columnValues);
        WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes()), logSequenceNumber);
        assertThat(actual.getAfterRow().get(0), is(new BigDecimal("10.1")));
        assertThat(actual.getAfterRow().get(1), is("101"));
        assertThat(actual.getAfterRow().get(2), is(1.5F));
        assertThat(actual.getAfterRow().get(3), is(2.5D));
        assertThat(actual.getAfterRow().get(4), is("1.08"));
        assertThat(((byte[]) actual.getAfterRow().get(5)).length, is(0));
        assertThat(((byte[]) actual.getAfterRow().get(6))[0], is((byte) 1));
        assertNull(actual.getAfterRow().get(7));
        assertThat(actual.getAfterRow().get(8), is("a"));
        assertNull(actual.getAfterRow().get(9));
        assertThat(actual.getAfterRow().get(10).toString(), is("[\"2020-01-01 00:00:00+00\",\"2021-01-01 00:00:00+00\"]"));
    }
    
    @Test
    void assertDecodeSeriallyPlaceholderEvent() {
        ByteBuffer data = ByteBuffer.wrap("RANDOM".getBytes());
        AbstractWALEvent actual = new MppdbDecodingPlugin(null, true, false).decode(data, logSequenceNumber);
        assertThat(actual, isA(PlaceholderEvent.class));
    }
    
    @Test
    void assertDecodeParallelBeginWithoutFirstLsn() {
        MppdbDecodingPlugin mppdbDecodingPlugin = new MppdbDecodingPlugin(null, true, true);
        AbstractWALEvent beginEvent = mppdbDecodingPlugin.decode(ByteBuffer.wrap("BEGIN CSN: 10".getBytes()), logSequenceNumber);
        assertThat(beginEvent, isA(BeginTXEvent.class));
        assertThat(((BeginTXEvent) beginEvent).getCsn(), is(0L));
        AbstractWALEvent placeholderEvent = mppdbDecodingPlugin.decode(ByteBuffer.wrap("OTHER".getBytes()), logSequenceNumber);
        assertThat(placeholderEvent, isA(PlaceholderEvent.class));
    }
    
    @Test
    void assertDecodeParallelCommitUppercase() {
        AbstractWALEvent actual = new MppdbDecodingPlugin(null, true, true).decode(ByteBuffer.wrap("COMMIT xid: 20".getBytes()), logSequenceNumber);
        assertThat(actual, isA(CommitTXEvent.class));
        assertThat(((CommitTXEvent) actual).getXid(), is(20L));
        assertNull(((CommitTXEvent) actual).getCsn());
    }
    
    @Test
    void assertDecodeSelectRowEventType() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("SELECT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"character varying"});
        tableData.setColumnsVal(new String[]{"'1'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        assertThrows(IngestException.class, () -> new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber));
    }
    
    @Test
    void assertDecodeTimestampThrowsDecodingException() throws SQLException {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"timestamp without time zone"});
        tableData.setColumnsVal(new String[]{"'2020-01-01'"});
        TimestampUtils timestampUtils = mock(TimestampUtils.class);
        when(timestampUtils.toTimestamp(null, "2020-01-01")).thenThrow(new SQLException(""));
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        assertThrows(DecodingException.class, () -> new MppdbDecodingPlugin(new OpenGaussTimestampUtils(timestampUtils), false, false).decode(data, logSequenceNumber));
    }
    
    @Test
    void assertDecodePgObjectWhenSetValueThrowsSQLException() {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"raw"});
        tableData.setColumnsVal(new String[]{"'7D'"});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        try (MockedConstruction<PGobject> ignored = mockConstruction(PGobject.class, (mocked, mockContext) -> doThrow(new SQLException()).when(mocked).setValue(anyString()))) {
            WriteRowEvent actual = (WriteRowEvent) new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber);
            assertNull(actual.getAfterRow().get(0));
        }
    }
    
    @ParameterizedTest
    @MethodSource("invalidHexValueProvider")
    void assertDecodeByteaWithInvalidHexValue(final String hexValue) {
        MppTableData tableData = new MppTableData();
        tableData.setTableName("public.test");
        tableData.setOpType("INSERT");
        tableData.setColumnsName(new String[]{"data"});
        tableData.setColumnsType(new String[]{"bytea"});
        tableData.setColumnsVal(new String[]{hexValue});
        ByteBuffer data = ByteBuffer.wrap(JsonUtils.toJsonString(tableData).getBytes());
        assertThrows(IllegalArgumentException.class, () -> new MppdbDecodingPlugin(null, false, false).decode(data, logSequenceNumber));
    }
    
    private static Stream<String> invalidHexValueProvider() {
        return Stream.of("'\\xabc'", "'\\x0g'", "'\\xg0'");
    }
}
