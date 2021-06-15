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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * JSON type value decoder for MySQL.
 *
 * @see <a href="https://github.com/mysql/mysql-server/blob/5.7/sql/json_binary.h">json_binary</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLJsonValueDecoder {
    
    private static final BigInteger MAX_BIG_INTEGER_VALUE = new BigInteger("18446744073709551615");
    
    /**
     * Decode mysql json binary data to json string.
     *
     * @param byteBuf json binary value payload
     * @return json string
     */
    public static Serializable decode(final ByteBuf byteBuf) {
        int valueType = byteBuf.readUnsignedByte() & 0xff;
        StringBuilder result = new StringBuilder();
        decodeValue(valueType, 1, byteBuf, result);
        return result.toString();
    }
    
    private static void decodeValue(final int type, final int offset, final ByteBuf byteBuf, final StringBuilder result) {
        int oldOffset = byteBuf.readerIndex();
        byteBuf.readerIndex(offset);
        try {
            switch (type) {
                case JsonValueTypes.SMALL_JSON_OBJECT:
                    decodeJsonObject(true, byteBuf.slice(), result);
                    break;
                case JsonValueTypes.LARGE_JSON_OBJECT:
                    decodeJsonObject(false, byteBuf.slice(), result);
                    break;
                case JsonValueTypes.SMALL_JSON_ARRAY:
                    decodeJsonArray(true, byteBuf.slice(), result);
                    break;
                case JsonValueTypes.LARGE_JSON_ARRAY:
                    decodeJsonArray(false, byteBuf.slice(), result);
                    break;
                case JsonValueTypes.INT16:
                    result.append(byteBuf.readShortLE());
                    break;
                case JsonValueTypes.UINT16:
                    result.append(byteBuf.readUnsignedShortLE());
                    break;
                case JsonValueTypes.INT32:
                    result.append(byteBuf.readIntLE());
                    break;
                case JsonValueTypes.UINT32:
                    result.append(byteBuf.readUnsignedIntLE());
                    break;
                case JsonValueTypes.INT64:
                    result.append(byteBuf.readLongLE());
                    break;
                case JsonValueTypes.UINT64:
                    result.append(readUnsignedLongLE(byteBuf));
                    break;
                case JsonValueTypes.DOUBLE:
                    result.append(byteBuf.readDoubleLE());
                    break;
                case JsonValueTypes.STRING:
                    outputString(decodeString(byteBuf.slice()), result);
                    break;
                default:
                    throw new UnsupportedOperationException(String.valueOf(type));
            }
        } finally {
            byteBuf.readerIndex(oldOffset);
        }
    }
    
    private static BigInteger readUnsignedLongLE(final ByteBuf byteBuf) {
        long value = byteBuf.readLongLE();
        return 0 <= value ? BigInteger.valueOf(value) : MAX_BIG_INTEGER_VALUE.add(BigInteger.valueOf(1 + value));
    }
    
    private static void decodeJsonObject(final boolean isSmall, final ByteBuf byteBuf, final StringBuilder result) {
        result.append('{');
        int count = getIntBasedObjectSize(byteBuf, isSmall);
        int size = getIntBasedObjectSize(byteBuf, isSmall);
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = decodeKeyEntry(isSmall, byteBuf);
        }
        for (int i = 0; i < count; i++) {
            if (0 < i) {
                result.append(',');
            }
            result.append('"').append(keys[i]).append("\":");
            decodeValueEntry(isSmall, byteBuf, result);
        }
        result.append('}');
    }
    
    private static void decodeJsonArray(final boolean isSmall, final ByteBuf byteBuf, final StringBuilder result) {
        result.append('[');
        int count = getIntBasedObjectSize(byteBuf, isSmall);
        int size = getIntBasedObjectSize(byteBuf, isSmall);
        for (int i = 0; i < count; i++) {
            if (0 < i) {
                result.append(',');
            }
            decodeValueEntry(isSmall, byteBuf, result);
        }
        result.append(']');
    }
    
    private static String decodeKeyEntry(final boolean isSmall, final ByteBuf byteBuf) {
        int offset = getIntBasedObjectSize(byteBuf, isSmall);
        int length = byteBuf.readUnsignedShortLE();
        byte[] data = new byte[length];
        byteBuf.getBytes(offset, data, 0, length);
        return new String(data);
    }
    
    private static void decodeValueEntry(final boolean isSmall, final ByteBuf byteBuf, final StringBuilder stringBuilder) {
        int type = byteBuf.readUnsignedByte() & 0xff;
        switch (type) {
            case JsonValueTypes.SMALL_JSON_OBJECT:
            case JsonValueTypes.LARGE_JSON_OBJECT:
            case JsonValueTypes.SMALL_JSON_ARRAY:
            case JsonValueTypes.LARGE_JSON_ARRAY:
            case JsonValueTypes.INT64:
            case JsonValueTypes.UINT64:
            case JsonValueTypes.DOUBLE:
            case JsonValueTypes.STRING:
                decodeValue(type, getIntBasedObjectSize(byteBuf, isSmall), byteBuf, stringBuilder);
                break;
            case JsonValueTypes.INT16:
                stringBuilder.append(byteBuf.readShortLE());
                if (!isSmall) {
                    byteBuf.skipBytes(2);
                }
                break;
            case JsonValueTypes.UINT16:
                stringBuilder.append(getIntBasedObjectSize(byteBuf, isSmall));
                break;
            case JsonValueTypes.INT32:
                if (isSmall) {
                    decodeValue(type, byteBuf.readUnsignedShortLE(), byteBuf, stringBuilder);
                } else {
                    stringBuilder.append(byteBuf.readIntLE());
                }
                break;
            case JsonValueTypes.UINT32:
                if (isSmall) {
                    decodeValue(type, byteBuf.readUnsignedShortLE(), byteBuf, stringBuilder);
                } else {
                    stringBuilder.append(byteBuf.readUnsignedIntLE());
                }
                break;
            case JsonValueTypes.LITERAL:
                outputLiteral(getIntBasedObjectSize(byteBuf, isSmall), stringBuilder);
                break;
            default:
                throw new UnsupportedOperationException(String.valueOf(type));
        }
    }
    
    private static int getIntBasedObjectSize(final ByteBuf byteBuf, final boolean isSmall) {
        return isSmall ? byteBuf.readUnsignedShortLE() : (int) byteBuf.readUnsignedIntLE();
    }
    
    private static void outputLiteral(final int inlineValue, final StringBuilder out) {
        switch (inlineValue) {
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
                throw new UnsupportedOperationException(String.valueOf(inlineValue));
        }
    }
    
    private static String decodeString(final ByteBuf byteBuf) {
        int length = decodeDataLength(byteBuf);
        byte[] buffer = new byte[length];
        byteBuf.readBytes(buffer, 0, length);
        return new String(buffer);
    }
    
    private static int decodeDataLength(final ByteBuf byteBuf) {
        int result = 0;
        for (int i = 0; ; i++) {
            int data = byteBuf.readUnsignedByte();
            result |= (data & 0x7f) << (7 * i);
            if (0 == (data & 0x80)) {
                break;
            }
        }
        return result;
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
    
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class JsonValueTypes {
        
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
