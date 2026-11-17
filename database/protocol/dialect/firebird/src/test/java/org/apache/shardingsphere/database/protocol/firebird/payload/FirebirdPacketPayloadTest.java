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
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
    
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, 258, -1, Short.MIN_VALUE, Short.MAX_VALUE})
    void assertReadInt2Signed(final short value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Short.BYTES).order(ByteOrder.BIG_ENDIAN).putShort(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt2Signed(), is((int) value));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
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
    
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, 258, -1, Short.MIN_VALUE, Short.MAX_VALUE})
    void assertReadInt2SignedLE(final short value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt2SignedLE(), is((int) value));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
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
    
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 0x01020304, -1, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void assertReadInt4LE(final int value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt4LE(), is(value));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertReadInt4Unsigned() {
        when(byteBuf.readUnsignedInt()).thenReturn(1L);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt4Unsigned(), is(1L));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 0x01020304, -1, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void assertReadInt4UnsignedLE(final int value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt4UnsignedLE(), is(Integer.toUnsignedLong(value)));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertReadBlobHandle() {
        when(byteBuf.readInt()).thenReturn(0xFFFFFFFF);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readBlobHandle(), is(0xFFFF));
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
    
    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 0x0102030405060708L, -1L, Long.MIN_VALUE, Long.MAX_VALUE})
    void assertReadInt8LE(final long value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt8LE(), is(value));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
    }
    
    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 0x0102030405060708L, -1L, Long.MIN_VALUE, Long.MAX_VALUE})
    void assertReadInt8Unsigned(final long value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt8Unsigned(), is(new BigInteger(Long.toUnsignedString(value))));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
    }
    
    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 0x0102030405060708L, -1L, Long.MIN_VALUE, Long.MAX_VALUE})
    void assertReadInt8UnsignedLE(final long value) {
        ByteBuf input = Unpooled.wrappedBuffer(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array());
        try {
            assertThat(new FirebirdPacketPayload(input, StandardCharsets.UTF_8).readInt8UnsignedLE(), is(new BigInteger(Long.toUnsignedString(value))));
            assertThat(input.readableBytes(), is(0));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertWriteInt8() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt8(1L);
        verify(byteBuf).writeLong(1L);
    }
    
    @Test
    void assertWriteInt8LE() {
        ByteBuf actual = Unpooled.buffer();
        try {
            new FirebirdPacketPayload(actual, StandardCharsets.UTF_8).writeInt8LE(0x0102030405060708L);
            assertThat(actual.readableBytes(), is(8));
            byte[] actualBytes = new byte[8];
            actual.readBytes(actualBytes);
            assertThat(actualBytes, is(new byte[]{8, 7, 6, 5, 4, 3, 2, 1}));
        } finally {
            actual.release();
        }
    }
    
    @Test
    void assertWriteBytes() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytes("value".getBytes());
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertReadBytes() {
        ByteBuf sliced = mock(ByteBuf.class);
        when(byteBuf.readSlice(4)).thenReturn(sliced);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readBytes(4), is(sliced));
    }
    
    @Test
    void assertReadBuffer() {
        ByteBuf sliced = mock(ByteBuf.class);
        when(byteBuf.readSlice(0)).thenReturn(sliced);
        assertThat(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).readBuffer(), is(sliced));
    }
    
    @Test
    void assertWriteBuffer() {
        ByteBuf value = mock(ByteBuf.class);
        when(value.writerIndex()).thenReturn(4);
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBuffer(value);
        verify(byteBuf).writeInt(4);
        verify(byteBuf).writeBytes(value);
        verify(byteBuf).writeZero(0);
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
        verify(byteBuf).writeZero(3);
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
    void assertWritePadding() {
        new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8).writePadding(5);
        verify(byteBuf).writeZero(3);
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
