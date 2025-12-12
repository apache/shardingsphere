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

package org.apache.shardingsphere.database.protocol.firebird.payload;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdPacketPayloadTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertReadInt1Unsigned() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt1Unsigned(), is(1));
    }
    
    @Test
    void assertReadInt1() {
        when(byteBuf.readByte()).thenReturn((byte) 1);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt1(), is(1));
    }
    
    @Test
    void assertWriteInt1() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt1(1);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    void assertReadInt2() {
        when(byteBuf.readUnsignedShort()).thenReturn(1);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt2(), is(1));
    }
    
    @Test
    void assertWriteInt2() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt2(1);
        verify(byteBuf).writeShort(1);
    }
    
    @Test
    void assertReadInt2LE() {
        when(byteBuf.readUnsignedShortLE()).thenReturn(1);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt2LE(), is(1));
    }
    
    @Test
    void assertWriteInt2LE() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt2LE(1);
        verify(byteBuf).writeShortLE(1);
    }
    
    @Test
    void assertReadInt4() {
        when(byteBuf.readInt()).thenReturn(1);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt4(), is(1));
    }
    
    @Test
    void assertReadInt4Unsigned() {
        when(byteBuf.readUnsignedInt()).thenReturn(1L);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt4Unsigned(), is(1L));
    }
    
    @Test
    void assertWriteInt4() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt4(1);
        verify(byteBuf).writeInt(1);
    }
    
    @Test
    void assertWriteInt4LE() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt4LE(1);
        verify(byteBuf).writeIntLE(1);
    }
    
    @Test
    void assertReadInt8() {
        when(byteBuf.readLong()).thenReturn(1L);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt8(), is(1L));
    }
    
    @Test
    void assertWriteInt8() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt8(1L);
        verify(byteBuf).writeLong(1L);
    }
    
    @Test
    void assertWriteBytes() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytes("value".getBytes());
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertReadBuffer() {
        when(byteBuf.readInt()).thenReturn(4);
        ByteBuf sliced = mock(ByteBuf.class);
        when(byteBuf.readSlice(4)).thenReturn(sliced);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readBuffer(), is(sliced));
    }
    
    @Test
    void assertWriteBuffer() {
        ByteBuf value = mock(ByteBuf.class);
        when(value.writerIndex()).thenReturn(4);
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBuffer(value);
        verify(byteBuf).writeInt(4);
        verify(byteBuf).writeBytes(value);
        verify(byteBuf).writeBytes(new byte[0]);
    }
    
    @Test
    void assertReadString() {
        when(byteBuf.readInt()).thenReturn(5);
        ByteBuf sliced = mock(ByteBuf.class);
        when(byteBuf.readSlice(5)).thenReturn(sliced);
        when(sliced.toString(StandardCharsets.UTF_8)).thenReturn("value");
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readString(), is("value"));
    }
    
    @Test
    void assertWriteString() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeString("value");
        verify(byteBuf).writeInt(5);
        verify(byteBuf).writeBytes("value".getBytes(StandardCharsets.UTF_8));
        verify(byteBuf).writeBytes(new byte[3]);
    }
    
    @Test
    void assertSkipReserved() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).skipReserved(8);
        verify(byteBuf).skipBytes(8);
    }
    
    @Test
    void assertSkipPadding() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).skipPadding(5);
        verify(byteBuf).skipBytes(3);
    }
    
    @Test
    void assertGetPadding() {
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).getPadding(5), is(3));
    }
    
    @Test
    void assertGetBufferLength() {
        when(byteBuf.getInt(0)).thenReturn(5);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).getBufferLength(0), is(12));
    }
}
