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
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBinlogRowsEventPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private MySQLBinlogEventHeader binlogEventHeader;
    
    private List<MySQLBinlogColumnDef> columnDefs;
    
    @BeforeEach
    void setUp() {
        columnDefs = Collections.singletonList(new MySQLBinlogColumnDef(MySQLBinaryColumnType.LONGLONG));
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(2);
        when(payload.readIntLenenc()).thenReturn(1L);
    }
    
    @Test
    void assertReadWriteRowV1WithoutNullValue() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        assertBinlogRowsEventV1BeforeRows(actual);
        assertFalse(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertNull(actual.getColumnsPresentBitmap2());
        MySQLPacketPayload packetPayload = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(((Serializable[]) Plugins.getMemberAccessor()
                .invoke(MySQLBinlogRowsEventPacket.class.getDeclaredMethod("readRow", List.class, MySQLPacketPayload.class), actual, columnDefs, packetPayload))[0], is(0L));
    }
    
    private void assertBinlogRowsEventV1BeforeRows(final MySQLBinlogRowsEventPacket actual) {
        assertThat(actual.getTableId(), is(1L));
        assertThat(actual.getFlags(), is(2));
        verify(payload, never()).skipReserved(2);
        assertThat(actual.getColumnNumber(), is(1));
    }
}
