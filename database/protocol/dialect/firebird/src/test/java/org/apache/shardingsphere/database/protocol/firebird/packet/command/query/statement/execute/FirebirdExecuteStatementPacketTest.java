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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdExecuteStatementPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertExecuteStatementPacket() {
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 1, 123);
        when(payload.readInt1()).thenReturn(0);
        doNothing().when(payload).skipPadding(anyInt());
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 5, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_end);
        when(byteBuf.skipBytes(anyInt())).thenReturn(byteBuf);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        assertThat(packet.getType(), is(FirebirdCommandPacketType.EXECUTE));
        assertThat(packet.getStatementId(), is(1));
        assertThat(packet.getTransactionId(), is(2));
        assertThat(packet.getParameterTypes(), is(Collections.singletonList(FirebirdBinaryColumnType.LONG)));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(123)));
    }
    
    @Test
    void assertExecuteStatementPacketForStoredProcedure() {
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE2.getValue(), 1, 2, 0, 1, 123, 9);
        when(payload.readInt1()).thenReturn(0);
        doNothing().when(payload).skipPadding(anyInt());
        ByteBuf returnBlr = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(byteBuf, returnBlr);
        when(byteBuf.isReadable()).thenReturn(true);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 5, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_end);
        when(byteBuf.skipBytes(anyInt())).thenReturn(byteBuf);
        when(returnBlr.isReadable()).thenReturn(true);
        when(returnBlr.readUnsignedByte()).thenReturn((short) 5, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_end);
        when(returnBlr.skipBytes(anyInt())).thenReturn(returnBlr);
        when(payload.readInt4Unsigned()).thenReturn(30L, 1L, 1024L);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION19);
        assertThat(packet.isStoredProcedure(), is(true));
        assertThat(packet.getReturnColumns(), is(Collections.singletonList(FirebirdBinaryColumnType.LONG)));
        assertThat(packet.getOutputMessageNumber(), is(9));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(123)));
        assertThat(packet.getStatementTimeout(), is(30L));
        assertThat(packet.getCursorFlags(), is(1L));
        assertThat(packet.getMaxBlobSize(), is(1024L));
    }
    
    @Test
    void assertGetLength() {
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 1, 123);
        when(payload.readInt1()).thenReturn(0);
        doNothing().when(payload).skipPadding(anyInt());
        ByteBuf blr = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(blr);
        when(blr.isReadable()).thenReturn(true);
        when(blr.readUnsignedByte()).thenReturn((short) 5, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_end);
        when(blr.skipBytes(anyInt())).thenReturn(blr);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readerIndex()).thenReturn(42);
        assertThat(FirebirdExecuteStatementPacket.getLength(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13), is(42));
        verify(byteBuf).resetReaderIndex();
    }
}
