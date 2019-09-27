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

package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import lombok.var;

import java.util.BitSet;

/**
 * Data types codec.
 * https://dev.mysql.com/doc/internals/en/describing-packets.html
 *
 * @author avalon566
 * @author yangyi
 */
public final class DataTypesCodec {

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
     * @param in byte buffer
     * @return bitset value
     */
    public static BitSet readBitmap(final int length, final ByteBuf in) {
        var bitSet = new BitSet(length);
        for (int bit = 0; bit < length; bit += 8) {
            int flag = ((int) in.readByte()) & 0xff;
            if (flag != 0) {
                if ((flag & 0x01) != 0) {
                    bitSet.set(bit);
                }
                if ((flag & 0x02) != 0) {
                    bitSet.set(bit + 1);
                }
                if ((flag & 0x04) != 0) {
                    bitSet.set(bit + 2);
                }
                if ((flag & 0x08) != 0) {
                    bitSet.set(bit + 3);
                }
                if ((flag & 0x10) != 0) {
                    bitSet.set(bit + 4);
                }
                if ((flag & 0x20) != 0) {
                    bitSet.set(bit + 5);
                }
                if ((flag & 0x40) != 0) {
                    bitSet.set(bit + 6);
                }
                if ((flag & 0x80) != 0) {
                    bitSet.set(bit + 7);
                }
            }
        }
        return bitSet;
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
        return (long) (0xff & in.readByte()) << 32
                | ((long) (0xff & in.readByte())) << 24
                | ((long) (0xff & in.readByte())) << 16
                | ((long) (0xff & in.readByte())) << 8
                | ((long) (0xff & in.readByte()));
    }

    /**
     * Read unsigned little endian byte order 6 byte integer from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return long value
     */
    public static long readUnsignedInt6LE(final ByteBuf in) {
        return (long) (0xff & in.readByte())
                | ((long) (0xff & in.readByte())) << 8
                | ((long) (0xff & in.readByte())) << 16
                | ((long) (0xff & in.readByte())) << 24
                | ((long) (0xff & in.readByte())) << 32
                | ((long) (0xff & in.readByte())) << 40;
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
     * @param in byte buffer
     * @return byte array
     */
    public static byte[] readBytes(final int length, final ByteBuf in) {
        var buffer = new byte[length];
        in.readBytes(buffer, 0, length);
        return buffer;
    }

    /**
     * Read fixed length string from {@code ByteBuf}.
     *
     * @param length length
     * @param in byte buffer
     * @return string value
     */
    public static String readFixedLengthString(final int length, final ByteBuf in) {
        var buffer = new byte[length];
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
        var length = (int) readLengthCodedIntLE(in);
        return readFixedLengthString(length, in);
    }

    /**
     * Read nul terminated string from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return string value
     */
    public static String readNulTerminatedString(final ByteBuf in) {
        var length = in.bytesBefore((byte) 0x00);
        var str = readFixedLengthString(length, in);
        readNul(in);
        return str;
    }

    /**
     * Write byte array to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeByte(final byte data, final ByteBuf out) {
        out.writeByte(data);
    }

    /**
     * Write little endian byte order 2 byte integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out target byte buf
     */
    public static void writeInt2LE(final short data, final ByteBuf out) {
        out.writeShortLE(data);
    }

    /**
     * Write little endian byte order 4 byte integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out target byte buf
     */
    public static void writeInt4LE(final int data, final ByteBuf out) {
        out.writeIntLE(data);
    }

    /**
     * Write little endian byte order length coded integer to {@code ByteBuf}.
     *
     * @param data the data
     * @param out target byte buf
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
     * @param out target byte buf
     */
    public static void writeBytes(final byte[] data, final ByteBuf out) {
        out.writeBytes(data);
    }

    /**
     * Write nul terminated string to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeNulTerminatedString(final String data, final ByteBuf out) {
        out.writeBytes(data.getBytes());
        out.writeByte(0x00);
    }

    /**
     * Write length coded binary to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeLengthCodedBinary(final byte[] data, final ByteBuf out) {
        writeLengthCodedInt(data.length, out);
        out.writeBytes(data);
    }
}
