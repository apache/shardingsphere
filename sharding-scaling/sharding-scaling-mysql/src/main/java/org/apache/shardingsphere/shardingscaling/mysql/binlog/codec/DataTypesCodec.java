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

import java.math.BigInteger;
import java.util.BitSet;

/**
 * Data types codec.
 *
 * <p>
 *     https://dev.mysql.com/doc/internals/en/describing-packets.html
 * </p>
 */
public final class DataTypesCodec {
    
    private static final BigInteger MAX_BIG_INTEGER_VALUE = new BigInteger("18446744073709551615");
    
    /**
     * Skip length byte in {@code ByteBuf}.
     *
     * @param length to skip
     * @param in byte buffer
     */
    public static void skipBytes(final int length, final ByteBuf in) {
        in.skipBytes(length);
    }
    
    /**
     * Read nul from {@code ByteBuf}.
     *
     * @param in byte buffer
     */
    public static void readNul(final ByteBuf in) {
        in.readByte();
    }
    
    /**
     * Read little endian byte order float from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return float value
     */
    public static float readFloatLE(final ByteBuf in) {
        return in.readFloatLE();
    }
    
    /**
     * Read little endian byte order double from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return double value
     */
    public static double readDoubleLE(final ByteBuf in) {
        return in.readDoubleLE();
    }
    
    /**
     * Read bitmap from {@code ByteBuf}.
     *
     * @param length length
     * @param in     byte buffer
     * @return bitset value
     */
    public static BitSet readBitmap(final int length, final ByteBuf in) {
        BitSet bitSet = new BitSet(length);
        for (int bit = 0; bit < length; bit += 8) {
            int flag = ((int) in.readByte()) & 0xff;
            if (flag != 0) {
                for (int i = 0; i < 8; i++) {
                    if ((flag & (0x01 << i)) != 0) {
                        bitSet.set(bit + i);
                    }
                }
            }
        }
        return bitSet;
    }
    
    /**
     * Read 1 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static byte readInt1(final ByteBuf in) {
        return in.readByte();
    }
    
    /**
     * Read little endian byte order 2 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static short readInt2LE(final ByteBuf in) {
        return in.readShortLE();
    }
    
    /**
     * Read little endian byte order 3 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readInt3LE(final ByteBuf in) {
        return in.readMediumLE();
    }
    
    /**
     * Read little endian byte order 4 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readInt4LE(final ByteBuf in) {
        return in.readIntLE();
    }
    
    /**
     * Read little endian byte order 8 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readInt8LE(final ByteBuf in) {
        return in.readLongLE();
    }
    
    /**
     * Read unsigned ittle endian byte order 1 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return shor value
     */
    public static short readUnsignedInt1(final ByteBuf in) {
        return in.readUnsignedByte();
    }
    
    /**
     * Read unsigned big endian byte order 2 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readUnsignedInt2BE(final ByteBuf in) {
        return in.readUnsignedShort();
    }
    
    /**
     * Read unsigned little endian byte order 2 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readUnsignedInt2LE(final ByteBuf in) {
        return in.readUnsignedShortLE();
    }
    
    /**
     * Read unsigned big endian byte order 3 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readUnsignedInt3BE(final ByteBuf in) {
        return in.readUnsignedMedium();
    }
    
    /**
     * Read unsigned little endian byte order 3 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readUnsignedInt3LE(final ByteBuf in) {
        return in.readUnsignedMediumLE();
    }
    
    /**
     * Read unsigned big endian byte order 4 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readUnsignedInt4BE(final ByteBuf in) {
        return in.readUnsignedInt();
    }
    
    /**
     * Read unsigned little endian byte order 4 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readUnsignedInt4LE(final ByteBuf in) {
        return in.readUnsignedIntLE();
    }
    
    /**
     * Read unsigned big endian byte order 5 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readUnsignedInt5BE(final ByteBuf in) {
        long result = 0;
        for (int i = 4; i >= 0; i--) {
            result |= ((long) (0xff & in.readByte())) << (8 * i);
        }
        return result;
    }
    
    /**
     * Read unsigned little endian byte order 6 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readUnsignedInt6LE(final ByteBuf in) {
        long result = 0;
        for (int i = 0; i < 6; i++) {
            result |= ((long) (0xff & in.readByte())) << (8 * i);
        }
        return result;
    }
    
    /**
     * Read unsigned little endian byte order 8 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return big integer value
     */
    public static BigInteger readUnsignedInt8LE(final ByteBuf in) {
        long value = readInt8LE(in);
        return 0 <= value ? BigInteger.valueOf(value) : MAX_BIG_INTEGER_VALUE.add(BigInteger.valueOf(1 + value));
    }
    
    /**
     * Read unsigned little endian byte order length coded integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readLengthCodedIntLE(final ByteBuf in) {
        int firstByte = in.readByte() & 0xFF;
        switch (firstByte) {
            case 251:
                return -1;
            case 252:
                return in.readUnsignedShortLE();
            case 253:
                return in.readUnsignedMediumLE();
            case 254:
                return in.readLongLE();
            default:
                return firstByte;
        }
    }
    
    /**
     * Read byte array from {@code ByteBuf}.
     *
     * @param length length
     * @param in     byte buffer
     * @return byte array
     */
    public static byte[] readBytes(final int length, final ByteBuf in) {
        byte[] buffer = new byte[length];
        in.readBytes(buffer, 0, length);
        return buffer;
    }
    
    /**
     * Read fixed length string from {@code ByteBuf}.
     *
     * @param length length
     * @param in     byte buffer
     * @return string value
     */
    public static String readFixedLengthString(final int length, final ByteBuf in) {
        byte[] buffer = new byte[length];
        in.readBytes(buffer, 0, length);
        return new String(buffer);
    }
    
    /**
     * Read length coded string from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return string value
     */
    public static String readLengthCodedString(final ByteBuf in) {
        int length = (int) readLengthCodedIntLE(in);
        return readFixedLengthString(length, in);
    }
    
    /**
     * Read nul terminated string from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return string value
     */
    public static String readNulTerminatedString(final ByteBuf in) {
        int length = in.bytesBefore((byte) 0x00);
        String str = readFixedLengthString(length, in);
        readNul(in);
        return str;
    }
    
    /**
     * Write byte array to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out  target byte buf
     */
    public static void writeByte(final byte data, final ByteBuf out) {
        out.writeByte(data);
    }
    
    /**
     * Write big endian byte order 5 byte integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out  target byte buf
     */
    public static void writeInt5(final long data, final ByteBuf out) {
        for (int i = 4; i >= 0; i--) {
            out.writeByte((byte) (data >> (8 * i)));
        }
    }
    
    /**
     * Write big endian byte order n byte integer to {@code ByteBuf}.
     *
     * @param length length of n
     * @param data the data
     * @param out  target byte buf
     */
    public static void writeIntN(final int length, final long data, final ByteBuf out) {
        for (int i = length - 1; i >= 0; i--) {
            out.writeByte((byte) (data >> (8 * i)));
        }
    }
    
    /**
     * Write little endian byte order 2 byte integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out  target byte buf
     */
    public static void writeInt2LE(final short data, final ByteBuf out) {
        out.writeShortLE(data);
    }
    
    /**
     * Write little endian byte order 4 byte integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out  target byte buf
     */
    public static void writeInt4LE(final int data, final ByteBuf out) {
        out.writeIntLE(data);
    }
    
    /**
     * Write little endian byte order n byte integer to {@code ByteBuf}.
     *
     * @param length length of n
     * @param data the data
     * @param out  target byte buf
     */
    public static void writeIntNLE(final int length, final long data, final ByteBuf out) {
        for (int i = 0; i < length; i++) {
            out.writeByte((byte) (data >> (8 * i)));
        }
    }
    
    /**
     * Write little endian byte order length coded integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out  target byte buf
     */
    public static void writeLengthCodedInt(final int data, final ByteBuf out) {
        if (data < 252) {
            out.writeByte((byte) data);
        } else if (data < (1 << 16L)) {
            out.writeByte((byte) 252);
            out.writeShortLE(data);
        } else if (data < (1 << 24L)) {
            out.writeByte((byte) 253);
            out.writeMediumLE(data);
        } else {
            out.writeByte((byte) 254);
            out.writeIntLE(data);
        }
    }
    
    /**
     * Write byte array to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out  target byte buf
     */
    public static void writeBytes(final byte[] data, final ByteBuf out) {
        out.writeBytes(data);
    }
    
    /**
     * Write nul terminated string to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out  target byte buf
     */
    public static void writeNulTerminatedString(final String data, final ByteBuf out) {
        out.writeBytes(data.getBytes());
        out.writeByte(0x00);
    }
    
    /**
     * Write length coded binary to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out  target byte buf
     */
    public static void writeLengthCodedBinary(final byte[] data, final ByteBuf out) {
        writeLengthCodedInt(data.length, out);
        out.writeBytes(data);
    }
}
