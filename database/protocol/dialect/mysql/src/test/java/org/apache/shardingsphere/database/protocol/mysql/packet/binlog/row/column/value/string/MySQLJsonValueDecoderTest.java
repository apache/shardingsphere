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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLJsonValueDecoder.JsonValueTypes;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLJsonValueDecoderTest {
    
    private static final int SMALL_JSON_INT_LENGTH = 2;
    
    private static final int LARGE_JSON_INT_LENGTH = 4;
    
    private static final int SMALL_JSON_KEY_META_DATA_LENGTH = SMALL_JSON_INT_LENGTH + 2;
    
    private static final int SMALL_JSON_VALUE_META_DATA_LENGTH = SMALL_JSON_INT_LENGTH + 1;
    
    private static final int LARGE_JSON_KEY_META_DATA_LENGTH = LARGE_JSON_INT_LENGTH + 2;
    
    private static final int LARGE_JSON_VALUE_META_DATA_LENGTH = LARGE_JSON_INT_LENGTH + 1;
    
    @Test
    void assertDecodeSmallJsonObjectWithLiteral() {
        List<Object[]> jsonEntries = new LinkedList<>();
        jsonEntries.add(new Object[]{JsonValueTypes.LITERAL, "key1", JsonValueTypes.LITERAL_NULL});
        jsonEntries.add(new Object[]{JsonValueTypes.LITERAL, "key2", JsonValueTypes.LITERAL_TRUE});
        jsonEntries.add(new Object[]{JsonValueTypes.LITERAL, "key3", JsonValueTypes.LITERAL_FALSE});
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        assertThat(MySQLJsonValueDecoder.decode(payload), is("{\"key1\":null,\"key2\":true,\"key3\":false}"));
    }
    
    @Test
    void assertDecodeLargeJsonObjectWithLiteral() {
        List<Object[]> jsonEntries = new LinkedList<>();
        jsonEntries.add(new Object[]{JsonValueTypes.LITERAL, "key1", JsonValueTypes.LITERAL_NULL});
        jsonEntries.add(new Object[]{JsonValueTypes.LITERAL, "key2", JsonValueTypes.LITERAL_TRUE});
        jsonEntries.add(new Object[]{JsonValueTypes.LITERAL, "key3", JsonValueTypes.LITERAL_FALSE});
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, false);
        assertThat(MySQLJsonValueDecoder.decode(payload), is("{\"key1\":null,\"key2\":true,\"key3\":false}"));
    }
    
    @Test
    void assertDecodeSmallJsonArray() {
        List<Object[]> jsonEntries = new LinkedList<>();
        jsonEntries.add(new Object[]{JsonValueTypes.INT16, null, 0x00007fff});
        jsonEntries.add(new Object[]{JsonValueTypes.INT16, null, 0x00008000});
        ByteBuf payload = mockJsonArrayByteBuf(jsonEntries, true);
        assertThat(MySQLJsonValueDecoder.decode(payload), is("[32767,-32768]"));
    }
    
    @Test
    void assertDecodeLargeJsonArray() {
        List<Object[]> jsonEntries = new LinkedList<>();
        jsonEntries.add(new Object[]{JsonValueTypes.INT16, null, 0x00007fff});
        jsonEntries.add(new Object[]{JsonValueTypes.INT16, null, 0x00008000});
        ByteBuf payload = mockJsonArrayByteBuf(jsonEntries, false);
        assertThat(MySQLJsonValueDecoder.decode(payload), is("[32767,-32768]"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeTopLevelScalarArguments")
    void assertDecodeTopLevelScalar(final String name, final ByteBuf payload, final String expected) {
        assertThat(MySQLJsonValueDecoder.decode(payload), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeUnsupportedArguments")
    void assertDecodeUnsupportedOperation(final String name, final ByteBuf payload) {
        assertThrows(UnsupportedSQLOperationException.class, () -> MySQLJsonValueDecoder.decode(payload));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeObjectWithInlineNumberArguments")
    void assertDecodeObjectWithInlineNumber(final String name, final boolean isSmall, final byte type, final int firstValue, final int secondValue, final String expected) {
        List<Object[]> jsonEntries = new LinkedList<>();
        jsonEntries.add(new Object[]{type, "key1", firstValue});
        jsonEntries.add(new Object[]{type, "key2", secondValue});
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, isSmall);
        assertThat(MySQLJsonValueDecoder.decode(payload), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeSmallJsonObjectWithNonInlineTypeArguments")
    void assertDecodeSmallJsonObjectWithNonInlineType(final String name, final List<Object[]> jsonEntries, final String expected) {
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        assertThat(MySQLJsonValueDecoder.decode(payload), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeNestedJsonArguments")
    void assertDecodeNestedJson(final String name, final boolean isSmall, final byte nestedType, final String nestedKey, final String expected) {
        List<Object[]> nestedEntries = Collections.singletonList(new Object[]{JsonValueTypes.INT32, nestedKey, 111});
        ByteBuf payload = mockJsonObjectByteBuf(Collections.singletonList(new Object[]{nestedType, "subJson", nestedEntries}), isSmall);
        assertThat(MySQLJsonValueDecoder.decode(payload), is(expected));
    }
    
    private ByteBuf mockJsonObjectByteBuf(final List<Object[]> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(isSmall ? JsonValueTypes.SMALL_JSON_OBJECT : JsonValueTypes.LARGE_JSON_OBJECT);
        result.writeBytes(mockJsonObjectByteBufValue(jsonEntries, isSmall));
        return result;
    }
    
    private ByteBuf mockJsonObjectByteBufValue(final List<Object[]> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        writeInt(result, jsonEntries.size(), isSmall);
        writeInt(result, 0, isSmall);
        int startOffset = isSmall
                ? 1 + SMALL_JSON_INT_LENGTH + SMALL_JSON_INT_LENGTH + jsonEntries.size() * SMALL_JSON_KEY_META_DATA_LENGTH + jsonEntries.size() * SMALL_JSON_VALUE_META_DATA_LENGTH - 1
                : 1 + LARGE_JSON_INT_LENGTH + LARGE_JSON_INT_LENGTH + jsonEntries.size() * LARGE_JSON_KEY_META_DATA_LENGTH + jsonEntries.size() * LARGE_JSON_VALUE_META_DATA_LENGTH - 1;
        ByteBuf keyByteBuf = writeKeys(result, jsonEntries, startOffset, isSmall);
        startOffset += keyByteBuf.readableBytes();
        ByteBuf valueByteBuf = writeValues(result, jsonEntries, startOffset, isSmall);
        result.writeBytes(keyByteBuf);
        result.writeBytes(valueByteBuf);
        return result;
    }
    
    private ByteBuf mockJsonArrayByteBuf(final List<Object[]> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(isSmall ? JsonValueTypes.SMALL_JSON_ARRAY : JsonValueTypes.LARGE_JSON_ARRAY);
        result.writeBytes(mockJsonArrayByteBufValue(jsonEntries, isSmall));
        return result;
    }
    
    private ByteBuf mockJsonArrayByteBufValue(final List<Object[]> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        writeInt(result, jsonEntries.size(), isSmall);
        writeInt(result, 0, isSmall);
        int startOffset = isSmall
                ? 1 + SMALL_JSON_INT_LENGTH + SMALL_JSON_INT_LENGTH + jsonEntries.size() * SMALL_JSON_VALUE_META_DATA_LENGTH - 1
                : 1 + LARGE_JSON_INT_LENGTH + LARGE_JSON_INT_LENGTH + jsonEntries.size() * LARGE_JSON_VALUE_META_DATA_LENGTH - 1;
        ByteBuf valueByteBuf = writeValues(result, jsonEntries, startOffset, isSmall);
        result.writeBytes(valueByteBuf);
        return result;
    }
    
    private void writeInt(final ByteBuf jsonByteBuf, final int value, final boolean isSmall) {
        if (isSmall) {
            jsonByteBuf.writeShortLE(value);
        } else {
            jsonByteBuf.writeIntLE(value);
        }
    }
    
    private ByteBuf writeKeys(final ByteBuf jsonByteBuf, final List<Object[]> jsonEntries, final int startOffset, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        for (Object[] each : jsonEntries) {
            writeInt(jsonByteBuf, startOffset + result.readableBytes(), isSmall);
            byte[] keyBytes = ((String) each[1]).getBytes();
            jsonByteBuf.writeShortLE(keyBytes.length);
            result.writeBytes(keyBytes);
        }
        return result;
    }
    
    private ByteBuf writeValues(final ByteBuf jsonByteBuf, final List<Object[]> jsonEntries, final int startOffset, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        for (Object[] each : jsonEntries) {
            jsonByteBuf.writeByte((byte) each[0]);
            int offsetOrInlineValue = getOffsetOrInlineValue(startOffset, result.readableBytes(), each, isSmall);
            writeInt(jsonByteBuf, offsetOrInlineValue, isSmall);
            writeValueToByteBuf(each, result, isSmall);
        }
        return result;
    }
    
    private int getOffsetOrInlineValue(final int startOffset, final int readableBytes, final Object[] jsonEntry, final boolean isSmall) {
        switch ((byte) jsonEntry[0]) {
            case JsonValueTypes.INT16:
            case JsonValueTypes.UINT16:
                return (int) jsonEntry[2];
            case JsonValueTypes.LITERAL:
                return (byte) jsonEntry[2];
            case JsonValueTypes.INT32:
            case JsonValueTypes.UINT32:
                return isSmall ? startOffset + readableBytes : (int) jsonEntry[2];
            default:
                return startOffset + readableBytes;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void writeValueToByteBuf(final Object[] jsonEntry, final ByteBuf byteBuf, final boolean isSmall) {
        switch ((byte) jsonEntry[0]) {
            case JsonValueTypes.SMALL_JSON_OBJECT:
                byteBuf.writeBytes(mockJsonObjectByteBufValue((List<Object[]>) jsonEntry[2], true));
                break;
            case JsonValueTypes.LARGE_JSON_OBJECT:
                byteBuf.writeBytes(mockJsonObjectByteBufValue((List<Object[]>) jsonEntry[2], false));
                break;
            case JsonValueTypes.SMALL_JSON_ARRAY:
                byteBuf.writeBytes(mockJsonArrayByteBufValue((List<Object[]>) jsonEntry[2], true));
                break;
            case JsonValueTypes.LARGE_JSON_ARRAY:
                byteBuf.writeBytes(mockJsonArrayByteBufValue((List<Object[]>) jsonEntry[2], false));
                break;
            case JsonValueTypes.INT32:
            case JsonValueTypes.UINT32:
                if (isSmall) {
                    byteBuf.writeIntLE((int) jsonEntry[2]);
                }
                break;
            case JsonValueTypes.INT64:
            case JsonValueTypes.UINT64:
                byteBuf.writeLongLE((long) jsonEntry[2]);
                break;
            case JsonValueTypes.DOUBLE:
                byteBuf.writeDoubleLE((double) jsonEntry[2]);
                break;
            case JsonValueTypes.STRING:
                writeString(byteBuf, (String) jsonEntry[2]);
                break;
            default:
        }
    }
    
    private void writeString(final ByteBuf jsonByteBuf, final String value) {
        byte[] result = codecDataLength(value.length());
        jsonByteBuf.writeBytes(result, 0, result.length);
        jsonByteBuf.writeBytes(value.getBytes());
    }
    
    private byte[] codecDataLength(final int length) {
        byte[] lengthData = new byte[32 / 7 + (0 == 32 % 7 ? 0 : 1)];
        for (int i = 0; i < lengthData.length; i++) {
            lengthData[i] = (byte) ((length >> (7 * i)) & 0x7f);
        }
        int index = lengthData.length - 1;
        while (index > 0) {
            if (0 != lengthData[index]) {
                break;
            }
            index--;
        }
        for (int i = 0; i < index; i++) {
            lengthData[i] |= 0x80;
        }
        byte[] result = new byte[index + 1];
        System.arraycopy(lengthData, 0, result, 0, index + 1);
        return result;
    }
    
    private static Stream<Arguments> decodeTopLevelScalarArguments() {
        return Stream.of(
                Arguments.of("decode int16 top-level", mockTopLevelInt16ByteBuf(), "-32768"),
                Arguments.of("decode uint16 top-level", mockTopLevelUInt16ByteBuf(), "65535"),
                Arguments.of("decode string with escaped chars", mockTopLevelStringByteBuf(), "\"a\\\"\\\\b\""));
    }
    
    private static Stream<Arguments> decodeUnsupportedArguments() {
        MySQLJsonValueDecoderTest test = new MySQLJsonValueDecoderTest();
        return Stream.of(
                Arguments.of("throw unsupported exception for top-level custom type", mockUnsupportedTopLevelTypeByteBuf()),
                Arguments.of("throw unsupported exception for value entry custom type",
                        test.mockJsonObjectByteBuf(Collections.singletonList(new Object[]{JsonValueTypes.CUSTOM_DATA, "key1", 1}), true)),
                Arguments.of("throw unsupported exception for literal inline value",
                        test.mockJsonObjectByteBuf(Collections.singletonList(new Object[]{JsonValueTypes.LITERAL, "key1", (byte) 3}), true)));
    }
    
    private static Stream<Arguments> decodeObjectWithInlineNumberArguments() {
        return Stream.of(
                Arguments.of("decode small object with int16", true, JsonValueTypes.INT16, 0x00007fff, 0x00008000, "{\"key1\":32767,\"key2\":-32768}"),
                Arguments.of("decode large object with int16", false, JsonValueTypes.INT16, 0x00007fff, 0x00008000, "{\"key1\":32767,\"key2\":-32768}"),
                Arguments.of("decode small object with uint16", true, JsonValueTypes.UINT16, 0x00007fff, 0x00008000, "{\"key1\":32767,\"key2\":32768}"),
                Arguments.of("decode large object with uint16", false, JsonValueTypes.UINT16, 0x00007fff, 0x00008000, "{\"key1\":32767,\"key2\":32768}"),
                Arguments.of("decode small object with int32", true, JsonValueTypes.INT32, Integer.MAX_VALUE, Integer.MIN_VALUE, "{\"key1\":2147483647,\"key2\":-2147483648}"),
                Arguments.of("decode large object with int32", false, JsonValueTypes.INT32, Integer.MAX_VALUE, Integer.MIN_VALUE, "{\"key1\":2147483647,\"key2\":-2147483648}"),
                Arguments.of("decode small object with uint32", true, JsonValueTypes.UINT32, Integer.MAX_VALUE, Integer.MIN_VALUE, "{\"key1\":2147483647,\"key2\":2147483648}"),
                Arguments.of("decode large object with uint32", false, JsonValueTypes.UINT32, Integer.MAX_VALUE, Integer.MIN_VALUE, "{\"key1\":2147483647,\"key2\":2147483648}"));
    }
    
    private static Stream<Arguments> decodeSmallJsonObjectWithNonInlineTypeArguments() {
        String value1 = "";
        String value2 = Strings.repeat("1", (int) (Math.pow(2D, 7D) - 1D));
        String value3 = Strings.repeat("1", (int) (Math.pow(2D, 7D) - 1D + 1D));
        String value4 = Strings.repeat("1", (int) (Math.pow(2D, 14D) - 1D));
        return Stream.of(
                Arguments.of("decode small object with int64", Arrays.asList(new Object[]{JsonValueTypes.INT64, "key1", Long.MAX_VALUE}, new Object[]{JsonValueTypes.INT64, "key2", Long.MIN_VALUE}),
                        "{\"key1\":9223372036854775807,\"key2\":-9223372036854775808}"),
                Arguments.of("decode small object with uint64", Arrays.asList(new Object[]{JsonValueTypes.UINT64, "key1", Long.MAX_VALUE}, new Object[]{JsonValueTypes.UINT64, "key2", Long.MIN_VALUE}),
                        "{\"key1\":9223372036854775807,\"key2\":9223372036854775808}"),
                Arguments.of("decode small object with double", Arrays.asList(new Object[][]{new Object[]{JsonValueTypes.DOUBLE, "key1", Double.MAX_VALUE}}),
                        "{\"key1\":1.7976931348623157E308}"),
                Arguments.of("decode small object with string",
                        Arrays.asList(new Object[]{JsonValueTypes.STRING, "key1", value1}, new Object[]{JsonValueTypes.STRING, "key2", value2}, new Object[]{JsonValueTypes.STRING, "key3", value3},
                                new Object[]{JsonValueTypes.STRING, "key4", value4}),
                        String.format("{\"key1\":\"%s\",\"key2\":\"%s\",\"key3\":\"%s\",\"key4\":\"%s\"}", value1, value2, value3, value4)));
    }
    
    private static Stream<Arguments> decodeNestedJsonArguments() {
        return Stream.of(
                Arguments.of("decode small object with sub json", true, JsonValueTypes.SMALL_JSON_OBJECT, "key1", "{\"subJson\":{\"key1\":111}}"),
                Arguments.of("decode large object with sub json", false, JsonValueTypes.SMALL_JSON_OBJECT, "key1", "{\"subJson\":{\"key1\":111}}"),
                Arguments.of("decode small object with sub array", true, JsonValueTypes.SMALL_JSON_ARRAY, null, "{\"subJson\":[111]}"));
    }
    
    private static ByteBuf mockTopLevelInt16ByteBuf() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(JsonValueTypes.INT16);
        result.writeShortLE(32768);
        return result;
    }
    
    private static ByteBuf mockTopLevelUInt16ByteBuf() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(JsonValueTypes.UINT16);
        result.writeShortLE(65535);
        return result;
    }
    
    private static ByteBuf mockTopLevelStringByteBuf() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(JsonValueTypes.STRING);
        result.writeByte("a\"\\b".length());
        result.writeBytes("a\"\\b".getBytes());
        return result;
    }
    
    private static ByteBuf mockUnsupportedTopLevelTypeByteBuf() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(JsonValueTypes.CUSTOM_DATA);
        return result;
    }
}
