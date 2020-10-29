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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLStringBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @Before
    public void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLColumnType.MYSQL_TYPE_STRING);
    }
    
    @Test
    public void assertReadEnumValueWithMeta1() {
        columnDef.setColumnMeta((MySQLColumnType.MYSQL_TYPE_ENUM.getValue() << 8) + 1);
        when(payload.readInt1()).thenReturn(1);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is(1));
    }
    
    @Test
    public void assertReadEnumValueWithMeta2() {
        columnDef.setColumnMeta((MySQLColumnType.MYSQL_TYPE_ENUM.getValue() << 8) + 2);
        when(payload.readInt2()).thenReturn(32767);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is(32767));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertReadEnumValueWithMetaFailure() {
        columnDef.setColumnMeta((MySQLColumnType.MYSQL_TYPE_ENUM.getValue() << 8) + 3);
        new MySQLStringBinlogProtocolValue().read(columnDef, payload);
    }
    
    @Test
    public void assertReadSetValue() {
        columnDef.setColumnMeta(MySQLColumnType.MYSQL_TYPE_SET.getValue() << 8);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readByte()).thenReturn((byte) 0xff);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is((byte) 0xff));
    }
    
    @Test
    public void assertReadStringValue() {
        String expected = "test_value";
        columnDef.setColumnMeta(MySQLColumnType.MYSQL_TYPE_STRING.getValue() << 8);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedByte()).thenReturn((short) expected.length());
        when(payload.readStringFix(expected.length())).thenReturn(expected);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is(expected));
    }
    
    @Test
    public void assertReadLongStringValue() {
        String expected = "test_value";
        columnDef.setColumnMeta((MySQLColumnType.MYSQL_TYPE_STRING.getValue() ^ ((256 & 0x300) >> 4)) << 8);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShortLE()).thenReturn(expected.length());
        when(payload.readStringFix(expected.length())).thenReturn(expected);
        assertThat(new MySQLStringBinlogProtocolValue().read(columnDef, payload), is(expected));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertReadValueWithUnknownType() {
        columnDef.setColumnMeta(MySQLColumnType.MYSQL_TYPE_VAR_STRING.getValue() << 8);
        new MySQLStringBinlogProtocolValue().read(columnDef, payload);
    }
}
