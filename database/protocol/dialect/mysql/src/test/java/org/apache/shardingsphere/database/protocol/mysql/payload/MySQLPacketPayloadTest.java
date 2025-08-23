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

package org.apache.shardingsphere.database.protocol.mysql.payload;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLPacketPayloadTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertReadInt1() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt1(), is(1));
    }
    
    @Test
    void assertWriteInt1() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt1(1);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    void assertReadInt2() {
        when(byteBuf.readUnsignedShortLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt2(), is(1));
    }
    
    @Test
    void assertWriteInt2() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt2(1);
        verify(byteBuf).writeShortLE(1);
    }
    
    @Test
    void assertReadInt3() {
        when(byteBuf.readUnsignedMediumLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt3(), is(1));
    }
    
    @Test
    void assertWriteInt3() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt3(1);
        verify(byteBuf).writeMediumLE(1);
    }
    
    @Test
    void assertReadInt4() {
        when(byteBuf.readIntLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt4(), is(1));
    }
    
    @Test
    void assertWriteInt4() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt4(1);
        verify(byteBuf).writeIntLE(1);
    }
    
    @Test
    void assertReadInt6() {
        when(byteBuf.readByte()).thenReturn((byte) 0x01, (byte) 0x00);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt6(), is(1L));
        when(byteBuf.readByte()).thenReturn((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt6(), is(0x800000000000L));
    }
    
    @Test
    void assertWriteInt6() {
        assertDoesNotThrow(() -> new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt6(1L));
    }
    
    @Test
    void assertReadInt8() {
        when(byteBuf.readLongLE()).thenReturn(1L);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt8(), is(1L));
    }
    
    @Test
    void assertWriteInt8() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt8(1L);
        verify(byteBuf).writeLongLE(1L);
    }
    
    @Test
    void assertReadIntLenencWithOneByte() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(1L));
    }
    
    @Test
    void assertReadIntLenencWithZero() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfb);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(0L));
    }
    
    @Test
    void assertReadIntLenencWithTwoBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfc);
        when(byteBuf.readUnsignedShortLE()).thenReturn(100);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(100L));
    }
    
    @Test
    void assertReadIntLenencWithThreeBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfd);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(99999);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(99999L));
    }
    
    @Test
    void assertReadIntLenencWithFourBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xff);
        when(byteBuf.readLongLE()).thenReturn(Long.MAX_VALUE);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(Long.MAX_VALUE));
    }
    
    @Test
    void assertWriteIntLenencWithOneByte() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc(1L);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    void assertWriteIntLenencWithTwoBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc((long) (Math.pow(2D, 16D)) - 1L);
        verify(byteBuf).writeByte(0xfc);
        verify(byteBuf).writeShortLE((int) (Math.pow(2D, 16D)) - 1);
    }
    
    @Test
    void assertWriteIntLenencWithThreeBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc((long) (Math.pow(2D, 24D)) - 1L);
        verify(byteBuf).writeByte(0xfd);
        verify(byteBuf).writeMediumLE((int) (Math.pow(2D, 24D)) - 1);
    }
    
    @Test
    void assertWriteIntLenencWithFourBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc((long) (Math.pow(2D, 25D)) - 1L);
        verify(byteBuf).writeByte(0xfe);
        verify(byteBuf).writeLongLE((int) (Math.pow(2D, 25D)) - 1L);
    }
    
    @Test
    void assertReadLong() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readLong(1), is(1L));
    }
    
    @Test
    void assertReadStringLenenc() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringLenenc(), is(""));
    }
    
    @Test
    void assertReadStringLenencByBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringLenencByBytes(), is(new byte[]{}));
    }
    
    @Test
    void assertWriteStringLenencWithEmpty() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringLenenc("");
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    void assertWriteBytesLenenc() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytesLenenc("value".getBytes());
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertWriteBytesLenencWithEmpty() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytesLenenc("".getBytes());
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    void assertWriteStringLenenc() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringLenenc("value");
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertReadStringFix() {
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringFix(0), is(""));
    }
    
    @Test
    void assertReadStringFixByBytes() {
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringFixByBytes(0), is(new byte[]{}));
    }
    
    @Test
    void assertWriteStringFix() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringFix("value");
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertWriteBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytes("value".getBytes());
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertReadStringVar() {
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringVar(), is(""));
    }
    
    @Test
    void assertWriteStringVar() {
        assertDoesNotThrow(() -> new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringVar(""));
    }
    
    @Test
    void assertReadStringNul() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringNul(), is(""));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    void assertReadStringNulByBytes() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringNulByBytes(), is(new byte[]{}));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    void assertWriteStringNul() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringNul("value");
        verify(byteBuf).writeBytes("value".getBytes());
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    void assertReadStringEOF() {
        when(byteBuf.readableBytes()).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringEOF(), is(""));
    }
    
    @Test
    void assertWriteStringEOF() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringEOF("value");
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertSkipReserved() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).skipReserved(10);
        verify(byteBuf).skipBytes(10);
    }
    
    @Test
    void assertWriteReserved() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeReserved(10);
        verify(byteBuf).writeZero(10);
    }
}
