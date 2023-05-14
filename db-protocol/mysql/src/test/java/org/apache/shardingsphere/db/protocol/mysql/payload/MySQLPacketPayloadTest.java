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

package org.apache.shardingsphere.db.protocol.mysql.payload;

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
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt1(), is(1));
        }
    }
    
    @Test
    void assertWriteInt1() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeInt1(1);
        }
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    void assertReadInt2() {
        when(byteBuf.readUnsignedShortLE()).thenReturn(1);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt2(), is(1));
        }
    }
    
    @Test
    void assertWriteInt2() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeInt2(1);
        }
        verify(byteBuf).writeShortLE(1);
    }
    
    @Test
    void assertReadInt3() {
        when(byteBuf.readUnsignedMediumLE()).thenReturn(1);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt3(), is(1));
        }
    }
    
    @Test
    void assertWriteInt3() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeInt3(1);
        }
        verify(byteBuf).writeMediumLE(1);
    }
    
    @Test
    void assertReadInt4() {
        when(byteBuf.readIntLE()).thenReturn(1);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt4(), is(1));
        }
    }
    
    @Test
    void assertWriteInt4() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeInt4(1);
        }
        verify(byteBuf).writeIntLE(1);
    }
    
    @Test
    void assertReadInt6() {
        when(byteBuf.readByte()).thenReturn((byte) 0x01, (byte) 0x00);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt6(), is(1L));
        }
        when(byteBuf.readByte()).thenReturn((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt6(), is(0x800000000000L));
        }
    }
    
    @Test
    void assertWriteInt6() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertDoesNotThrow(() -> actual.writeInt6(1L));
        }
    }
    
    @Test
    void assertReadInt8() {
        when(byteBuf.readLongLE()).thenReturn(1L);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readInt8(), is(1L));
        }
    }
    
    @Test
    void assertWriteInt8() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeInt8(1L);
        }
        verify(byteBuf).writeLongLE(1L);
    }
    
    @Test
    void assertReadIntLenencWithOneByte() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readIntLenenc(), is(1L));
        }
    }
    
    @Test
    void assertReadIntLenencWithZero() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfb);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readIntLenenc(), is(0L));
        }
    }
    
    @Test
    void assertReadIntLenencWithTwoBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfc);
        when(byteBuf.readUnsignedShortLE()).thenReturn(100);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readIntLenenc(), is(100L));
        }
    }
    
    @Test
    void assertReadIntLenencWithThreeBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfd);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(99999);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readIntLenenc(), is(99999L));
        }
    }
    
    @Test
    void assertReadIntLenencWithFourBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xff);
        when(byteBuf.readLongLE()).thenReturn(Long.MAX_VALUE);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readIntLenenc(), is(Long.MAX_VALUE));
        }
    }
    
    @Test
    void assertWriteIntLenencWithOneByte() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeIntLenenc(1L);
        }
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    void assertWriteIntLenencWithTwoBytes() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeIntLenenc(Double.valueOf(Math.pow(2, 16)).longValue() - 1);
        }
        verify(byteBuf).writeByte(0xfc);
        verify(byteBuf).writeShortLE(Double.valueOf(Math.pow(2, 16)).intValue() - 1);
    }
    
    @Test
    void assertWriteIntLenencWithThreeBytes() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeIntLenenc(Double.valueOf(Math.pow(2, 24)).longValue() - 1);
        }
        verify(byteBuf).writeByte(0xfd);
        verify(byteBuf).writeMediumLE(Double.valueOf(Math.pow(2, 24)).intValue() - 1);
    }
    
    @Test
    void assertWriteIntLenencWithFourBytes() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeIntLenenc(Double.valueOf(Math.pow(2, 25)).longValue() - 1);
        }
        verify(byteBuf).writeByte(0xfe);
        verify(byteBuf).writeLongLE(Double.valueOf(Math.pow(2, 25)).intValue() - 1);
    }
    
    @Test
    void assertReadStringLenenc() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringLenenc(), is(""));
        }
    }
    
    @Test
    void assertReadStringLenencByBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringLenencByBytes(), is(new byte[]{}));
        }
    }
    
    @Test
    void assertWriteStringLenencWithEmpty() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeStringLenenc("");
        }
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    void assertWriteBytesLenenc() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeBytesLenenc("value".getBytes());
        }
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertWriteBytesLenencWithEmpty() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeBytesLenenc("".getBytes());
        }
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    void assertWriteStringLenenc() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeStringLenenc("value");
        }
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertReadStringFix() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringFix(0), is(""));
        }
    }
    
    @Test
    void assertReadStringFixByBytes() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringFixByBytes(0), is(new byte[]{}));
        }
    }
    
    @Test
    void assertWriteStringFix() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeStringFix("value");
        }
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertWriteBytes() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeBytes("value".getBytes());
        }
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertReadStringVar() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringVar(), is(""));
        }
    }
    
    @Test
    void assertWriteStringVar() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertDoesNotThrow(() -> actual.writeStringVar(""));
        }
    }
    
    @Test
    void assertReadStringNul() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringNul(), is(""));
        }
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    void assertReadStringNulByBytes() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringNulByBytes(), is(new byte[]{}));
        }
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    void assertWriteStringNul() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeStringNul("value");
        }
        verify(byteBuf).writeBytes("value".getBytes());
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    void assertReadStringEOF() {
        when(byteBuf.readableBytes()).thenReturn(0);
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            assertThat(actual.readStringEOF(), is(""));
        }
    }
    
    @Test
    void assertWriteStringEOF() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeStringEOF("value");
        }
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    void assertSkipReserved() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.skipReserved(10);
        }
        verify(byteBuf).skipBytes(10);
    }
    
    @Test
    void assertWriteReserved() {
        try (MySQLPacketPayload actual = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)) {
            actual.writeReserved(10);
        }
        verify(byteBuf).writeZero(10);
    }
    
    @Test
    void assertClose() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).close();
        verify(byteBuf).release();
    }
}
