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
class FirebirdOpenBlobCommandPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertOpenBlobPacketWithoutBpb() {
        when(payload.readInt4()).thenReturn(5);
        when(payload.readInt8()).thenReturn(7L);
        FirebirdOpenBlobCommandPacket packet = new FirebirdOpenBlobCommandPacket(FirebirdCommandPacketType.OPEN_BLOB, payload);
        verify(payload).skipReserved(4);
        verify(payload, never()).readBuffer();
        assertThat(packet.getBlobParameterBuffer().length, is(0));
        assertThat(packet.getTransactionId(), is(5));
        assertThat(packet.getBlobId(), is(7L));
    }
    
    @Test
    void assertOpenBlobPacketWithBpb() {
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{4, 5, 6}));
        when(payload.readInt4()).thenReturn(9);
        when(payload.readInt8()).thenReturn(12L);
        FirebirdOpenBlobCommandPacket packet = new FirebirdOpenBlobCommandPacket(FirebirdCommandPacketType.OPEN_BLOB2, payload);
        verify(payload).skipReserved(4);
        verify(payload).readBuffer();
        assertThat(packet.getBlobParameterBuffer(), is(new byte[]{4, 5, 6}));
        assertThat(packet.getTransactionId(), is(9));
        assertThat(packet.getBlobId(), is(12L));
    }
    
    @Test
    void assertGetLengthWithoutBpb() {
        assertThat(FirebirdOpenBlobCommandPacket.getLength(FirebirdCommandPacketType.OPEN_BLOB, payload), is(16));
    }
}
