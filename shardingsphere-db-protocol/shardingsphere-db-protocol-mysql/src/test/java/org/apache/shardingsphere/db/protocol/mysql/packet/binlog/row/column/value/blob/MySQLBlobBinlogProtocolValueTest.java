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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.blob;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
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
public final class MySQLBlobBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @Before
    public void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_STRING);
    }
    
    @Test
    public void assertReadWithMeta1() {
        columnDef.setColumnMeta(1);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xff);
        when(payload.readStringFixByBytes(0xff)).thenReturn(new byte[255]);
        assertThat(new MySQLBlobBinlogProtocolValue().read(columnDef, payload), is(new byte[255]));
    }
    
    @Test
    public void assertReadWithMeta2() {
        columnDef.setColumnMeta(2);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShortLE()).thenReturn(0xffff);
        when(payload.readStringFixByBytes(0xffff)).thenReturn(new byte[65535]);
        assertThat(new MySQLBlobBinlogProtocolValue().read(columnDef, payload), is(new byte[65535]));
    }
    
    @Test
    public void assertReadWithMeta3() {
        columnDef.setColumnMeta(3);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(0xffffff);
        when(payload.readStringFixByBytes(0xffffff)).thenReturn(new byte[255]);
        assertThat(new MySQLBlobBinlogProtocolValue().read(columnDef, payload), is(new byte[255]));
    }
    
    @Test
    public void assertReadWithMeta4() {
        columnDef.setColumnMeta(4);
        when(payload.readInt4()).thenReturn(Integer.MAX_VALUE);
        when(payload.readStringFixByBytes(Integer.MAX_VALUE)).thenReturn(new byte[255]);
        assertThat(new MySQLBlobBinlogProtocolValue().read(columnDef, payload), is(new byte[255]));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertReadWithUnknownMetaValue() {
        columnDef.setColumnMeta(5);
        new MySQLBlobBinlogProtocolValue().read(columnDef, payload);
    }
}
