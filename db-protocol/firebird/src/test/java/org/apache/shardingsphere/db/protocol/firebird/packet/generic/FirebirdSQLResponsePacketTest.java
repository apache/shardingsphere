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

package org.apache.shardingsphere.db.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.binary.BinaryRow;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

class FirebirdSQLResponsePacketTest {
    
    @Test
    void assertWriteWithRow() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.writerIndex()).thenReturn(0);
        when(byteBuf.writeZero(4)).thenReturn(byteBuf);
        BinaryRow row = new BinaryRow(Collections.singleton(new BinaryCell(FirebirdBinaryColumnType.LONG, 5)));
        FirebirdBinaryProtocolValue protocolValue = mock(FirebirdBinaryProtocolValue.class);
        try (MockedStatic<FirebirdBinaryProtocolValueFactory> mocked = mockStatic(FirebirdBinaryProtocolValueFactory.class)) {
            mocked.when(() -> FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.LONG)).thenReturn(protocolValue);
            FirebirdSQLResponsePacket packet = new FirebirdSQLResponsePacket(row);
            packet.write(payload);
            verify(payload).writeInt4(FirebirdCommandPacketType.SQL_RESPONSE.getValue());
            verify(payload).writeInt4(1);
            verify(byteBuf).writeZero(4);
            verify(protocolValue).write(payload, 5);
        }
    }
    
    @Test
    void assertWriteWithoutRow() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        FirebirdSQLResponsePacket packet = new FirebirdSQLResponsePacket();
        packet.write(payload);
        verify(payload).writeInt4(FirebirdCommandPacketType.SQL_RESPONSE.getValue());
        verify(payload).writeInt4(0);
        verify(payload, never()).getByteBuf();
    }
}
