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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLPacketPayloadTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertReadInt1() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt1(), is(1));
    }
    
    @Test
    public void assertWriteInt1() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt1(1);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    public void assertReadInt2() {
        when(byteBuf.readUnsignedShortLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt2(), is(1));
    }
    
    @Test
    public void assertWriteInt2() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt2(1);
        verify(byteBuf).writeShortLE(1);
    }
    
    @Test
    public void assertReadInt3() {
        when(byteBuf.readUnsignedMediumLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt3(), is(1));
    }
    
    @Test
    public void assertWriteInt3() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt3(1);
        verify(byteBuf).writeMediumLE(1);
    }
    
    @Test
    public void assertReadInt4() {
        when(byteBuf.readIntLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt4(), is(1));
    }
    
    @Test
    public void assertWriteInt4() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt4(1);
        verify(byteBuf).writeIntLE(1);
    }
    
    @Test
    public void assertReadInt6() {
        when(byteBuf.readByte()).thenReturn((byte) 0x01, (byte) 0x00);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt6(), is(1L));
        when(byteBuf.readByte()).thenReturn((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt6(), is(0x800000000000L));
    }
    
    @Test
    public void assertWriteInt6() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt6(1L);
    }
    
    @Test
    public void assertReadInt8() {
        when(byteBuf.readLongLE()).thenReturn(1L);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readInt8(), is(1L));
    }
    
    @Test
    public void assertWriteInt8() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeInt8(1L);
        verify(byteBuf).writeLongLE(1L);
    }
    
    @Test
    public void assertReadIntLenencWithOneByte() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(1L));
    }
    
    @Test
    public void assertReadIntLenencWithZero() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfb);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(0L));
    }
    
    @Test
    public void assertReadIntLenencWithTwoBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfc);
        when(byteBuf.readUnsignedShortLE()).thenReturn(100);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(100L));
    }
    
    @Test
    public void assertReadIntLenencWithThreeBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xfd);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(99999);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(99999L));
    }
    
    @Test
    public void assertReadIntLenencWithFourBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xff);
        when(byteBuf.readLongLE()).thenReturn(Long.MAX_VALUE);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readIntLenenc(), is(Long.MAX_VALUE));
    }
    
    @Test
    public void assertWriteIntLenencWithOneByte() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc(1L);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    public void assertWriteIntLenencWithTwoBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc(new Double(Math.pow(2, 16)).longValue() - 1);
        verify(byteBuf).writeByte(0xfc);
        verify(byteBuf).writeShortLE(new Double(Math.pow(2, 16)).intValue() - 1);
    }
    
    @Test
    public void assertWriteIntLenencWithThreeBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc(new Double(Math.pow(2, 24)).longValue() - 1);
        verify(byteBuf).writeByte(0xfd);
        verify(byteBuf).writeMediumLE(new Double(Math.pow(2, 24)).intValue() - 1);
    }
    
    @Test
    public void assertWriteIntLenencWithFourBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeIntLenenc(new Double(Math.pow(2, 25)).longValue() - 1);
        verify(byteBuf).writeByte(0xfe);
        verify(byteBuf).writeLongLE(new Double(Math.pow(2, 25)).intValue() - 1);
    }
    
    @Test
    public void assertReadStringLenenc() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringLenenc(), is(""));
    }
    
    @Test
    public void assertReadStringLenencByBytes() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringLenencByBytes(), is(new byte[]{}));
    }
    
    @Test
    public void assertWriteStringLenencWithEmpty() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringLenenc("");
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    public void assertWriteBytesLenenc() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytesLenenc("value".getBytes());
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertWriteBytesLenencWithEmpty() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytesLenenc("".getBytes());
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    public void assertWriteStringLenenc() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringLenenc("value");
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertReadStringFix() {
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringFix(0), is(""));
    }
    
    @Test
    public void assertReadStringFixByBytes() {
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringFixByBytes(0), is(new byte[]{}));
    }
    
    @Test
    public void assertWriteStringFix() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringFix("value");
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertWriteBytes() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeBytes("value".getBytes());
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertReadStringVar() {
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringVar(), is(""));
    }
    
    @Test
    public void assertWriteStringVar() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringVar("");
    }
    
    @Test
    public void assertReadStringNul() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringNul(), is(""));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    public void assertReadStringNulByBytes() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringNulByBytes(), is(new byte[]{}));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    public void assertWriteStringNul() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringNul("value");
        verify(byteBuf).writeBytes("value".getBytes());
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    public void assertReadStringEOF() {
        when(byteBuf.readableBytes()).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringEOF(), is(""));
    }
    
    @Test
    public void assertWriteStringEOF() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeStringEOF("value");
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertSkipReserved() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).skipReserved(10);
        verify(byteBuf).skipBytes(10);
    }
    
    @Test
    public void assertWriteReserved() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).writeReserved(10);
        verify(byteBuf, times(1)).writeZero(10);
    }
    
    @Test
    public void assertClose() {
        new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).close();
        verify(byteBuf).release();
    }
}
