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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        assertThat(packet.getBlobParameterBuffer().length, is(0));
    }
    
    @Test
    void assertCreateBlobPacketWithBpb() {
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4}));
        when(payload.readInt4()).thenReturn(7);
        when(payload.readInt8()).thenReturn(11L);
        FirebirdCreateBlobCommandPacket packet = new FirebirdCreateBlobCommandPacket(FirebirdCommandPacketType.CREATE_BLOB2, payload);
        verify(payload).skipReserved(4);
        verify(payload).readBuffer();
        assertThat(packet.getTransactionId(), is(7));
        assertThat(packet.getRequestedBlobId(), is(11L));
        assertThat(packet.getBlobParameterBuffer(), is(new byte[]{1, 2, 3, 4}));
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
