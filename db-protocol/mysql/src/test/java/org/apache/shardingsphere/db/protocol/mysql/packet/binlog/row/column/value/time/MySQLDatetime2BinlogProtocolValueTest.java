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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class MySQLDatetime2BinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    public void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_DATETIME2);
    }
    
    @Test
    public void assertReadWithoutFraction() {
        when(payload.readInt1()).thenReturn(0xfe, 0xf3, 0xff, 0x7e, 0xfb);
        LocalDateTime expected = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        assertThat(new MySQLDatetime2BinlogProtocolValue().read(columnDef, payload), is(Timestamp.valueOf(expected)));
    }
    
    @Test
    public void assertReadWithoutFraction1() {
        columnDef.setColumnMeta(1);
        when(payload.readInt1()).thenReturn(0xfe, 0xf3, 0xff, 0x7e, 0xfb, 0x00);
        LocalDateTime expected = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 0);
        assertThat(new MySQLDatetime2BinlogProtocolValue().read(columnDef, payload), is(Timestamp.valueOf(expected)));
    }
    
    @Test
    public void assertReadWithoutFraction3() {
        columnDef.setColumnMeta(3);
        when(payload.readInt1()).thenReturn(0xfe, 0xf3, 0xff, 0x7e, 0xfb);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShort()).thenReturn(9990);
        LocalDateTime expected = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999 * 1000 * 1000);
        assertThat(new MySQLDatetime2BinlogProtocolValue().read(columnDef, payload), is(Timestamp.valueOf(expected)));
    }
    
    @Test
    public void assertReadWithoutFraction5() {
        columnDef.setColumnMeta(5);
        when(payload.readInt1()).thenReturn(0xfe, 0xf3, 0xff, 0x7e, 0xfb);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(999990);
        LocalDateTime expected = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999990000);
        assertThat(new MySQLDatetime2BinlogProtocolValue().read(columnDef, payload), is(Timestamp.valueOf(expected)));
    }
    
    @Test
    public void assertReadNullTime() {
        assertThat(new MySQLDatetime2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtil.DATETIME_OF_ZERO));
    }
}
