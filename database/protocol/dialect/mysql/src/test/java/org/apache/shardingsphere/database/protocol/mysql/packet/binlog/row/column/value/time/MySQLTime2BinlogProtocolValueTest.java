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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTime2BinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.TIME2);
        columnDef.setColumnMeta(0);
    }
    
    @Test
    void assertRead() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4)));
    }
    
    @Test
    void assertReadWithFraction1() {
        columnDef.setColumnMeta(1);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(payload.readInt1()).thenReturn(90);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4).withNano(900000000)));
    }
    
    @Test
    void assertReadWithFraction3() {
        columnDef.setColumnMeta(3);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShort()).thenReturn(9000);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4).withNano(900000000)));
    }
    
    @Test
    void assertReadWithFraction6() {
        columnDef.setColumnMeta(6);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04, 10123);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4).withNano(10123000)));
    }
    
    @Test
    void assertReadNullTime() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtils.ZERO_OF_TIME));
    }
}
