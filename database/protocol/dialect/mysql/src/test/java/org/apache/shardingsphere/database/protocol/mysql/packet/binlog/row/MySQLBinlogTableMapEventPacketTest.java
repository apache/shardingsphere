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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.MySQLNullBitmap;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBinlogTableMapEventPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private MySQLBinlogEventHeader binlogEventHeader;
    
    @Test
    void assertNew() {
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(0, 255);
        when(payload.readInt1()).thenReturn(4, 4, MySQLBinaryColumnType.LONGLONG.getValue(), MySQLBinaryColumnType.VARCHAR.getValue(),
                MySQLBinaryColumnType.NEWDECIMAL.getValue(), MySQLBinaryColumnType.DATETIME2.getValue(), 11,
                0x0e);
        when(payload.readStringFix(4)).thenReturn("test");
        when(payload.readIntLenenc()).thenReturn(4L);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShort()).thenReturn(10);
        when(byteBuf.readerIndex()).thenReturn(1);
        MySQLBinlogTableMapEventPacket actual = new MySQLBinlogTableMapEventPacket(binlogEventHeader, payload);
        assertThat(actual.getTableId(), is(1L));
        assertThat(actual.getFlags(), is(0));
        assertThat(actual.getSchemaName(), is("test"));
        assertThat(actual.getTableName(), is("test"));
        verify(payload, times(2)).skipReserved(1);
        assertThat(actual.getColumnCount(), is(4));
        assertColumnDefs(actual.getColumnDefs());
        assertNullBitmap(actual.getNullBitMap());
    }
    
    private void assertColumnDefs(final Collection<MySQLBinlogColumnDef> columnDefs) {
        assertThat(columnDefs.size(), is(4));
        Iterator<MySQLBinlogColumnDef> columnDefIterator = columnDefs.iterator();
        assertColumnDef(columnDefIterator.next(), MySQLBinaryColumnType.LONGLONG, 0);
        assertColumnDef(columnDefIterator.next(), MySQLBinaryColumnType.VARCHAR, 255);
        assertColumnDef(columnDefIterator.next(), MySQLBinaryColumnType.NEWDECIMAL, 10);
        assertColumnDef(columnDefIterator.next(), MySQLBinaryColumnType.DATETIME2, 11);
    }
    
    private void assertColumnDef(final MySQLBinlogColumnDef actual, final MySQLBinaryColumnType columnType, final int columnMeta) {
        assertThat(actual.getColumnType(), is(columnType));
        assertThat(actual.getColumnMeta(), is(columnMeta));
    }
    
    private void assertNullBitmap(final MySQLNullBitmap actualNullBitMap) {
        assertFalse(actualNullBitMap.isNullParameter(0));
        assertTrue(actualNullBitMap.isNullParameter(1));
        assertTrue(actualNullBitMap.isNullParameter(2));
        assertTrue(actualNullBitMap.isNullParameter(3));
    }
}
