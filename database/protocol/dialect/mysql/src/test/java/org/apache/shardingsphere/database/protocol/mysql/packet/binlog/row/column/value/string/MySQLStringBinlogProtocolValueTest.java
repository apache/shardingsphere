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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLStringBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.STRING);
    }
    
    @Test
    void assertReadEnumValueWithMeta1() {
        columnDef.setColumnMeta((MySQLBinaryColumnType.ENUM.getValue() << 8) + 1);
        when(payload.readInt1()).thenReturn(1);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is(1));
    }
    
    @Test
    void assertReadEnumValueWithMeta2() {
        columnDef.setColumnMeta((MySQLBinaryColumnType.ENUM.getValue() << 8) + 2);
        when(payload.readInt2()).thenReturn(32767);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is(32767));
    }
    
    @Test
    void assertReadEnumValueWithMetaFailure() {
        columnDef.setColumnMeta((MySQLBinaryColumnType.ENUM.getValue() << 8) + 3);
        assertThrows(UnsupportedSQLOperationException.class, () -> new MySQLStringBinlogProtocolValue().read(columnDef, payload));
    }
    
    @Test
    void assertReadSetValue() {
        columnDef.setColumnMeta(MySQLBinaryColumnType.SET.getValue() << 8);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readByte()).thenReturn((byte) 0xff);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is((byte) 0xff));
    }
    
    @Test
    void assertReadStringValue() {
        String expected = "test_value";
        columnDef.setColumnMeta(MySQLBinaryColumnType.STRING.getValue() << 8);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedByte()).thenReturn((short) expected.length());
        when(payload.readStringFixByBytes(expected.length())).thenReturn(expected.getBytes());
        Serializable actual = new MySQLStringBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual, isA(MySQLBinaryString.class));
        assertThat(((MySQLBinaryString) actual).getBytes(), is(expected.getBytes()));
    }
    
    @Test
    void assertReadLongStringValue() {
        String expected = "test_value";
        columnDef.setColumnMeta((MySQLBinaryColumnType.STRING.getValue() ^ ((256 & 0x300) >> 4)) << 8);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShortLE()).thenReturn(expected.length());
        when(payload.readStringFixByBytes(expected.length())).thenReturn(expected.getBytes());
        Serializable actual = new MySQLStringBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual, isA(MySQLBinaryString.class));
        assertThat(((MySQLBinaryString) actual).getBytes(), is(expected.getBytes()));
    }
    
    @Test
    void assertReadValueWithUnknownType() {
        columnDef.setColumnMeta(MySQLBinaryColumnType.VAR_STRING.getValue() << 8);
        assertThrows(UnsupportedSQLOperationException.class, () -> new MySQLStringBinlogProtocolValue().read(columnDef, payload));
    }
}
