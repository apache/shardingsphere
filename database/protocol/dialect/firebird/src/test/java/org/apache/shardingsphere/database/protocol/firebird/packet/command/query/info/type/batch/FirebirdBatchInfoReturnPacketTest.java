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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
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
    
    @Test
    void assertWriteDataAndBlobsSize() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(7, Collections.emptyList(), 1024L, false, false, true);
        batchStatement.addSize(32L);
        batchStatement.appendBlobStream(Unpooled.buffer().writeLong(1L).writeInt(0).writeInt(0), 16L);
        List<FirebirdInfoPacketType> infoItems = Arrays.asList(FirebirdBatchInfoPacketType.DATA_BYTES_SIZE, FirebirdBatchInfoPacketType.BLOBS_BYTES_SIZE);
        assertThat(write(infoItems, batchStatement, 64), is("0b040020000000" + "0c040010000000" + "01"));
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
    
    private String write(final List<FirebirdInfoPacketType> infoItems, final FirebirdBatchStatement batchStatement, final int maxLength) {
        ByteBuf result = Unpooled.buffer();
        new FirebirdBatchInfoReturnPacket(infoItems, batchStatement, maxLength).write(new FirebirdPacketPayload(result, StandardCharsets.UTF_8));
        return ByteBufUtil.hexDump(result);
    }
}
