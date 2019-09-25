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

/**
 * Data types codec.
 *
 * @author avalon566
 * @author yangyi
 */
public final class DataTypesCodec {
    
    /**
     * Read byte from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return byte value
     */
    public static byte readByte(final ByteBuf in) {
        return in.readByte();
    }
    
    /**
     * Write byte to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeByte(final byte data, final ByteBuf out) {
        out.writeByte(data);
    }
    
    /**
     * Read short from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return short value
     */
    public static short readShort(final ByteBuf in) {
        return in.readShortLE();
    }
    
    /**
     * Write short to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeShort(final short data, final ByteBuf out) {
        out.writeShortLE(data);
    }
    
    /**
     * Read int from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return int value
     */
    public static int readInt(final ByteBuf in) {
        return in.readIntLE();
    }
    
    /**
     * Write int to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeInt(final int data, final ByteBuf out) {
        out.writeIntLE(data);
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
     * Write byte array to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeBytes(final byte[] data, final ByteBuf out) {
        out.writeBytes(data);
    }
    
    /**
     * Read string until terminated from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return string value
     */
    public static String readNullTerminatedString(final ByteBuf in) {
        var length = in.bytesBefore((byte) 0x00);
        var data = new byte[length];
        in.readBytes(data, 0, length);
        in.readByte();
        return new String(data);
    }
    
    /**
     * Write string with terminated to {@code ByteBuf}.
     *
     * @param data wrote value
     * @param out target byte buf
     */
    public static void writeNullTerminatedString(final String data, final ByteBuf out) {
        out.writeBytes(data.getBytes());
        out.writeByte(0x00);
    }
    
    /**
     * Read length coded from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return length
     */
    public static long readLengthCoded(final ByteBuf in) {
        int firstByte = in.readByte() & 0xFF;
        switch (firstByte) {
            case 251:
                return -1;
            case 252:
                return in.readShortLE();
            case 253:
                return in.readUnsignedMediumLE();
            case 254:
                return in.readLongLE();
            default:
                return firstByte;
        }
    }
    
    /**
     * Write length coded to {@code ByteBuf}.
     *
     * @param length length
     * @param out target byte buf
     */
    public static void writeLengthCoded(final int length, final ByteBuf out) {
        if (length < 252) {
            out.writeByte((byte) length);
        } else if (length < (1 << 16L)) {
            out.writeByte((byte) 252);
            out.writeShortLE(length);
        } else if (length < (1 << 24L)) {
            out.writeByte((byte) 253);
            out.writeMediumLE(length);
        } else {
            out.writeByte((byte) 254);
            out.writeIntLE(length);
        }
    }
    
    /**
     * Read length coded string from {@code ByteBuf}.
     *
     * @param in byte buffer
     * @return length coded string value
     */
    public static String readLengthCodedString(final ByteBuf in) {
        var length = (int) readLengthCoded(in);
        var buffer = new byte[length];
        in.readBytes(buffer, 0, length);
        return new String(buffer);
    }
    
    /**
     * Write length coded binary to {@code ByteBuf}.
     *
     * @param data length coded binary
     * @param out target byte buf
     */
    public static void writeLengthCodedBinary(final byte[] data, final ByteBuf out) {
        writeLengthCoded(data.length, out);
        out.writeBytes(data);
    }
}
