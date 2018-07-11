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

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Payload operation for MySQL packet data types.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/describing-packets.html">describing packets</a>
 * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html">binary protocol value</a>
 * 
 * @author zhangliang
 * @author zhangyonglun
 * @author wangkai
 */
@RequiredArgsConstructor
@Getter
public final class MySQLPacketPayload {
    
    private final ByteBuf byteBuf;
    
    /**
     * Read 1 byte fixed length integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     * 
     * @return 1 byte fixed length integer
     */
    public int readInt1() {
        return byteBuf.readByte() & 0xff;
    }
    
    /**
     * Write 1 byte fixed length integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     * 
     * @param value 1 byte fixed length integer
     */
    public void writeInt1(final int value) {
        byteBuf.writeByte(value);
    }
    
    /**
     * Read 2 byte fixed length integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @return 2 byte fixed length integer
     */
    public int readInt2() {
        return byteBuf.readShortLE() & 0xffff;
    }
    
    /**
     * Write 2 byte fixed length integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @param value 2 byte fixed length integer
     */
    public void writeInt2(final int value) {
        byteBuf.writeShortLE(value);
    }
    
    /**
     * Read 3 byte fixed length integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @return 3 byte fixed length integer
     */
    public int readInt3() {
        return byteBuf.readMediumLE() & 0xffffff;
    }
    
    /**
     * Write 3 byte fixed length integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @param value 3 byte fixed length integer
     */
    public void writeInt3(final int value) {
        byteBuf.writeMediumLE(value);
    }
    
    /**
     * Read 4 byte fixed length integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @return 4 byte fixed length integer
     */
    public int readInt4() {
        return byteBuf.readIntLE();
    }
    
    /**
     * Write 4 byte fixed length integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @param value 4 byte fixed length integer
     */
    public void writeInt4(final int value) {
        byteBuf.writeIntLE(value);
    
    }
    
    /**
     * Read 6 byte fixed length integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @return 6 byte fixed length integer
     */
    public int readInt6() {
        // TODO
        return 0;
    }
    
    /**
     * Write 6 byte fixed length integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @param value 6 byte fixed length integer
     */
    public void writeInt6(final int value) {
        // TODO
    }
    
    /**
     * Read 8 byte fixed length integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @return 8 byte fixed length integer
     */
    public long readInt8() {
        return byteBuf.readLongLE();
    }
    
    /**
     * Write 8 byte fixed length integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::FixedLengthInteger">FixedLengthInteger</a>
     *
     * @param value 8 byte fixed length integer
     */
    public void writeInt8(final long value) {
        byteBuf.writeLongLE(value);
        
    }
    
    /**
     * Read length encoded integer from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::LengthEncodedInteger">LengthEncodedInteger</a>
     *
     * @return length encoded integer
     */
    public long readIntLenenc() {
        int firstByte = readInt1();
        if (firstByte < 0xfb) {
            return firstByte;
        }
        if (0xfb == firstByte) {
            return 0;
        }
        if (0xfc == firstByte) {
            return byteBuf.readShortLE();
        }
        if (0xfd == firstByte) {
            return byteBuf.readMediumLE();
        }
        return byteBuf.readLongLE();
    }
    
    /**
     * Write length encoded integer to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/integer.html#packet-Protocol::LengthEncodedInteger">LengthEncodedInteger</a>
     *
     * @param value length encoded integer
     */
    public void writeIntLenenc(final long value) {
        if (value < 251) {
            byteBuf.writeByte((int) value);
            return;
        }
        if (value < Math.pow(2, 16)) {
            byteBuf.writeByte(0xfc);
            byteBuf.writeShortLE((int) value);
            return;
        }
        if (value < Math.pow(2, 24)) {
            byteBuf.writeByte(0xfd);
            byteBuf.writeInt((int) value);
            return;
        }
        byteBuf.writeByte(0xfe);
        byteBuf.writeLongLE(value);
    }
    
    /**
     * Read fixed length string from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::FixedLengthString">FixedLengthString</a>
     *
     * @return fixed length string
     */
    public String readStringLenenc() {
        int length = (int) readIntLenenc();
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return new String(result);
    }
    
    /**
     * Read fixed length string from byte buffers.
     *
     * @return fixed length bytes
     */
    public byte[] readStringLenencByBytes() {
        int length = (int) readIntLenenc();
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }
    
    /**
     * Write fixed length string to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::FixedLengthString">FixedLengthString</a>
     *
     * @param value fixed length string
     */
    public void writeStringLenenc(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            byteBuf.writeByte(0);
            return;
        }
        writeIntLenenc(value.getBytes().length);
        byteBuf.writeBytes(value.getBytes());
    }
    
    /**
     * Read fixed length string from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::FixedLengthString">FixedLengthString</a>
     *
     * @param length length of fixed string
     * 
     * @return fixed length string
     */
    public String readStringFix(final int length) {
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return new String(result);
    }
    
    /**
     * Read fixed length string from byte buffers.
     *
     * @param length length of fixed string
     *
     * @return fixed length string
     */
    public byte[] readStringFixByBytes(final int length) {
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }
    
    /**
     * Write variable length string to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::FixedLengthString">FixedLengthString</a>
     *
     * @param value fixed length string
     */
    public void writeStringFix(final String value) {
        byteBuf.writeBytes(value.getBytes());
    }
    
    /**
     * Write variable length bytes to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/secure-password-authentication.html#packet-Authentication::Native41">Native41</a>
     *
     * @param value fixed length bytes
     */
    public void writeBytes(final byte[] value) {
        byteBuf.writeBytes(value);
    }
    
    /**
     * Read variable length string from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::VariableLengthString">FixedLengthString</a>
     *
     * @return variable length string
     */
    public String readStringVar() {
        // TODO
        return "";
    }
    
    /**
     * Write fixed length string to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::VariableLengthString">FixedLengthString</a>
     *
     * @param value variable length string
     */
    public void writeStringVar(final String value) {
        // TODO
    }
    
    /**
     * Read null terminated string from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::NulTerminatedString">NulTerminatedString</a>
     *
     * @return null terminated string
     */
    public String readStringNul() {
        byte[] result = new byte[byteBuf.bytesBefore((byte) 0)];
        byteBuf.readBytes(result);
        byteBuf.skipBytes(1);
        return new String(result);
    }
    
    /**
     * Read null terminated string from byte buffers.
     *
     * @return null terminated string
     */
    public byte[] readStringNulByBytes() {
        byte[] result = new byte[byteBuf.bytesBefore((byte) 0)];
        byteBuf.readBytes(result);
        byteBuf.skipBytes(1);
        return result;
    }
    
    /**
     * Write null terminated string to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::NulTerminatedString">NulTerminatedString</a>
     *
     * @param value null terminated string
     */
    public void writeStringNul(final String value) {
        byteBuf.writeBytes(value.getBytes());
        byteBuf.writeByte(0);
    }
    
    /**
     * Read rest of packet string from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::RestOfPacketString">RestOfPacketString</a>
     *
     * @return rest of packet string
     */
    public String readStringEOF() {
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return new String(result);
    }
    
    /**
     * Write rest of packet string to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/string.html#packet-Protocol::RestOfPacketString">RestOfPacketString</a>
     *
     * @param value rest of packet string
     */
    public void writeStringEOF(final String value) {
        byteBuf.writeBytes(value.getBytes());
    }
    
    /**
     * Skip reserved from byte buffers.
     * 
     * @param length length of reserved
     */
    public void skipReserved(final int length) {
        byteBuf.skipBytes(length);
    }
    
    /**
     * Write null for reserved to byte buffers.
     * 
     * @param length length of reserved
     */
    public void writeReserved(final int length) {
        for (int i = 0; i < length; i++) {
            byteBuf.writeByte(0);
        }
    }
    
    /**
     * Read 4 byte float from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_FLOAT">MYSQL_TYPE_FLOAT</a>
     *
     * @return 4 byte float
     */
    public float readFloat() {
        return byteBuf.readFloatLE();
    }
    
    /**
     * Write 4 byte float to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_FLOAT">MYSQL_TYPE_FLOAT</a>
     *
     * @param value 4 byte float
     */
    public void writeFloat(final float value) {
        byteBuf.writeFloatLE(value);
    }
    
    /**
     * Read 8 byte double from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_DOUBLE">MYSQL_TYPE_DOUBLE</a>
     *
     * @return 8 byte double
     */
    public double readDouble() {
        return byteBuf.readDoubleLE();
    }
    
    /**
     * Write 8 byte double to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_DOUBLE">MYSQL_TYPE_DOUBLE</a>
     *
     * @param value 8 byte double
     */
    public void writeDouble(final double value) {
        byteBuf.writeDoubleLE(value);
    }
    
    /**
     * Read date from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_DATE">MYSQL_TYPE_DATE</a>
     *
     * @return timestamp
     */
    public Timestamp readDate() {
        Timestamp timestamp;
        Calendar calendar = Calendar.getInstance();
        int length = readInt1();
        switch (length) {
            case 0:
                timestamp = new Timestamp(0);
                break;
            case 4:
                calendar.set(readInt2(), readInt1() - 1, readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                break;
            case 7:
                calendar.set(readInt2(), readInt1() - 1, readInt1(),
                    readInt1(), readInt1(), readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                break;
            case 11:
                calendar.set(readInt2(), readInt1() - 1, readInt1(),
                    readInt1(), readInt1(), readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                timestamp.setNanos(readInt4());
                break;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_TIME", length));
        }
        return timestamp;
    }
    
    /**
     * Write date to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_DATE">MYSQL_TYPE_DATE</a>
     *
     * @param timestamp timestamp
     */
    public void writeDate(final Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = timestamp.getNanos();
        boolean isDateValueAbsent = 0 == year && 0 == month && 0 == day;
        boolean isTimeValueAbsent = 0 == hour && 0 == minute && 0 == second;
        boolean isMillisecondValueAbsent = 0 == millisecond;
        if (isDateValueAbsent && isTimeValueAbsent && isMillisecondValueAbsent) {
            writeInt1(0);
        } else if (isTimeValueAbsent && isMillisecondValueAbsent) {
            writeInt1(4);
            writeInt2(year);
            writeInt1(month);
            writeInt1(day);
        } else if (isMillisecondValueAbsent) {
            writeInt1(7);
            writeInt2(year);
            writeInt1(month);
            writeInt1(day);
            writeInt1(hour);
            writeInt1(minute);
            writeInt1(second);
        } else {
            writeInt1(11);
            writeInt2(year);
            writeInt1(month);
            writeInt1(day);
            writeInt1(hour);
            writeInt1(minute);
            writeInt1(second);
            writeInt4(millisecond);
        }
    }
    
    /**
     * Read time from byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_TIME">MYSQL_TYPE_TIME</a>
     *
     * @return timestamp
     */
    public Timestamp readTime() {
        Timestamp timestamp;
        Calendar calendar = Calendar.getInstance();
        int length = readInt1();
        readInt1();
        readInt4();
        switch (length) {
            case 0:
                timestamp = new Timestamp(0);
                break;
            case 8:
                calendar.set(0, 0, 0, readInt1(), readInt1(), readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                timestamp.setNanos(0);
                break;
            case 12:
                calendar.set(0, 0, 0, readInt1(), readInt1(), readInt1());
                timestamp = new Timestamp(calendar.getTimeInMillis());
                timestamp.setNanos(readInt4());
                break;
            default:
                throw new IllegalArgumentException(String.format("Wrong length '%d' of MYSQL_TYPE_DATE", length));
        }
        return timestamp;
    }
    
    /**
     * Write time to byte buffers.
     * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-value.html#ProtocolBinary::MYSQL_TYPE_TIME">MYSQL_TYPE_TIME</a>
     *
     * @param date date
     */
    public void writeTime(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        Timestamp timestamp = new Timestamp(date.getTime());
        int millisecond = timestamp.getNanos();
        boolean isTimeValueAbsent = 0 == hour && 0 == minute && 0 == second;
        boolean isMillisecondValueAbsent = 0 == millisecond;
        if (isTimeValueAbsent && isMillisecondValueAbsent) {
            writeInt1(0);
        } else if (isMillisecondValueAbsent) {
            writeInt1(8);
            writeInt1(0);
            writeInt4(0);
            writeInt1(hour);
            writeInt1(minute);
            writeInt1(second);
        } else {
            writeInt1(12);
            writeInt1(0);
            writeInt4(0);
            writeInt1(hour);
            writeInt1(minute);
            writeInt1(second);
            writeInt4(millisecond);
        }
    }
}
