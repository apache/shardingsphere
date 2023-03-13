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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TimestampProto;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.WrappersProto;
import com.google.protobuf.util.JsonFormat;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponseProtocol;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.Builder;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataRecordResultConvertUtilTest {
    
    @Test
    public void assertConvertDataRecordToRecord() throws InvalidProtocolBufferException, SQLException {
        DataRecord dataRecord = new DataRecord(new IntegerPrimaryKeyPosition(0, 1), 2);
        dataRecord.addColumn(new Column("BigInteger", BigInteger.ONE, false, true));
        dataRecord.addColumn(new Column("BigDecimal", BigDecimal.valueOf(123), false, false));
        dataRecord.addColumn(new Column("Long", Long.MAX_VALUE, false, false));
        dataRecord.addColumn(new Column("Integer", Integer.MAX_VALUE, false, false));
        dataRecord.addColumn(new Column("LocalTime", LocalTime.now(), false, false));
        Blob mockBlob = mock(Blob.class);
        when(mockBlob.getBytes(anyLong(), anyInt())).thenReturn(new byte[0]);
        dataRecord.addColumn(new Column("Blob", mockBlob, false, false));
        Clob mockClob = mock(Clob.class);
        when(mockClob.getSubString(anyLong(), anyInt())).thenReturn("");
        dataRecord.addColumn(new Column("Clob", mockClob, false, false));
        dataRecord.addColumn(new Column("Timestamp", new Timestamp(System.currentTimeMillis()), false, false));
        dataRecord.setTableName("t_order");
        dataRecord.setType("INSERT");
        Record originRecord = DataRecordResultConvertUtil.convertDataRecordToRecord("test", null, dataRecord);
        TypeRegistry registry = TypeRegistry.newBuilder().add(CDCResponseProtocol.getDescriptor().getFile().getMessageTypes()).add(WrappersProto.getDescriptor().getMessageTypes())
                .add(TimestampProto.getDescriptor().getMessageTypes()).build();
        String print = JsonFormat.printer().usingTypeRegistry(registry).print(originRecord);
        Builder newRecord = Record.newBuilder();
        JsonFormat.parser().usingTypeRegistry(registry).merge(print, newRecord);
        assertEquals(newRecord.build(), originRecord);
    }
}
