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

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.JsonValueDecoder.JsonValueTypes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonValueDecoderTest {
    
    private static final int SMALL_JSON_INT_LENGTH = 2;
    
    private static final int LARGE_JSON_INT_LENGTH = 4;
    
    private static final int SMALL_JSON_KEY_META_DATA_LENGTH = SMALL_JSON_INT_LENGTH + 2;
    
    private static final int SMALL_JSON_VALUE_META_DATA_LENGTH = SMALL_JSON_INT_LENGTH + 1;
    
    private static final int LARGE_JSON_KEY_META_DATA_LENGTH = LARGE_JSON_INT_LENGTH + 2;
    
    private static final int LARGE_JSON_VALUE_META_DATA_LENGTH = LARGE_JSON_INT_LENGTH + 1;
    
    @Test
    public void assertDecodeSmallJsonObjectWithInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, true);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":-32768}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, false);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":-32768}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithUInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, true);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":32768}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithUInt16() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key1", 0x00007fff));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT16, "key2", 0x00008000));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, false);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":32767,\"key2\":32768}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, true);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":-2147483648}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.INT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, false);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":-2147483648}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithUInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, true);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":2147483648}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithUInt32() {
        List<JsonEntry> jsonEntries = new LinkedList<>();
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key1", Integer.MAX_VALUE));
        jsonEntries.add(new JsonEntry(JsonValueTypes.UINT32, "key2", Integer.MIN_VALUE));
        ByteBuf payload = mockJsonByteBuf(jsonEntries, false);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":2147483647,\"key2\":2147483648}"));
    }
    
    @Test
    public void assertDecodeSmallJsonObjectWithSubJson() {
        List<JsonEntry> subJsons = Collections.singletonList(new JsonEntry(JsonValueTypes.INT32, "key1", 111));
        ByteBuf payload = mockJsonByteBuf(Collections.singletonList(new JsonEntry(JsonValueTypes.SMALL_JSON_OBJECT, "subJson", subJsons)), true);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"subJson\":{\"key1\":111}}"));
    }
    
    @Test
    public void assertDecodeLargeJsonObjectWithSubJson() {
        List<JsonEntry> subJsons = Collections.singletonList(new JsonEntry(JsonValueTypes.INT32, "key1", 111));
        ByteBuf payload = mockJsonByteBuf(Collections.singletonList(new JsonEntry(JsonValueTypes.SMALL_JSON_OBJECT, "subJson", subJsons)), false);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"subJson\":{\"key1\":111}}"));
    }
    
    private ByteBuf mockJsonByteBuf(final List<JsonEntry> jsonEntries, final boolean isSmall) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(isSmall ? JsonValueTypes.SMALL_JSON_OBJECT : JsonValueTypes.LARGE_JSON_OBJECT);
        result.writeBytes(mockJsonByteBufValue(jsonEntries, isSmall));
        return result;
    }
    
    private ByteBuf mockJsonByteBufValue(final List<JsonEntry> jsonEntries, final boolean isSmall) {
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
            int offsetOrInlinedValue = getOffsetOrInlineValue(startOffset, result.readableBytes(), each, isSmall);
            writeInt(jsonByteBuf, offsetOrInlinedValue, isSmall);
            writeValueToByteBuf(each, result, isSmall);
        }
        return result;
    }
    
    private int getOffsetOrInlineValue(final int startOffset, final int readableBytes, final JsonEntry jsonEntry, final boolean isSmall) {
        switch (jsonEntry.getType()) {
            case JsonValueTypes.INT16:
            case JsonValueTypes.UINT16:
            case JsonValueTypes.LITERAL:
                return (int) jsonEntry.getValue();
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
                byteBuf.writeBytes(mockJsonByteBufValue((List<JsonEntry>) jsonEntry.getValue(), true));
                break;
            case JsonValueTypes.LARGE_JSON_OBJECT:
                byteBuf.writeBytes(mockJsonByteBufValue((List<JsonEntry>) jsonEntry.getValue(), false));
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
                byteBuf.writeBytes(((String) jsonEntry.getValue()).getBytes());
                break;
            default:
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private class JsonEntry {
        
        private final byte type;
        
        private final String key;
        
        private final Object value;
    }
}
