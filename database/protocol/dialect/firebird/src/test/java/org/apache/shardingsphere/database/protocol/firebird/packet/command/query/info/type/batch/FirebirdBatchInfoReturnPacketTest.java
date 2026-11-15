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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdBatchInfoReturnPacketTest {
    
    private static final List<FirebirdInfoPacketType> CLIENT_INFO_ITEMS = Arrays.asList(
            FirebirdBatchInfoPacketType.BLOB_ALIGNMENT, FirebirdBatchInfoPacketType.BUFFER_BYTES_SIZE, FirebirdBatchInfoPacketType.BLOB_HEADER, FirebirdCommonInfoPacketType.END);
    
    @Test
    void assertWrite() {
        assertThat(write(CLIENT_INFO_ITEMS, new FirebirdBatchStatement(7, Collections.emptyList(), 16L * 1024 * 1024), 64), is("0d040004000000" + "0a040000000001" + "0e040010000000" + "01"));
    }
    
    private String write(final List<FirebirdInfoPacketType> infoItems, final FirebirdBatchStatement batchStatement, final int maxLength) {
        ByteBuf result = Unpooled.buffer();
        new FirebirdBatchInfoReturnPacket(infoItems, batchStatement, maxLength).write(new FirebirdPacketPayload(result, StandardCharsets.UTF_8));
        return ByteBufUtil.hexDump(result);
    }
    
    @Test
    void assertWriteDataAndBlobsSize() {
        FirebirdBatchStatement batchStatement = createBatchStatement(1, field(BlrConstants.blr_short, 0));
        appendMessages(batchStatement, 1, "0000000000000007");
        batchStatement.appendBlobStream(Unpooled.buffer().writeLong(1L).writeInt(0).writeInt(0), 16L);
        List<FirebirdInfoPacketType> infoItems = Arrays.asList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE, FirebirdBatchInfoPacketType.BLOBS_BYTES_SIZE);
        assertThat(write(infoItems, batchStatement, 64), is("0b040004000000" + "0c040010000000" + "01"));
    }
    
    private FirebirdBatchStatement createBatchStatement(final int columnCount, final byte[] fields) {
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(columnCount * 2);
        blr.writeBytes(fields).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return new FirebirdBatchStatement(7, FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields(), 1024L);
    }
    
    private static byte[] field(final int type, final int... attributes) {
        ByteBuf result = Unpooled.buffer().writeByte(type);
        for (int each : attributes) {
            result.writeByte(each);
        }
        result.writeByte(BlrConstants.blr_short).writeByte(0);
        return ByteBufUtil.getBytes(result);
    }
    
    private void appendMessages(final FirebirdBatchStatement batchStatement, final int messageCount, final String messages) {
        ByteBuf buffer = Unpooled.buffer().writeInt(0).writeInt(7).writeInt(messageCount).writeBytes(ByteBufUtil.decodeHexDump(messages));
        FirebirdBatchMessageCommandPacket packet = new FirebirdBatchMessageCommandPacket(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
        for (List<Object> each : packet.readParameterValues(batchStatement.getColumnDescriptors())) {
            batchStatement.addParameterValues(each);
        }
        batchStatement.addSize(packet.getDataLength());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nativeMessageArguments")
    void assertWriteDataBytesSize(final String name, final byte[] fields, final String message, final int expectedSize) {
        FirebirdBatchStatement batchStatement = createBatchStatement(1, fields);
        appendMessages(batchStatement, 1, message);
        ByteBuf actual = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(write(Collections.singletonList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE), batchStatement, 64)));
        assertThat((int) actual.readUnsignedByte(), is(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE.getCode()));
        assertThat(actual.readUnsignedShortLE(), is(4));
        assertThat(actual.readIntLE(), is(expectedSize));
        assertThat((int) actual.readUnsignedByte(), is(FirebirdCommonInfoPacketType.END.getCode()));
        assertThat(actual.readableBytes(), is(0));
    }
    
    private static Stream<Arguments> nativeMessageArguments() {
        return Stream.of(
                Arguments.of("smallint", field(BlrConstants.blr_short, 0), "0000000000000007", 4),
                Arguments.of("null_smallint", field(BlrConstants.blr_short, 0), "01000000", 4),
                Arguments.of("integer", field(BlrConstants.blr_long, 0), "0000000000000007", 8),
                Arguments.of("null_integer", field(BlrConstants.blr_long, 0), "01000000", 8),
                Arguments.of("bigint", field(BlrConstants.blr_int64, 0), "000000000000000000000007", 16),
                Arguments.of("int128", field(BlrConstants.blr_int128, 0), "0000000000000000000000000000000000000007", 24),
                Arguments.of("float", field(BlrConstants.blr_float), "000000003f800000", 8),
                Arguments.of("double", field(BlrConstants.blr_double), "000000003ff0000000000000", 16),
                Arguments.of("d_float", field(BlrConstants.blr_d_float), "000000003ff0000000000000", 16),
                Arguments.of("date", field(BlrConstants.blr_sql_date), "0000000000000000", 8),
                Arguments.of("time", field(BlrConstants.blr_sql_time), "0000000000000000", 8),
                Arguments.of("timestamp", field(BlrConstants.blr_timestamp), "000000000000000000000000", 12),
                Arguments.of("boolean", field(BlrConstants.blr_bool), "0000000001000000", 4),
                Arguments.of("legacy_text", field(BlrConstants.blr_text, 3, 0), "00000000666f6f00", 6),
                Arguments.of("text", field(BlrConstants.blr_text2, 4, 0, 3, 0), "00000000666f6f00", 6),
                Arguments.of("legacy_varying", field(BlrConstants.blr_varying, 5, 0), "0000000000000003666f6f00", 10),
                Arguments.of("varying", field(BlrConstants.blr_varying2, 4, 0, 5, 0), "0000000000000003666f6f00", 10),
                Arguments.of("empty_varying", field(BlrConstants.blr_varying2, 4, 0, 5, 0), "0000000000000000", 10),
                Arguments.of("null_varying", field(BlrConstants.blr_varying2, 4, 0, 5, 0), "01000000", 10),
                Arguments.of("quad", field(BlrConstants.blr_quad, 0), "000000000000000000000007", 12),
                Arguments.of("blob", field(BlrConstants.blr_blob2, 0, 0, 0, 0), "000000000000000000000007", 12));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mixedMessageArguments")
    void assertWriteDataBytesSizeWithMixedFields(final String name, final byte[] fields, final String message, final int expectedSize) {
        FirebirdBatchStatement batchStatement = createBatchStatement(2, fields);
        appendMessages(batchStatement, 1, message);
        ByteBuf actual = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(write(Collections.singletonList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE), batchStatement, 64)));
        assertThat(actual.getIntLE(3), is(expectedSize));
    }
    
    private static Stream<Arguments> mixedMessageArguments() {
        return Stream.of(
                Arguments.of("smallint_bigint", fields(field(BlrConstants.blr_short, 0), field(BlrConstants.blr_int64, 0)), "00000000000000070000000000000007", 24),
                Arguments.of("bigint_smallint", fields(field(BlrConstants.blr_int64, 0), field(BlrConstants.blr_short, 0)), "00000000000000000000000700000007", 16),
                Arguments.of("smallint_int128", fields(field(BlrConstants.blr_short, 0), field(BlrConstants.blr_int128, 0)), "000000000000000700000000000000000000000000000007", 32),
                Arguments.of("null_smallint_int128", fields(field(BlrConstants.blr_short, 0), field(BlrConstants.blr_int128, 0)), "03000000", 32));
    }
    
    private static byte[] fields(final byte[] first, final byte[] second) {
        return ByteBufUtil.getBytes(Unpooled.buffer().writeBytes(first).writeBytes(second));
    }
    
    @Test
    void assertWriteDataBytesSizeWithMultipleMessages() {
        FirebirdBatchStatement batchStatement = createBatchStatement(1, field(BlrConstants.blr_varying, 5, 0));
        appendMessages(batchStatement, 2, "0000000000000003666f6f0001000000");
        appendMessages(batchStatement, 1, "000000000000000362617200");
        assertThat(write(Collections.singletonList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE), batchStatement, 64), is("0b04001e00000001"));
        assertThat(batchStatement.getAccumulatedSize(), is(28L));
    }
    
    @Test
    void assertWriteDataBytesSizeWithEmptyBatch() {
        FirebirdBatchStatement batchStatement = createBatchStatement(1, field(BlrConstants.blr_int64, 0));
        assertThat(write(Collections.singletonList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE), batchStatement, 64), is("0b04000000000001"));
    }
    
    @Test
    void assertWriteDataBytesSizeAfterReset() {
        FirebirdBatchStatement batchStatement = createBatchStatement(1, field(BlrConstants.blr_int64, 0));
        appendMessages(batchStatement, 1, "000000000000000000000007");
        batchStatement.reset();
        assertThat(write(Collections.singletonList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE), batchStatement, 64), is("0b04000000000001"));
    }
    
    @Test
    void assertWriteDataBytesSizeAfterReuse() {
        FirebirdBatchStatement batchStatement = createBatchStatement(1, field(BlrConstants.blr_int64, 0));
        appendMessages(batchStatement, 1, "000000000000000000000007");
        batchStatement.reset();
        appendMessages(batchStatement, 2, "0100000001000000");
        assertThat(write(Collections.singletonList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE), batchStatement, 64), is("0b04002000000001"));
    }
    
    @Test
    void assertWriteBlobsSizeWithoutBlobData() {
        assertThat(write(Collections.singletonList(FirebirdBatchInfoPacketType.BLOBS_BYTES_SIZE), new FirebirdBatchStatement(7), 64), is("01"));
    }
    
    @Test
    void assertWriteWithRequestedLength() {
        List<FirebirdInfoPacketType> infoItems = Arrays.asList(FirebirdCommonInfoPacketType.LENGTH, FirebirdBatchInfoPacketType.BLOB_ALIGNMENT);
        assertThat(write(infoItems, new FirebirdBatchStatement(7), 64), is("0d040004000000" + "01"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("limitedBufferArguments")
    void assertWriteWithLimitedBuffer(final String name, final int maxLength, final String expectedData) {
        List<FirebirdInfoPacketType> infoItems = Arrays.asList(FirebirdBatchInfoPacketType.BLOB_ALIGNMENT, FirebirdBatchInfoPacketType.BLOB_HEADER);
        assertThat(write(infoItems, new FirebirdBatchStatement(7), maxLength), is(expectedData));
    }
    
    private static Stream<Arguments> limitedBufferArguments() {
        return Stream.of(
                Arguments.of("empty_buffer", 0, ""),
                Arguments.of("one_byte_buffer", 1, "02"),
                Arguments.of("two_bytes_buffer", 2, "0201"),
                Arguments.of("truncated_before_first_item", 7, "0201" + "0000000000"),
                Arguments.of("truncated_after_first_item", 10, "0d040004000000" + "0201" + "00"),
                Arguments.of("truncated_without_end", 8, "0d040004000000" + "02"),
                Arguments.of("end_in_reserved_byte", 15, "0d040004000000" + "0e040010000000" + "01"),
                Arguments.of("without_room_for_length", 22, "0d040004000000" + "0e040010000000" + "01" + "00000000000000"),
                Arguments.of("exact_room_for_length", 23, "0d040004000000" + "0e040010000000" + "01"));
    }
    
    @Test
    void assertWriteWithUnknownInfo() {
        List<FirebirdInfoPacketType> infoItems = Arrays.asList(FirebirdBatchInfoPacketType.BUFFER_BYTES_SIZE, FirebirdCommonInfoPacketType.ERROR, FirebirdBatchInfoPacketType.BLOB_ALIGNMENT);
        assertThat(write(infoItems, new FirebirdBatchStatement(7, Collections.emptyList(), 16L * 1024 * 1024), 256), is("0a040000000001" + "03040015000014" + "0d040004000000" + "01"));
    }
    
    @Test
    void assertWriteWithRepeatedInfo() {
        List<FirebirdInfoPacketType> infoItems = Arrays.asList(
                FirebirdCommonInfoPacketType.ERROR,
                FirebirdCommonInfoPacketType.ERROR,
                FirebirdBatchInfoPacketType.BLOB_ALIGNMENT,
                FirebirdBatchInfoPacketType.BLOB_ALIGNMENT);
        assertThat(write(infoItems, new FirebirdBatchStatement(7), 64), is("03040015000014" + "03040015000014" + "0d040004000000" + "0d040004000000" + "01"));
    }
    
}
