/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet;

import io.netty.buffer.ByteBuf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLPacketPayloadTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertReadInt1() {
        when(byteBuf.readByte()).thenReturn((byte) 1);
        assertThat(new MySQLPacketPayload(byteBuf).readInt1(), is(1));
    }
    
    @Test
    public void assertWriteInt1() {
        new MySQLPacketPayload(byteBuf).writeInt1(1);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    public void assertReadInt2() {
        when(byteBuf.readShortLE()).thenReturn((short) 1);
        assertThat(new MySQLPacketPayload(byteBuf).readInt2(), is(1));
    }
    
    @Test
    public void assertWriteInt2() {
        new MySQLPacketPayload(byteBuf).writeInt2(1);
        verify(byteBuf).writeShortLE(1);
    }
    
    @Test
    public void assertReadInt3() {
        when(byteBuf.readMediumLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf).readInt3(), is(1));
    }
    
    @Test
    public void assertWriteInt3() {
        new MySQLPacketPayload(byteBuf).writeInt3(1);
        verify(byteBuf).writeMediumLE(1);
    }
    
    @Test
    public void assertReadInt4() {
        when(byteBuf.readIntLE()).thenReturn(1);
        assertThat(new MySQLPacketPayload(byteBuf).readInt4(), is(1));
    }
    
    @Test
    public void assertWriteInt4() {
        new MySQLPacketPayload(byteBuf).writeInt4(1);
        verify(byteBuf).writeIntLE(1);
    }
    
    @Test
    public void assertReadInt6() {
        assertThat(new MySQLPacketPayload(byteBuf).readInt6(), is(0));
    }
    
    @Test
    public void assertWriteInt6() {
        new MySQLPacketPayload(byteBuf).writeInt6(1);
    }
    
    @Test
    public void assertReadInt8() {
        when(byteBuf.readLongLE()).thenReturn(1L);
        assertThat(new MySQLPacketPayload(byteBuf).readInt8(), is(1L));
    }
    
    @Test
    public void assertWriteInt8() {
        new MySQLPacketPayload(byteBuf).writeInt8(1L);
        verify(byteBuf).writeLongLE(1L);
    }
    
    @Test
    public void assertReadIntLenencWithOneByte() {
        when(byteBuf.readByte()).thenReturn((byte) 1);
        assertThat(new MySQLPacketPayload(byteBuf).readIntLenenc(), is(1L));
    }
    
    @Test
    public void assertReadIntLenencWithZero() {
        when(byteBuf.readByte()).thenReturn((byte) 0xfb);
        assertThat(new MySQLPacketPayload(byteBuf).readIntLenenc(), is(0L));
    }
    
    @Test
    public void assertReadIntLenencWithTwoBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 0xfc);
        when(byteBuf.readShortLE()).thenReturn((short) 100);
        assertThat(new MySQLPacketPayload(byteBuf).readIntLenenc(), is(100L));
    }
    
    @Test
    public void assertReadIntLenencWithThreeBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 0xfd);
        when(byteBuf.readMediumLE()).thenReturn(99999);
        assertThat(new MySQLPacketPayload(byteBuf).readIntLenenc(), is(99999L));
    }
    
    @Test
    public void assertReadIntLenencWithFourBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 0xff);
        when(byteBuf.readLongLE()).thenReturn(Long.MAX_VALUE);
        assertThat(new MySQLPacketPayload(byteBuf).readIntLenenc(), is(Long.MAX_VALUE));
    }
    
    @Test
    public void assertWriteIntLenencWithOneByte() {
        new MySQLPacketPayload(byteBuf).writeIntLenenc(1L);
        verify(byteBuf).writeByte(1);
    }
    
    @Test
    public void assertWriteIntLenencWithTwoBytes() {
        new MySQLPacketPayload(byteBuf).writeIntLenenc(new Double(Math.pow(2, 16)).longValue() - 1);
        verify(byteBuf).writeByte(0xfc);
        verify(byteBuf).writeShortLE(new Double(Math.pow(2, 16)).intValue() - 1);
    }
    
    @Test
    public void assertWriteIntLenencWithThreeBytes() {
        new MySQLPacketPayload(byteBuf).writeIntLenenc(new Double(Math.pow(2, 24)).longValue() - 1);
        verify(byteBuf).writeByte(0xfd);
        verify(byteBuf).writeMediumLE(new Double(Math.pow(2, 24)).intValue() - 1);
    }
    
    @Test
    public void assertWriteIntLenencWithFourBytes() {
        new MySQLPacketPayload(byteBuf).writeIntLenenc(new Double(Math.pow(2, 25)).longValue() - 1);
        verify(byteBuf).writeByte(0xfe);
        verify(byteBuf).writeLongLE(new Double(Math.pow(2, 25)).intValue() - 1);
    }
    
    @Test
    public void assertReadStringLenenc() {
        when(byteBuf.readByte()).thenReturn((byte) 0);
        assertThat(new MySQLPacketPayload(byteBuf).readStringLenenc(), is(""));
    }
    
    @Test
    public void assertReadStringLenencByBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 0);
        assertThat(new MySQLPacketPayload(byteBuf).readStringLenencByBytes(), is(new byte[] {}));
    }
    
    @Test
    public void assertWriteStringLenencWithEmpty() {
        new MySQLPacketPayload(byteBuf).writeStringLenenc("");
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    public void assertWriteStringLenenc() {
        new MySQLPacketPayload(byteBuf).writeStringLenenc("value");
        verify(byteBuf).writeByte(5);
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertReadStringFix() {
        assertThat(new MySQLPacketPayload(byteBuf).readStringFix(0), is(""));
    }
    
    @Test
    public void assertReadStringFixByBytes() {
        assertThat(new MySQLPacketPayload(byteBuf).readStringFixByBytes(0), is(new byte[] {}));
    }
    
    @Test
    public void assertWriteStringFix() {
        new MySQLPacketPayload(byteBuf).writeStringFix("value");
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertWriteBytes() {
        new MySQLPacketPayload(byteBuf).writeBytes("value".getBytes());
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertReadStringVar() {
        assertThat(new MySQLPacketPayload(byteBuf).readStringVar(), is(""));
    }
    
    @Test
    public void assertWriteStringVar() {
        new MySQLPacketPayload(byteBuf).writeStringVar("");
    }
    
    @Test
    public void assertReadStringNul() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf).readStringNul(), is(""));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    public void assertReadStringNulByBytes() {
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf).readStringNulByBytes(), is(new byte[] {}));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    public void assertWriteStringNul() {
        new MySQLPacketPayload(byteBuf).writeStringNul("value");
        verify(byteBuf).writeBytes("value".getBytes());
        verify(byteBuf).writeByte(0);
    }
    
    @Test
    public void assertReadStringEOF() {
        when(byteBuf.readableBytes()).thenReturn(0);
        assertThat(new MySQLPacketPayload(byteBuf).readStringEOF(), is(""));
    }
    
    @Test
    public void assertWriteStringEOF() {
        new MySQLPacketPayload(byteBuf).writeStringEOF("value");
        verify(byteBuf).writeBytes("value".getBytes());
    }
    
    @Test
    public void assertSkipReserved() {
        new MySQLPacketPayload(byteBuf).skipReserved(10);
        verify(byteBuf).skipBytes(10);
    }
    
    @Test
    public void assertWriteReserved() {
        new MySQLPacketPayload(byteBuf).writeReserved(10);
        verify(byteBuf, times(10)).writeByte(0);
    }
    
    @Test
    public void assertReadFloat() {
        when(byteBuf.readFloatLE()).thenReturn(1f);
        assertThat(new MySQLPacketPayload(byteBuf).readFloat(), is(1f));
    }
    
    @Test
    public void assertWriteFloat() {
        new MySQLPacketPayload(byteBuf).writeFloat(1f);
        verify(byteBuf).writeFloatLE(1f);
    }
    
    @Test
    public void assertReadDouble() {
        when(byteBuf.readDoubleLE()).thenReturn(1d);
        assertThat(new MySQLPacketPayload(byteBuf).readDouble(), is(1d));
    }
    
    @Test
    public void assertWriteDouble() {
        new MySQLPacketPayload(byteBuf).writeDouble(1d);
        verify(byteBuf).writeDoubleLE(1d);
    }
    
    @Test
    public void assertReadDateWithZeroByte() {
        assertThat(new MySQLPacketPayload(byteBuf).readDate(), is(new Timestamp(0)));
    }
    
    @Test
    public void assertReadDateWithFourBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 4, (byte) 12, (byte) 31);
        when(byteBuf.readShortLE()).thenReturn((short) 2018);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(new MySQLPacketPayload(byteBuf).readDate().getTime());
        assertThat(actual.get(Calendar.YEAR), is(2018));
        assertThat(actual.get(Calendar.MONTH), is(Calendar.DECEMBER));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), is(31));
    }
    
    @Test
    public void assertReadDateWithSevenBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 7, (byte) 12, (byte) 31, (byte) 10, (byte) 59, (byte) 0);
        when(byteBuf.readShortLE()).thenReturn((short) 2018);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(new MySQLPacketPayload(byteBuf).readDate().getTime());
        assertThat(actual.get(Calendar.YEAR), is(2018));
        assertThat(actual.get(Calendar.MONTH), is(Calendar.DECEMBER));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), is(31));
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test
    public void assertReadDateWithElevenBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 11, (byte) 12, (byte) 31, (byte) 10, (byte) 59, (byte) 0);
        when(byteBuf.readShortLE()).thenReturn((short) 2018);
        when(byteBuf.readIntLE()).thenReturn(500);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(new MySQLPacketPayload(byteBuf).readDate().getTime());
        assertThat(actual.get(Calendar.YEAR), is(2018));
        assertThat(actual.get(Calendar.MONTH), is(Calendar.DECEMBER));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), is(31));
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertReadDateWithIllegalArgument() {
        when(byteBuf.readByte()).thenReturn((byte) 100);
        new MySQLPacketPayload(byteBuf).readDate();
    }
    
    @Test
    public void assertReadTimeWithZeroByte() {
        assertThat(new MySQLPacketPayload(byteBuf).readTime(), is(new Timestamp(0)));
    }
    
    @Test
    public void assertReadTimeWithEightBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 8, (byte) 0, (byte) 10, (byte) 59, (byte) 0);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(new MySQLPacketPayload(byteBuf).readTime().getTime());
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test
    public void assertReadTimeWithTwelveBytes() {
        when(byteBuf.readByte()).thenReturn((byte) 12, (byte) 0, (byte) 10, (byte) 59, (byte) 0);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(new MySQLPacketPayload(byteBuf).readTime().getTime());
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertReadTimeWithIllegalArgument() {
        when(byteBuf.readByte()).thenReturn((byte) 100);
        new MySQLPacketPayload(byteBuf).readTime();
    }
    
    @Test
    public void assertClose() {
        new MySQLPacketPayload(byteBuf).close();
        verify(byteBuf).release();
    }
}
