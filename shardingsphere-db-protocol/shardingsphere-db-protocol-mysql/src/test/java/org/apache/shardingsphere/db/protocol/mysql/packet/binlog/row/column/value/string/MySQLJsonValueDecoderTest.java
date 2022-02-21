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

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string.MySQLJsonValueDecoder.JsonValueTypes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLJsonValueDecoderTest {
    
    private static final int SMALL_JSON_INT_LENGTH = 2;
    
    private static final int LARGE_JSON_INT_LENGTH = 4;
    
    private static final int SMALL_JSON_KEY_META_DATA_LENGTH = SMALL_JSON_INT_LENGTH + 2;
    
    private static final int SMALL_JSON_VALUE_META_DATA_LENGTH = SMALL_JSON_INT_LENGTH + 1;
    
    private static final int LARGE_JSON_KEY_META_DATA_LENGTH = LARGE_JSON_INT_LENGTH + 2;
    
    private static final int LARGE_JSON_VALUE_META_DATA_LENGTH = LARGE_JSON_INT_LENGTH + 1;
    
    @Test
    public void assertDecodeSmallJsonObjectWithLiteral() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.LITERAL, "key1", JsonValueTypes.LITERAL_NULL));
        jsonEntries.add(new JsonEntry(JsonValueTypes.LITERAL, "key2", JsonValueTypes.LITERAL_TRUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.LITERAL, "key3", JsonValueTypes.LITERAL_FALSE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":null,\"key2\":true,\"key3\":false}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithLiteral() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.LITERAL, "key1", JsonValueTypes.LITERAL_NULL));
        jsonEntries.add(new JsonEntry(JsonValueTypes.LITERAL, "key2", JsonValueTypes.LITERAL_TRUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.LITERAL, "key3", JsonValueTypes.LITERAL_FALSE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":null,\"key2\":true,\"key3\":false}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":-32768}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":-32768}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithUInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":32768}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithUInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":32768}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":-2147483648}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":-2147483648}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithUInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":2147483648}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithUInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":2147483648}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithInt64() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT64, "key1", Long.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT64, "key2", Long.MIN_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":9223372036854775807,\"key2\":-9223372036854775808}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithUInt64() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT64, "key1", Long.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT64, "key2", Long.MIN_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":9223372036854775807,\"key2\":9223372036854775808}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithDouble() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.DOUBLE, "key1", Double.MAX_VALUE));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":1.7976931348623157E308}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithString() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        String value1 = "";
        String value2 = Strings.repeat("1", (int) (Math.pow(2, 7) - 1));
        String value3 = Strings.repeat("1", (int) (Math.pow(2, 7) - 1 + 1));
        String value4 = Strings.repeat("1", (int) (Math.pow(2, 14) - 1));
        jsonEntries.add(new JsonEntry(JsonValueTypes.STRING, "key1", value1));
        jsonEntries.add(new JsonEntry(JsonValueTypes.STRING, "key2", value2));
        jsonEntries.add(new JsonEntry(JsonValueTypes.STRING, "key3", value3));
        jsonEntries.add(new JsonEntry(JsonValueTypes.STRING, "key4", value4));
        ByteBuf payload = mockJsonObjectByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is(String.format("{\"key1\":\"%s\",\"key2\":\"%s\",\"key3\":\"%s\",\"key4\":\"%s\"}", value1, value2, value3, value4)));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithSubJson() {
        List<JsonEntry> subJsons = Collections.singletonList(new JsonEntry(JsonValueTypes.INT32, "key1", 111));
        ByteBuf payload = mockJsonObjectByteBuf(Collections.singletonList(new JsonEntry(JsonValueTypes.SMALL_JSON_OBJECT, "subJson", subJsons)), true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"subJson\":{\"key1\":111}}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithSubJson() {
        List<JsonEntry> subJsons = Collections.singletonList(new JsonEntry(JsonValueTypes.INT32, "key1", 111));
        ByteBuf payload = mockJsonObjectByteBuf(Collections.singletonList(new JsonEntry(JsonValueTypes.SMALL_JSON_OBJECT, "subJson", subJsons)), false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"subJson\":{\"key1\":111}}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithSubArray() {
        List<JsonEntry> subArrays = Collections.singletonList(new JsonEntry(JsonValueTypes.INT32, null, 111));
        ByteBuf payload = mockJsonObjectByteBuf(Collections.singletonList(new JsonEntry(JsonValueTypes.SMALL_JSON_ARRAY, "subJson", subArrays)), true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"subJson\":[111]}"));
    }
    
    @Test
    public void assertDecodeSmallJsonArray() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, null, 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, null, 0x00008000));
        ByteBuf payload = mockJsonArrayByteBuf(jsonEntries, true);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("[32767,-32768]"));
    }
    
    @Test
    public void assertDecodeLargeJsonArray() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, null, 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, null, 0x00008000));
        ByteBuf payload = mockJsonArrayByteBuf(jsonEntries, false);
        String actual = (String) MySQLJsonValueDecoder.decode(payload);
        assertThat(actual, is("[32767,-32768]"));
    }
    
    private ByteBuf mockJsonObjectByteBuf(final List<JsonEntry> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(isSmall ? JsonValueTypes.SMALL_JSON_OBJECT : JsonValueTypes.LARGE_JSON_OBJECT);
        result.writeBytes(mockJsonObjectByteBufValue(jsonEntries, isSmall));
        return result;
    }
    
    private ByteBuf mockJsonObjectByteBufValue(final List<JsonEntry> jsonEntries, final boolean isSmall) {
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
    
    private ByteBuf mockJsonArrayByteBuf(final List<JsonEntry> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(isSmall ? JsonValueTypes.SMALL_JSON_ARRAY : JsonValueTypes.LARGE_JSON_ARRAY);
        result.writeBytes(mockJsonArrayByteBufValue(jsonEntries, isSmall));
        return result;
    }
    
    private ByteBuf mockJsonArrayByteBufValue(final List<JsonEntry> jsonEntries, final boolean isSmall) {
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
    
    private ByteBuf writeKeys(final ByteBuf jsonByteBuf, final List<JsonEntry> jsonEntries, final int startOffset, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        for (JsonEntry each : jsonEntries) {
            writeInt(jsonByteBuf, startOffset + result.readableBytes(), isSmall);
            byte[] keyBytes = each.getKey().getBytes();
            jsonByteBuf.writeShortLE(keyBytes.length);
            result.writeBytes(keyBytes);
        }
        return result;
    }
    
    private ByteBuf writeValues(final ByteBuf jsonByteBuf, final List<JsonEntry> jsonEntries, final int startOffset, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        for (JsonEntry each : jsonEntries) {
            jsonByteBuf.writeByte(each.getType());
            int offsetOrInlineValue = getOffsetOrInlineValue(startOffset, result.readableBytes(), each, isSmall);
            writeInt(jsonByteBuf, offsetOrInlineValue, isSmall);
            writeValueToByteBuf(each, result, isSmall);
        }
        return result;
    }
    
    private int getOffsetOrInlineValue(final int startOffset, final int readableBytes, final JsonEntry jsonEntry, final boolean isSmall) {
        switch (jsonEntry.getType()) {
            case JsonValueTypes.INT16:
            case JsonValueTypes.UINT16:
                return (int) jsonEntry.getValue();
            case JsonValueTypes.LITERAL:
                return (byte) jsonEntry.getValue();
            case JsonValueTypes.INT32:
            case JsonValueTypes.UINT32:
                return isSmall ? startOffset + readableBytes : (int) jsonEntry.getValue();
            default:
                return startOffset + readableBytes;
        }
    }
    
    private void writeValueToByteBuf(final JsonEntry jsonEntry, final ByteBuf byteBuf, final boolean isSmall) {
        switch (jsonEntry.getType()) {
            case JsonValueTypes.SMALL_JSON_OBJECT:
                byteBuf.writeBytes(mockJsonObjectByteBufValue((List<JsonEntry>) jsonEntry.getValue(), true));
                break;
            case JsonValueTypes.LARGE_JSON_OBJECT:
                byteBuf.writeBytes(mockJsonObjectByteBufValue((List<JsonEntry>) jsonEntry.getValue(), false));
                break;
            case JsonValueTypes.SMALL_JSON_ARRAY:
                byteBuf.writeBytes(mockJsonArrayByteBufValue((List<JsonEntry>) jsonEntry.getValue(), true));
                break;
            case JsonValueTypes.LARGE_JSON_ARRAY:
                byteBuf.writeBytes(mockJsonArrayByteBufValue((List<JsonEntry>) jsonEntry.getValue(), false));
                break;
            case JsonValueTypes.INT32:
            case JsonValueTypes.UINT32:
                if (isSmall) {
                    byteBuf.writeIntLE((int) jsonEntry.getValue());
                }
                break;
            case JsonValueTypes.INT64:
            case JsonValueTypes.UINT64:
                byteBuf.writeLongLE((long) jsonEntry.getValue());
                break;
            case JsonValueTypes.DOUBLE:
                byteBuf.writeDoubleLE((double) jsonEntry.getValue());
                break;
            case JsonValueTypes.STRING:
                writeString(byteBuf, (String) jsonEntry.getValue());
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
        byte[] lengthData = new byte[32 / 7 + (32 % 7 == 0 ? 0 : 1)];
        for (int i = 0; i < lengthData.length; i++) {
            lengthData[i] = (byte) ((length >> (7 * i)) & 0x7f);
        }
        // compress
        int index = lengthData.length - 1;
        for (; index > 0; index--) {
            if (0 != lengthData[index]) {
                break;
            }
        }
        for (int i = 0; i < index; i++) {
            lengthData[i] |= 0x80;
        }
        byte[] result = new byte[index + 1];
        System.arraycopy(lengthData, 0, result, 0, index + 1);
        return result;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class JsonEntry {
        
        private final byte type;
        
        private final String key;
        
        private final Object value;
    }
}
