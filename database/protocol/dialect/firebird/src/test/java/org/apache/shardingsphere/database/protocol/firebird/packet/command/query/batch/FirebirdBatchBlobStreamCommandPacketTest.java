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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidClumpletStructureException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBatchBlobStreamCommandPacketTest {
    
    private static final byte[] SEGMENTED_BPB = {1, 3, 1, 0};
    
    @Test
    void assertConstructor() {
        FirebirdBatchBlobStreamCommandPacket actual = new FirebirdBatchBlobStreamCommandPacket(new FirebirdPacketPayload(createPacket(24L), StandardCharsets.UTF_8));
        assertThat(actual.getStatementHandle(), is(7));
        assertThat(actual.getLength(), is(24L));
        assertThat(actual.getData().readableBytes(), is(21));
    }
    
    @Test
    void assertGetLength() {
        ByteBuf packet = createPacket(24L);
        packet.writeInt(FirebirdCommandPacketType.BATCH_EXEC.getValue());
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        assertThat(FirebirdBatchBlobStreamCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8), blobStream), is(33));
        ByteBuf nextPacket = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_BLOB_STREAM.getValue()).writeInt(7).writeInt(16).writeLong(2L).writeInt(0).writeInt(0);
        assertThat(FirebirdBatchBlobStreamCommandPacket.getLength(new FirebirdPacketPayload(nextPacket, StandardCharsets.UTF_8), blobStream), is(28));
    }
    
    @Test
    void assertGetLengthWithIncompletePacket() {
        ByteBuf packet = createPacket(24L);
        ByteBuf incompletePacket = packet.slice(0, packet.readableBytes() - 1);
        FirebirdPacketPayload payload = new FirebirdPacketPayload(incompletePacket, StandardCharsets.UTF_8);
        assertThat(FirebirdBatchBlobStreamCommandPacket.getLength(payload, new FirebirdBatchBlobStream()), is(-1));
    }
    
    @Test
    void assertGetLengthWithIncompleteHeader() {
        ByteBuf incompletePacket = createPacket(24L).slice(0, 10);
        assertThat(FirebirdBatchBlobStreamCommandPacket.getLength(new FirebirdPacketPayload(incompletePacket, StandardCharsets.UTF_8), new FirebirdBatchBlobStream()), is(-1));
    }
    
    @Test
    void assertGetLengthWithIncompleteLargeBpb() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, Integer.MAX_VALUE, Integer.MAX_VALUE - 3);
        data.writeBytes(new byte[]{1, 3});
        assertThat(getLength(new FirebirdBatchBlobStream(), data, Integer.MAX_VALUE - 3L), is(-1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStreamArguments")
    void assertGetLengthWithInvalidStream(final String name, final ByteBuf data, final long length, final Class<? extends SQLDialectException> expectedException) {
        assertThrows(expectedException, () -> getLength(new FirebirdBatchBlobStream(), data, length));
    }
    
    private static Stream<Arguments> invalidStreamArguments() {
        return Stream.of(
                Arguments.of("unaligned_length", Unpooled.buffer(), 6L, DatabaseProtocolException.class),
                Arguments.of("wrong_bpb_version", createStreamWithBpb(4, new byte[]{2, 3, 1, 0}), 32L, InvalidBpbVersionException.class),
                Arguments.of("truncated_bpb_clumplet", createStreamWithBpb(4, new byte[]{1, 3, 4, 0}), 32L, InvalidClumpletStructureException.class),
                Arguments.of("segment_exceeds_blob", createStreamWithSegment(4, 3), 24L, DatabaseProtocolException.class));
    }
    
    private static ByteBuf createStreamWithBpb(final int blobLength, final byte[] bpb) {
        ByteBuf result = Unpooled.buffer();
        writeHeader(result, 1L, blobLength, bpb.length);
        result.writeBytes(bpb);
        result.writeBytes(new byte[16]);
        return result;
    }
    
    private static ByteBuf createStreamWithSegment(final int blobLength, final int segmentLength) {
        ByteBuf result = Unpooled.buffer();
        writeHeader(result, 1L, blobLength + SEGMENTED_BPB.length, SEGMENTED_BPB.length);
        result.writeBytes(SEGMENTED_BPB);
        result.writeInt(segmentLength);
        result.writeBytes(new byte[8]);
        return result;
    }
    
    private ByteBuf createPacket(final long length) {
        ByteBuf result = Unpooled.buffer();
        result.writeInt(FirebirdCommandPacketType.BATCH_BLOB_STREAM.getValue());
        result.writeInt(7);
        result.writeInt((int) length);
        result.writeLong(1L);
        result.writeInt(5);
        result.writeInt(0);
        result.writeBytes(ByteBufUtil.decodeHexDump("6162636465"));
        return result;
    }
    
    private static int getLength(final FirebirdBatchBlobStream blobStream, final ByteBuf data, final long length) {
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_BLOB_STREAM.getValue()).writeInt(1).writeInt((int) length).writeBytes(data);
        return FirebirdBatchBlobStreamCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8), blobStream);
    }
    
    private static void writeHeader(final ByteBuf data, final long batchBlobId, final int blobLength, final int bpbLength) {
        data.writeLong(batchBlobId);
        data.writeInt(blobLength);
        data.writeInt(bpbLength);
    }
}
