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

import java.io.Serializable;

/**
 * Json value decoder.
 * https://github.com/mysql/mysql-server/blob/5.7/sql/json_binary.h
 */
public final class JsonValueDecoder {

    /**
     * Decode mysql json binary data to json string.
     *
     * @param in buffer
     * @return string
     */
    public static Serializable decode(final ByteBuf in) {
        int valueType = DataTypesCodec.readUnsignedInt1(in);
        StringBuilder result = new StringBuilder();
        decodeValue(valueType, 1, in, result);
        return result.toString();
    }

    private static void decodeValue(final int type, final int offset, final ByteBuf in, final StringBuilder out) {
        int oldOffset = in.readerIndex();
        // set reader index to entry start position
        in.readerIndex(offset);
        try {
            switch (type) {
                case JsonValueTypes.SMALL_JSON_OBJECT:
                    decodeJsonObject(true, in.slice(), out);
                    break;
                case JsonValueTypes.LARGE_JSON_OBJECT:
                    decodeJsonObject(false, in.slice(), out);
                    break;
                case JsonValueTypes.SMALL_JSON_ARRAY:
                    decodeJsonArray(true, in.slice(), out);
                    break;
                case JsonValueTypes.LARGE_JSON_ARRAY:
                    decodeJsonArray(false, in.slice(), out);
                    break;
                case JsonValueTypes.INT16:
                    out.append(DataTypesCodec.readInt2LE(in));
                    break;
                case JsonValueTypes.UINT16:
                    out.append(DataTypesCodec.readUnsignedInt2LE(in));
                    break;
                case JsonValueTypes.INT32:
                    out.append(DataTypesCodec.readInt4LE(in));
                    break;
                case JsonValueTypes.UINT32:
                    out.append(DataTypesCodec.readUnsignedInt4LE(in));
                    break;
                case JsonValueTypes.INT64:
                    out.append(DataTypesCodec.readInt8LE(in));
                    break;
                case JsonValueTypes.UINT64:
                    out.append(DataTypesCodec.readUnsignedInt8LE(in));
                    break;
                case JsonValueTypes.DOUBLE:
                    out.append(DataTypesCodec.readDoubleLE(in));
                    break;
                case JsonValueTypes.STRING:
                    outputString(decodeString(in.slice()), out);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        } finally {
            in.readerIndex(oldOffset);
        }
    }

    private static void decodeJsonObject(final boolean isSmall, final ByteBuf in, final StringBuilder out) {
        out.append('{');
        int count = getIntBasedObjectSize(in, isSmall);
        int size = getIntBasedObjectSize(in, isSmall);
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = decodeKeyEntry(isSmall, in);
        }
        for (int i = 0; i < count; i++) {
            if (0 < i) {
                out.append(',');
            }
            out.append('"').append(keys[i]).append("\":");
            decodeValueEntry(isSmall, in, out);
        }
        out.append('}');
    }
    
    private static void decodeJsonArray(final boolean isSmall, final ByteBuf in, final StringBuilder out) {
        out.append('[');
        int count = getIntBasedObjectSize(in, isSmall);
        int size = getIntBasedObjectSize(in, isSmall);
        for (int i = 0; i < count; i++) {
            if (0 < i) {
                out.append(',');
            }
            decodeValueEntry(isSmall, in, out);
        }
        out.append(']');
    }
    
    private static String decodeKeyEntry(final boolean isSmall, final ByteBuf in) {
        int offset = getIntBasedObjectSize(in, isSmall);
        int length = DataTypesCodec.readUnsignedInt2LE(in);
        byte[] data = new byte[length];
        in.getBytes(offset, data, 0, length);
        return new String(data);
    }
    
    private static void decodeValueEntry(final boolean isSmall, final ByteBuf in, final StringBuilder out) {
        int type = DataTypesCodec.readUnsignedInt1(in);
        switch (type) {
            case JsonValueTypes.SMALL_JSON_OBJECT:
            case JsonValueTypes.LARGE_JSON_OBJECT:
            case JsonValueTypes.SMALL_JSON_ARRAY:
            case JsonValueTypes.LARGE_JSON_ARRAY:
            case JsonValueTypes.INT64:
            case JsonValueTypes.UINT64:
            case JsonValueTypes.DOUBLE:
            case JsonValueTypes.STRING:
                decodeValue(type, getIntBasedObjectSize(in, isSmall), in, out);
                break;
            case JsonValueTypes.INT16:
                out.append(DataTypesCodec.readInt2LE(in));
                if (!isSmall) {
                    DataTypesCodec.skipBytes(2, in);
                }
                break;
            case JsonValueTypes.UINT16:
                out.append(getIntBasedObjectSize(in, isSmall));
                break;
            case JsonValueTypes.INT32:
                if (isSmall) {
                    decodeValue(type, DataTypesCodec.readUnsignedInt2LE(in), in, out);
                } else {
                    out.append(DataTypesCodec.readInt4LE(in));
                }
                break;
            case JsonValueTypes.UINT32:
                if (isSmall) {
                    decodeValue(type, DataTypesCodec.readUnsignedInt2LE(in), in, out);
                } else {
                    out.append(DataTypesCodec.readUnsignedInt4LE(in));
                }
                break;
            case JsonValueTypes.LITERAL:
                outputLiteral(getIntBasedObjectSize(in, isSmall), out);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private static int getIntBasedObjectSize(final ByteBuf in, final boolean isSmall) {
        return isSmall ? DataTypesCodec.readUnsignedInt2LE(in) : (int) DataTypesCodec.readUnsignedInt4LE(in);
    }
    
    private static void outputLiteral(final int inlinedValue, final StringBuilder out) {
        switch (inlinedValue) {
            case JsonValueTypes.LITERAL_NULL:
                out.append("null");
                break;
            case JsonValueTypes.LITERAL_TRUE:
                out.append("true");
                break;
            case JsonValueTypes.LITERAL_FALSE:
                out.append("false");
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private static String decodeString(final ByteBuf in) {
        int length = decodeDataLength(in);
        return DataTypesCodec.readFixedLengthString(length, in);
    }
    
    private static int decodeDataLength(final ByteBuf in) {
        int length = 0;
        for (int i = 0; ; i++) {
            int data = DataTypesCodec.readUnsignedInt1(in);
            length |= (data & 0x7f) << (7 * i);
            if (0 == (data & 0x80)) {
                break;
            }
        }
        return length;
    }
    
    private static void outputString(final String str, final StringBuilder out) {
        out.append('"');
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == '"') {
                out.append('\\');
            } else if (c == '\\') {
                out.append('\\');
            }
            out.append(c);
        }
        out.append('"');
    }
    
    class JsonValueTypes {
    
        public static final byte SMALL_JSON_OBJECT = 0x00;
    
        public static final byte LARGE_JSON_OBJECT = 0x01;
    
        public static final byte SMALL_JSON_ARRAY = 0x02;

        public static final byte LARGE_JSON_ARRAY = 0x03;

        /**
         * Literal(true/false/null).
         */
        public static final byte LITERAL = 0x04;

        public static final byte LITERAL_NULL = 0x00;

        public static final byte LITERAL_TRUE = 0x01;

        public static final byte LITERAL_FALSE = 0x02;

        public static final byte INT16 = 0x05;

        public static final byte UINT16 = 0x06;

        public static final byte INT32 = 0x07;

        public static final byte UINT32 = 0x08;

        public static final byte INT64 = 0x09;

        public static final byte UINT64 = 0x0a;

        public static final byte DOUBLE = 0x0b;

        /**
         * Utf8mb4 string.
         */
        public static final byte STRING = 0x0c;

        /**
         * Custom data (any MySQL data type).
         */
        public static final byte CUSTOM_DATA = 0x0f;
    }
}
