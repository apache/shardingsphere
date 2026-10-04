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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdCreateBlobCommandPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertCreateBlobPacketWithoutBpb() {
        when(payload.readInt4()).thenReturn(42);
        when(payload.readInt8()).thenReturn(0x0102030405060708L);
        FirebirdCreateBlobCommandPacket packet = new FirebirdCreateBlobCommandPacket(FirebirdCommandPacketType.CREATE_BLOB, payload);
        verify(payload).skipReserved(4);
        verify(payload, never()).readBuffer();
        assertThat(packet.getTransactionId(), is(42));
        assertThat(packet.getRequestedBlobId(), is(0x0102030405060708L));
        assertFalse(packet.isStreamBlob());
        packet.write(payload);
        verify(payload).readInt4();
        verify(payload).readInt8();
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertCreateBlobPacketWithBpb() {
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{1, 3, 4, 1, 0, 0, 0}));
        when(payload.readInt4()).thenReturn(7);
        when(payload.readInt8()).thenReturn(11L);
        FirebirdCreateBlobCommandPacket packet = new FirebirdCreateBlobCommandPacket(FirebirdCommandPacketType.CREATE_BLOB2, payload);
        verify(payload).skipReserved(4);
        verify(payload).readBuffer();
        assertThat(packet.getTransactionId(), is(7));
        assertThat(packet.getRequestedBlobId(), is(11L));
        assertTrue(packet.isStreamBlob());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobParameterBufferCases")
    void assertIsStreamBlob(final String name, final byte[] blobParameterBuffer, final boolean expectedStreamBlob) {
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(blobParameterBuffer));
        FirebirdCreateBlobCommandPacket packet = new FirebirdCreateBlobCommandPacket(FirebirdCommandPacketType.CREATE_BLOB2, payload);
        assertThat(packet.isStreamBlob(), is(expectedStreamBlob));
    }
    
    private static Stream<Arguments> blobParameterBufferCases() {
        return Stream.of(
                Arguments.of("empty buffer", new byte[0], false),
                Arguments.of("version without items", new byte[]{1}, false),
                Arguments.of("stream type of one byte", new byte[]{1, 3, 1, 1}, true),
                Arguments.of("stream type of two bytes", new byte[]{1, 3, 2, 1, 0}, true),
                Arguments.of("stream type of four bytes", new byte[]{1, 3, 4, 1, 0, 0, 0}, true),
                Arguments.of("segmented type of one byte", new byte[]{1, 3, 1, 0}, false),
                Arguments.of("type after another item of one byte", new byte[]{1, 1, 1, 7, 3, 1, 1}, true),
                Arguments.of("buffer without type item", new byte[]{1, 1, 1, 7}, false));
    }
    
    @Test
    void assertGetLengthWithoutBpb() {
        assertThat(FirebirdCreateBlobCommandPacket.getLength(FirebirdCommandPacketType.CREATE_BLOB, payload), is(16));
    }
    
    @Test
    void assertGetLengthWithBpb() {
        when(payload.getBufferLength(4)).thenReturn(12);
        assertThat(FirebirdCreateBlobCommandPacket.getLength(FirebirdCommandPacketType.CREATE_BLOB2, payload), is(28));
        verify(payload).getBufferLength(4);
    }
    
}
