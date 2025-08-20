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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLColumnDefinition41PacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertWriteWithPayload() {
        when(payload.readInt1()).thenReturn(MySQLBinaryColumnType.LONG.getValue(), 0);
        when(payload.readInt2()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId(), 0);
        when(payload.readInt4()).thenReturn(10);
        when(payload.readIntLenenc()).thenReturn(0x0cL);
        when(payload.readStringLenenc()).thenReturn("def", "logic_db", "tbl", "tbl", "id", "id");
        MySQLColumnDefinition41Packet actual = new MySQLColumnDefinition41Packet(payload);
        actual.write(payload);
        verifyWrite();
    }
    
    private void verifyWrite() {
        verify(payload).writeStringLenenc("def");
        verify(payload).writeStringLenenc("logic_db");
        verify(payload, times(2)).writeStringLenenc("tbl");
        verify(payload, times(2)).writeStringLenenc("id");
        verify(payload).writeIntLenenc(0x0c);
        verify(payload).writeInt2(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeInt4(10);
        verify(payload).writeInt1(MySQLBinaryColumnType.LONG.getValue());
        verify(payload).writeInt2(0);
        verify(payload).writeInt1(0);
        verify(payload).writeReserved(2);
    }
}
