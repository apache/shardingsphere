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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigInteger;
import java.util.BitSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataTypesCodecTest {
    
    private static final String EXPECTED_STRING = "0123456789";
    
    private static final int UNSIGNED_INT3_MAV_VALUE = (int) Math.pow(2, 8 * 3 - 1) - 1;
    
    private static final int UNSIGNED_INT3_MIN_VALUE = (int) -Math.pow(2, 8 * 3 - 1);
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertSkipBytes() {
        DataTypesCodec.skipBytes(10, byteBuf);
        verify(byteBuf).skipBytes(10);
    }
    
    @Test
    public void assertReadNul() {
        DataTypesCodec.readNul(byteBuf);
        verify(byteBuf).readByte();
    }
    
    @Test
    public void assertReadFloatLE() {
        when(byteBuf.readFloatLE()).thenReturn(1.1f);
        assertThat(DataTypesCodec.readFloatLE(byteBuf), is(1.1f));
    }
    
    @Test
    public void assertReadDoubleLE() {
        when(byteBuf.readDoubleLE()).thenReturn(1.1d);
        assertThat(DataTypesCodec.readDoubleLE(byteBuf), is(1.1d));
    }
    
    @Test
    public void assertReadBitmap() {
        when(byteBuf.readByte()).thenReturn((byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x80);
        BitSet expected = new BitSet();
        for (int i = 0; i <= 7; i++) {
            expected.set(i * 8 + i);
        }
        assertThat(DataTypesCodec.readBitmap(64, byteBuf), is(expected));
    }
    
    @Test
    public void assertReadInt1() {
        when(byteBuf.readByte()).thenReturn(Byte.MAX_VALUE, Byte.MIN_VALUE);
        assertThat(DataTypesCodec.readInt1(byteBuf), is(Byte.MAX_VALUE));
        assertThat(DataTypesCodec.readInt1(byteBuf), is(Byte.MIN_VALUE));
    }
    
    @Test
    public void assertReadInt2LE() {
        when(byteBuf.readShortLE()).thenReturn(Short.MAX_VALUE, Short.MIN_VALUE);
        assertThat(DataTypesCodec.readInt2LE(byteBuf), is(Short.MAX_VALUE));
        assertThat(DataTypesCodec.readInt2LE(byteBuf), is(Short.MIN_VALUE));
    }
    
    @Test
    public void assertReadInt3LE() {
        when(byteBuf.readMediumLE()).thenReturn(UNSIGNED_INT3_MAV_VALUE, UNSIGNED_INT3_MIN_VALUE);
        assertThat(DataTypesCodec.readInt3LE(byteBuf), is(UNSIGNED_INT3_MAV_VALUE));
        assertThat(DataTypesCodec.readInt3LE(byteBuf), is(UNSIGNED_INT3_MIN_VALUE));
    }
    
    @Test
    public void assertReadInt4LE() {
        when(byteBuf.readIntLE()).thenReturn(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertThat(DataTypesCodec.readInt4LE(byteBuf), is(Integer.MAX_VALUE));
        assertThat(DataTypesCodec.readInt4LE(byteBuf), is(Integer.MIN_VALUE));
    }
    
    @Test
    public void assertReadInt8LE() {
        when(byteBuf.readLongLE()).thenReturn(1L);
        assertThat(DataTypesCodec.readInt8LE(byteBuf), is(1L));
    }
    
    @Test
    public void assertReadUnsignedInt1() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0xff);
        assertThat(DataTypesCodec.readUnsignedInt1(byteBuf), is((short) 0xff));
    }
    
    @Test
    public void assertReadUnsignedInt2BE() {
        when(byteBuf.readUnsignedShort()).thenReturn(0x0000ffff);
        assertThat(DataTypesCodec.readUnsignedInt2BE(byteBuf), is(0x0000ffff));
    }
    
    @Test
    public void assertReadUnsignedInt2LE() {
        when(byteBuf.readUnsignedShortLE()).thenReturn(0xff);
        assertThat(DataTypesCodec.readUnsignedInt2LE(byteBuf), is(0xff));
    }
    
    @Test
    public void assertReadUnsignedInt3BE() {
        when(byteBuf.readUnsignedMedium()).thenReturn(1);
        assertThat(DataTypesCodec.readUnsignedInt3BE(byteBuf), is(1));
    }
    
    @Test
    public void assertReadUnsignedInt3LE() {
        when(byteBuf.readUnsignedMediumLE()).thenReturn(1);
        assertThat(DataTypesCodec.readUnsignedInt3LE(byteBuf), is(1));
    }
    
    @Test
    public void assertReadUnsignedInt4BE() {
        when(byteBuf.readUnsignedInt()).thenReturn(1L + Integer.MAX_VALUE);
        assertThat(DataTypesCodec.readUnsignedInt4BE(byteBuf), is(1L + Integer.MAX_VALUE));
    }
    
    @Test
    public void assertReadUnsignedInt4LE() {
        when(byteBuf.readUnsignedIntLE()).thenReturn(1L + Integer.MAX_VALUE);
        assertThat(DataTypesCodec.readUnsignedInt4LE(byteBuf), is(1L + Integer.MAX_VALUE));
    }
    
    @Test
    public void assertReadUnsignedInt5BE() {
        when(byteBuf.readByte()).thenReturn((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x02, (byte) 0x01);
        assertThat(DataTypesCodec.readUnsignedInt5BE(byteBuf), is(4328718849L));
        when(byteBuf.readByte()).thenReturn((byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        assertThat(DataTypesCodec.readUnsignedInt5BE(byteBuf), is((long) Math.pow(2, 8 * 5 - 1)));
    }
    
    @Test
    public void assertReadUnsignedInt6LE() {
        when(byteBuf.readByte()).thenReturn((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x03, (byte) 0x02, (byte) 0x01);
        assertThat(DataTypesCodec.readUnsignedInt6LE(byteBuf), is(1108152091137L));
        when(byteBuf.readByte()).thenReturn((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80);
        assertThat(DataTypesCodec.readUnsignedInt6LE(byteBuf), is((long) Math.pow(2, 8 * 6 - 1)));
    }
    
    @Test
    public void assertReadUnsignedInt8LE() {
        when(byteBuf.readLongLE()).thenReturn(Long.MIN_VALUE, Long.MAX_VALUE);
        assertThat(DataTypesCodec.readUnsignedInt8LE(byteBuf), is(new BigInteger("9223372036854775808")));
        assertThat(DataTypesCodec.readUnsignedInt8LE(byteBuf), is(new BigInteger("9223372036854775807")));
    }
    
    @Test
    public void assertReadLengthCodedIntLE() {
        when(byteBuf.readByte()).thenReturn((byte) 251);
        assertThat(DataTypesCodec.readLengthCodedIntLE(byteBuf), is(-1L));
        when(byteBuf.readByte()).thenReturn((byte) 252);
        when(byteBuf.readUnsignedShortLE()).thenReturn(0x00010000);
        assertThat(DataTypesCodec.readLengthCodedIntLE(byteBuf), is(65536L));
        when(byteBuf.readByte()).thenReturn((byte) 253);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(0x01000000);
        assertThat(DataTypesCodec.readLengthCodedIntLE(byteBuf), is(16777216L));
        when(byteBuf.readByte()).thenReturn((byte) 254);
        when(byteBuf.readLongLE()).thenReturn(1L + Integer.MAX_VALUE);
        assertThat(DataTypesCodec.readLengthCodedIntLE(byteBuf), is(1L + Integer.MAX_VALUE));
        when(byteBuf.readByte()).thenReturn((byte) 10);
        assertThat(DataTypesCodec.readLengthCodedIntLE(byteBuf), is(10L));
    }
    
    @Test
    public void assertReadBytes() {
        byte[] actual = DataTypesCodec.readBytes(10, byteBuf);
        assertThat(actual.length, is(10));
        verify(byteBuf).readBytes(actual, 0, 10);
    }
    
    @Test
    public void assertReadFixedLengthString() {
        when(byteBuf.readBytes(any(byte[].class), eq(0), eq(10))).then(mockReadBytesAnswer());
        assertThat(DataTypesCodec.readFixedLengthString(10, byteBuf), is(EXPECTED_STRING));
    }
    
    @Test
    public void assertReadLengthCodedString() {
        when(byteBuf.readByte()).thenReturn((byte) 10);
        when(byteBuf.readBytes(any(byte[].class), eq(0), eq(10))).then(mockReadBytesAnswer());
        assertThat(DataTypesCodec.readLengthCodedString(byteBuf), is(EXPECTED_STRING));
    }
    
    @Test
    public void assertReadNulTerminatedString() {
        when(byteBuf.bytesBefore((byte) 0x00)).thenReturn(10);
        when(byteBuf.readBytes(any(byte[].class), eq(0), eq(10))).then(mockReadBytesAnswer());
        assertThat(DataTypesCodec.readNulTerminatedString(byteBuf), is(EXPECTED_STRING));
    }
    
    @Test
    public void assertWriteByte() {
        final byte data = 0x00;
        DataTypesCodec.writeByte(data, byteBuf);
        verify(byteBuf).writeByte(data);
    }
    
    @Test
    public void assertWriteInt5() {
        final long data = 1L << 32;
        DataTypesCodec.writeInt5(data, byteBuf);
        verify(byteBuf).writeByte(0x01);
        verify(byteBuf, times(4)).writeByte(0x00);
    }
    
    @Test
    public void assertWriteIntN() {
        long value = 0xff;
        long actual = writeIntN(1, value).readUnsignedByte();
        assertThat(actual, is(value));
        value = 0xff00;
        actual = writeIntN(2, value).readUnsignedShort();
        assertThat(actual, is(value));
        value = 0x00ff;
        actual = writeIntN(2, value).readUnsignedShort();
        assertThat(actual, is(value));
        assertThat(writeIntN(2, value).writerIndex(), is(2));
    }
    
    private ByteBuf writeIntN(final int length, final long value) {
        ByteBuf byteBuf = Unpooled.buffer();
        DataTypesCodec.writeIntN(length, value, byteBuf);
        return byteBuf;
    }
    
    @Test
    public void assertWriteInt2LE() {
        final short data = 0x00;
        DataTypesCodec.writeInt2LE(data, byteBuf);
        verify(byteBuf).writeShortLE(data);
    }
    
    @Test
    public void assertWriteInt4LE() {
        final int data = 0x00;
        DataTypesCodec.writeInt4LE(data, byteBuf);
        verify(byteBuf).writeIntLE(data);
    }
    
    @Test
    public void assertWriteIntNLE() {
        long value = 0xff;
        long actual = writeIntNLE(1, value).readUnsignedByte();
        assertThat(actual, is(value));
        value = 0xff00;
        actual = writeIntNLE(2, value).readUnsignedShortLE();
        assertThat(actual, is(value));
        value = 0x00ff;
        actual = writeIntNLE(2, value).readUnsignedShortLE();
        assertThat(actual, is(value));
        assertThat(writeIntNLE(2, value).writerIndex(), is(2));
    }
    
    private ByteBuf writeIntNLE(final int length, final long value) {
        ByteBuf byteBuf = Unpooled.buffer();
        DataTypesCodec.writeIntNLE(length, value, byteBuf);
        return byteBuf;
    }
    
    @Test
    public void assertWriteLengthCodedInt() {
        DataTypesCodec.writeLengthCodedInt(1, byteBuf);
        verify(byteBuf).writeByte(1);
        DataTypesCodec.writeLengthCodedInt(1 << 15L, byteBuf);
        verify(byteBuf).writeByte((byte) 252);
        verify(byteBuf).writeShortLE(1 << 15L);
        DataTypesCodec.writeLengthCodedInt(1 << 23L, byteBuf);
        verify(byteBuf).writeByte((byte) 253);
        verify(byteBuf).writeMediumLE(1 << 23L);
        DataTypesCodec.writeLengthCodedInt(1 << 24L, byteBuf);
        verify(byteBuf).writeByte((byte) 254);
        verify(byteBuf).writeIntLE(1 << 24L);
    }
    
    @Test
    public void assertWriteBytes() {
        final byte[] data = new byte[10];
        DataTypesCodec.writeBytes(data, byteBuf);
        verify(byteBuf).writeBytes(data);
    }
    
    @Test
    public void assertWriteNulTerminatedString() {
        DataTypesCodec.writeNulTerminatedString(EXPECTED_STRING, byteBuf);
        verify(byteBuf).writeBytes(EXPECTED_STRING.getBytes());
        verify(byteBuf).writeByte((byte) 0x00);
    }
    
    @Test
    public void assertWriteLengthCodedBinary() {
        DataTypesCodec.writeLengthCodedBinary(EXPECTED_STRING.getBytes(), byteBuf);
        verify(byteBuf).writeByte((byte) 10);
        verify(byteBuf).writeBytes(EXPECTED_STRING.getBytes());
    }
    
    private Answer mockReadBytesAnswer() {
        return (Answer<ByteBuf>) invocationOnMock -> {
            byte[] args = invocationOnMock.getArgument(0);
            byte[] expectedBytes = DataTypesCodecTest.EXPECTED_STRING.getBytes();
            System.arraycopy(expectedBytes, 0, args, 0, expectedBytes.length);
            return null;
        };
    }
}
