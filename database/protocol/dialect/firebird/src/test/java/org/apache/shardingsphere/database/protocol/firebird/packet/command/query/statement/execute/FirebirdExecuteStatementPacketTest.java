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
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdExecuteStatementPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteStatementPacketArguments")
    void assertExecuteStatementPacket(final String name, final short blrType, final FirebirdBinaryColumnType expectedParameterType, final Object expectedParameterValue) {
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 1, 123);
        when(payload.readInt1()).thenReturn(0);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 5, (short) 0, blrType, (short) BlrConstants.blr_end);
        when(byteBuf.skipBytes(anyInt())).thenReturn(byteBuf);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        assertThat(packet.getStatementId(), is(1));
        assertThat(packet.getTransactionId(), is(2));
        assertThat(packet.getParameterTypes(), is(Collections.singletonList(expectedParameterType)));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(expectedParameterValue)));
    }
    
    @Test
    void assertExecuteStatementPacketWithNullParameterValue() {
        when(payload.readInt1()).thenReturn(1);
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 1);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 5, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_end);
        when(byteBuf.skipBytes(anyInt())).thenReturn(byteBuf);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        assertThat(packet.getParameterTypes(), is(Collections.singletonList(FirebirdBinaryColumnType.LONG)));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertExecuteStatementPacketWhenBLRIsNotReadable() {
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 0);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        assertThat(packet.getParameterTypes(), is(Collections.emptyList()));
        assertThat(packet.getParameterValues(), is(Collections.emptyList()));
    }
    
    @Test
    void assertExecuteStatementPacketForStoredProcedure() {
        when(payload.readInt1()).thenReturn(0);
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE2.getValue(), 1, 2, 0, 1, 123, 9);
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
        assertThat(packet.getReturnColumns(), is(Collections.singletonList(FirebirdBinaryColumnType.LONG)));
        assertThat(packet.getOutputMessageNumber(), is(9));
        assertThat(packet.getParameterValues(), is(Collections.singletonList(123)));
        assertThat(packet.getStatementTimeout(), is(30L));
        assertThat(packet.getCursorFlags(), is(1L));
        assertThat(packet.getMaxBlobSize(), is(1024L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsStoredProcedureArguments")
    void assertIsStoredProcedure(final String name, final FirebirdCommandPacketType commandPacketType, final boolean expectedStoredProcedure) {
        when(payload.readInt4()).thenReturn(commandPacketType.getValue(), 1, 2, 0, 0);
        when(payload.readBuffer()).thenReturn(byteBuf);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        assertThat(packet.isStoredProcedure(), is(expectedStoredProcedure));
    }
    
    @Test
    void assertWrite() {
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 0);
        FirebirdExecuteStatementPacket packet = new FirebirdExecuteStatementPacket(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        FirebirdPacketPayload writePayload = mock(FirebirdPacketPayload.class);
        packet.write((PacketPayload) writePayload);
        verifyNoInteractions(writePayload);
    }
    
    @Test
    void assertGetLength() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue(), 1, 2, 0, 1, 123);
        when(payload.readInt1()).thenReturn(0);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 5, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_end);
        when(byteBuf.skipBytes(anyInt())).thenReturn(byteBuf);
        when(byteBuf.readerIndex()).thenReturn(42);
        assertThat(FirebirdExecuteStatementPacket.getLength(payload, FirebirdProtocolVersion.PROTOCOL_VERSION13), is(42));
        verify(byteBuf).resetReaderIndex();
    }
    
    private static Stream<Arguments> assertExecuteStatementPacketArguments() {
        return Stream.of(
                Arguments.of("skip_count_4", (short) BlrConstants.blr_varying2, FirebirdBinaryColumnType.VARYING, null),
                Arguments.of("skip_count_2", (short) BlrConstants.blr_text, FirebirdBinaryColumnType.LEGACY_TEXT, null),
                Arguments.of("skip_count_1", (short) BlrConstants.blr_long, FirebirdBinaryColumnType.LONG, 123),
                Arguments.of("skip_count_0", (short) BlrConstants.blr_bool, FirebirdBinaryColumnType.BOOLEAN, 0),
                Arguments.of("blob_parameter", (short) BlrConstants.blr_quad, FirebirdBinaryColumnType.BLOB, 0L));
    }
    
    private static Stream<Arguments> assertIsStoredProcedureArguments() {
        return Stream.of(
                Arguments.of("execute", FirebirdCommandPacketType.EXECUTE, false),
                Arguments.of("execute2", FirebirdCommandPacketType.EXECUTE2, true),
                Arguments.of("fetch", FirebirdCommandPacketType.FETCH, false));
    }
}
