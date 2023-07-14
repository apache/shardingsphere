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

import com.google.protobuf.EmptyProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TimestampProto;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.WrappersProto;
import com.google.protobuf.util.JsonFormat;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.Builder;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.IntegerPrimaryKeyPosition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataRecordResultConvertUtilsTest {
    
    @Test
    void assertConvertDataRecordToRecord() throws InvalidProtocolBufferException, SQLException {
        DataRecord dataRecord = new DataRecord("INSERT", "t_order", new IntegerPrimaryKeyPosition(0, 1), 2);
        dataRecord.addColumn(new Column("order_id", BigInteger.ONE, false, true));
        dataRecord.addColumn(new Column("price", BigDecimal.valueOf(123), false, false));
        dataRecord.addColumn(new Column("user_id", Long.MAX_VALUE, false, false));
        dataRecord.addColumn(new Column("item_id", Integer.MAX_VALUE, false, false));
        dataRecord.addColumn(new Column("create_date", LocalDate.now(), false, false));
        dataRecord.addColumn(new Column("create_date2", Date.valueOf(LocalDate.now()), false, false));
        dataRecord.addColumn(new Column("create_time", LocalTime.now(), false, false));
        dataRecord.addColumn(new Column("create_time2", OffsetTime.now(), false, false));
        dataRecord.addColumn(new Column("create_datetime", LocalDateTime.now(), false, false));
        dataRecord.addColumn(new Column("create_datetime2", OffsetDateTime.now(), false, false));
        dataRecord.addColumn(new Column("empty", null, false, false));
        Blob mockedBlob = mock(Blob.class);
        when(mockedBlob.getBytes(anyLong(), anyInt())).thenReturn(new byte[]{-1, 0, 1});
        dataRecord.addColumn(new Column("data_blob", mockedBlob, false, false));
        Clob mockedClob = mock(Clob.class);
        when(mockedClob.getSubString(anyLong(), anyInt())).thenReturn("clob\n");
        dataRecord.addColumn(new Column("text_clob", mockedClob, false, false));
        dataRecord.addColumn(new Column("update_time", new Timestamp(System.currentTimeMillis()), false, false));
        TypeRegistry registry = TypeRegistry.newBuilder().add(EmptyProto.getDescriptor().getMessageTypes()).add(TimestampProto.getDescriptor().getMessageTypes())
                .add(WrappersProto.getDescriptor().getMessageTypes()).build();
        Record expectedRecord = DataRecordResultConvertUtils.convertDataRecordToRecord("test", null, dataRecord);
        String print = JsonFormat.printer().usingTypeRegistry(registry).print(expectedRecord);
        Builder actualRecord = Record.newBuilder();
        JsonFormat.parser().usingTypeRegistry(registry).merge(print, actualRecord);
        assertThat(actualRecord.build(), is(expectedRecord));
    }
}
