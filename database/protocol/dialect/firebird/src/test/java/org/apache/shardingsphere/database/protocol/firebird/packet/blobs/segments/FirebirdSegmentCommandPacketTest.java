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

package org.apache.shardingsphere.database.protocol.firebird.packet.blobs.segments;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdSegmentCommandPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertGetSegmentPacket() {
        when(payload.readBlobHandle()).thenReturn(11);
        when(payload.readInt4()).thenReturn(3);
        when(payload.readBuffer()).thenReturn(Unpooled.EMPTY_BUFFER);
        FirebirdSegmentCommandPacket packet = new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.GET_SEGMENT, payload);
        assertThat(packet.getBlobHandle(), is(11));
        assertThat(packet.getSegmentLength(), is(3));
        assertTrue(packet.getSegments().isEmpty());
        verify(payload).skipReserved(4);
        verify(payload).readBlobHandle();
        verify(payload).readInt4();
        verify(payload).readBuffer();
        packet.write(payload);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertPutSegmentPacket() {
        when(payload.readBlobHandle()).thenReturn(15);
        when(payload.readInt4()).thenReturn(3);
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{5, 6, 7}));
        FirebirdSegmentCommandPacket packet = new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.PUT_SEGMENT, payload);
        assertThat(packet.getBlobHandle(), is(15));
        assertThat(packet.getSegmentLength(), is(3));
        assertThat(packet.getSegments().size(), is(1));
        assertThat(packet.getSegments().iterator().next(), is(new byte[]{5, 6, 7}));
        verify(payload).skipReserved(4);
        verify(payload).readBlobHandle();
        verify(payload).readInt4();
        verify(payload).readBuffer();
        packet.write(payload);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertBatchSegmentsPacket() {
        when(payload.readBlobHandle()).thenReturn(11);
        when(payload.readInt4()).thenReturn(9);
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{2, 0, 1, 2, 3, 0, 3, 4, 5}));
        FirebirdSegmentCommandPacket packet = new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.BATCH_SEGMENTS, payload);
        assertThat(packet.getBlobHandle(), is(11));
        assertThat(packet.getSegmentLength(), is(9));
        assertThat(packet.getSegments().size(), is(2));
        Iterator<byte[]> iterator = packet.getSegments().iterator();
        assertThat(iterator.next(), is(new byte[]{1, 2}));
        assertThat(iterator.next(), is(new byte[]{3, 4, 5}));
        verify(payload).skipReserved(4);
        verify(payload).readBlobHandle();
        verify(payload).readInt4();
        verify(payload).readBuffer();
        packet.write(payload);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(12)).thenReturn(20);
        assertThat(FirebirdSegmentCommandPacket.getLength(payload), is(32));
        verify(payload).getBufferLength(12);
    }
    
    @Test
    void assertRejectsTruncatedSegmentLength() {
        when(payload.readBlobHandle()).thenReturn(11);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{1}));
        assertThrows(DatabaseProtocolException.class, () -> new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.BATCH_SEGMENTS, payload));
    }
    
    @Test
    void assertRejectsSegmentLargerThanRemainingBuffer() {
        when(payload.readBlobHandle()).thenReturn(11);
        when(payload.readInt4()).thenReturn(3);
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{3, 0, 1}));
        assertThrows(DatabaseProtocolException.class, () -> new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.BATCH_SEGMENTS, payload));
    }
    
    @Test
    void assertReadEmptyBatch() {
        when(payload.readBlobHandle()).thenReturn(11);
        when(payload.readInt4()).thenReturn(0);
        when(payload.readBuffer()).thenReturn(Unpooled.EMPTY_BUFFER);
        assertTrue(new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.BATCH_SEGMENTS, payload).getSegments().isEmpty());
    }
    
    @Test
    void assertReadZeroLengthSegment() {
        when(payload.readBlobHandle()).thenReturn(11);
        when(payload.readInt4()).thenReturn(2);
        when(payload.readBuffer()).thenReturn(Unpooled.wrappedBuffer(new byte[]{0, 0}));
        Collection<byte[]> actual = new FirebirdSegmentCommandPacket(FirebirdCommandPacketType.BATCH_SEGMENTS, payload).getSegments();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(new byte[0]));
    }
}
