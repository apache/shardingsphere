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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBatchBlobStreamTest {
    
    private static final byte[] SEGMENTED_BPB = {1, 3, 1, 0};
    
    @Test
    void assertReadStreamBlob() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 0x100000002L, 5, 0);
        data.writeBytes("abcde".getBytes(StandardCharsets.US_ASCII));
        byte[] actual = new FirebirdBatchBlobStream().read(data, 24L);
        ByteBuf expected = Unpooled.buffer().writeIntLE(1).writeIntLE(2).writeIntLE(5).writeIntLE(0).writeBytes("abcde".getBytes(StandardCharsets.US_ASCII)).writeZero(3);
        assertThat(actual, is(ByteBufUtil.getBytes(expected)));
        assertThat(data.readerIndex(), is(21));
    }
    
    @Test
    void assertReadSegmentedBlobWithBpb() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 10L, 14, SEGMENTED_BPB.length);
        data.writeBytes(SEGMENTED_BPB);
        data.writeInt(3);
        data.writeBytes("abc".getBytes(StandardCharsets.US_ASCII));
        data.writeInt(2);
        data.writeBytes("de".getBytes(StandardCharsets.US_ASCII));
        byte[] actual = new FirebirdBatchBlobStream().read(data, 32L);
        ByteBuf expected = createRestoredHeader(10, 14, SEGMENTED_BPB.length).writeBytes(SEGMENTED_BPB).writeShortLE(3).writeBytes("abc".getBytes(StandardCharsets.US_ASCII)).writeZero(1)
                .writeShortLE(2).writeBytes("de".getBytes(StandardCharsets.US_ASCII)).writeZero(2);
        assertThat(actual, is(ByteBufUtil.getBytes(expected)));
        assertThat(data.readerIndex(), is(33));
    }
    
    @Test
    void assertReadBlobSplitAcrossPackets() {
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        ByteBuf firstData = Unpooled.buffer();
        writeHeader(firstData, 1L, 20, 0);
        firstData.writeBytes(new byte[16]);
        blobStream.read(firstData, 32L);
        ByteBuf secondData = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4});
        assertThat(blobStream.read(secondData, 4L), is(new byte[]{1, 2, 3, 4}));
        assertThat(secondData.readerIndex(), is(4));
    }
    
    @Test
    void assertReadWithHeaderSentInNextPacket() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 12, 0);
        data.writeBytes(new byte[12]);
        data.writeInt(1);
        byte[] actual = new FirebirdBatchBlobStream().read(data, 40L);
        assertThat(actual, is(ByteBufUtil.getBytes(createRestoredHeader(1, 12, 0).writeZero(12))));
        assertThat(data.readerIndex(), is(28));
    }
    
    @Test
    void assertReadWithDefaultSegmentedBpb() {
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        blobStream.setDefaultBpb(Unpooled.wrappedBuffer(SEGMENTED_BPB));
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 4, 0);
        data.writeInt(2);
        data.writeBytes(new byte[]{7, 8});
        byte[] actual = blobStream.read(data, 20L);
        assertThat(actual, is(ByteBufUtil.getBytes(createRestoredHeader(1, 4, 0).writeShortLE(2).writeBytes(new byte[]{7, 8}))));
        assertThat(data.readerIndex(), is(22));
    }
    
    @Test
    void assertReadWithDefaultStreamBpb() {
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        blobStream.setDefaultBpb(Unpooled.wrappedBuffer(new byte[]{1, 3, 4, 1, 0, 0, 0}));
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 4, 0);
        data.writeBytes(new byte[]{7, 8, 9, 10});
        byte[] actual = blobStream.read(data, 20L);
        assertThat(actual, is(ByteBufUtil.getBytes(createRestoredHeader(1, 4, 0).writeBytes(new byte[]{7, 8, 9, 10}))));
        assertThat(data.readerIndex(), is(20));
    }
    
    @Test
    void assertReadWithBpbLongerThanBlob() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 2, 4);
        data.writeBytes(new byte[]{1, 3, 1, 1});
        data.writeBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        byte[] actual = new FirebirdBatchBlobStream().read(data, 32L);
        assertThat(actual, is(ByteBufUtil.getBytes(createRestoredHeader(1, 2, 4).writeBytes(new byte[]{1, 3, 1, 1}).writeBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}))));
    }
    
    @Test
    void assertReadWithSegmentHeaderBeyondBlob() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 9, SEGMENTED_BPB.length);
        data.writeBytes(SEGMENTED_BPB);
        data.writeInt(2);
        data.writeBytes(new byte[]{1, 2});
        data.writeInt(2);
        data.writeBytes(new byte[]{3, 4});
        byte[] actual = new FirebirdBatchBlobStream().read(data, 28L);
        assertThat(actual, is(ByteBufUtil.getBytes(createRestoredHeader(1, 9, SEGMENTED_BPB.length).writeBytes(SEGMENTED_BPB).writeShortLE(2).writeBytes(new byte[]{1, 2}).writeShortLE(2)
                .writeBytes(new byte[]{3, 4}))));
    }
    
    @Test
    void assertReadWithIncompleteData() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 5, 0);
        data.writeBytes(new byte[2]);
        assertThrows(IndexOutOfBoundsException.class, () -> new FirebirdBatchBlobStream().read(data, 24L));
    }
    
    @Test
    void assertSkipWithIncompleteLargeBpb() {
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, Integer.MAX_VALUE, Integer.MAX_VALUE - 3);
        data.writeBytes(new byte[]{1, 3});
        assertThrows(IndexOutOfBoundsException.class, () -> new FirebirdBatchBlobStream().skip(data, Integer.MAX_VALUE - 3L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStreamArguments")
    void assertSkipWithInvalidStream(final String name, final ByteBuf data, final long length, final Class<? extends SQLDialectException> expectedException) {
        assertThrows(expectedException, () -> new FirebirdBatchBlobStream().skip(data, length));
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStructureBpbArguments")
    void assertSetDefaultBpbWithInvalidStructure(final String name, final byte[] bpb, final String expectedReason, final int expectedData) {
        InvalidClumpletStructureException actual = assertThrows(InvalidClumpletStructureException.class, () -> new FirebirdBatchBlobStream().setDefaultBpb(Unpooled.wrappedBuffer(bpb)));
        assertThat(actual.getReason(), is(expectedReason));
        assertThat(actual.getData(), is(expectedData));
    }
    
    private static Stream<Arguments> invalidStructureBpbArguments() {
        return Stream.of(
                Arguments.of("empty_bpb", new byte[0], "empty buffer", 0),
                Arguments.of("clumplet_without_length", new byte[]{1, 4}, "buffer end before end of clumplet - no length component", 1),
                Arguments.of("clumplet_too_long", new byte[]{1, 4, 9, 1}, "buffer end before end of clumplet - clumplet too long", 11),
                Arguments.of("type_clumplet_too_long", new byte[]{1, 3, 5, 1}, "buffer end before end of clumplet - clumplet too long", 7),
                Arguments.of("type_longer_than_integer", new byte[]{1, 3, 5, 1, 0, 0, 0, 0}, "length of integer exceeds 4 bytes", 5));
    }
    
    @Test
    void assertSetDefaultBpbWithWrongVersion() {
        InvalidBpbVersionException actual = assertThrows(InvalidBpbVersionException.class, () -> new FirebirdBatchBlobStream().setDefaultBpb(Unpooled.wrappedBuffer(new byte[]{2, 3, 1, 0})));
        assertThat(actual.getVersion(), is(2));
        assertThat(actual.getExpectedVersion(), is(1));
    }
    
    @Test
    void assertCopy() {
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 20, 0);
        data.writeBytes(new byte[16]);
        blobStream.copy().skip(data, 32L);
        ByteBuf nextData = Unpooled.buffer();
        writeHeader(nextData, 2L, 0, 0);
        assertThat(blobStream.read(nextData, 16L), is(ByteBufUtil.getBytes(createRestoredHeader(2, 0, 0))));
    }
    
    @Test
    void assertSkip() {
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 20, 0);
        data.writeBytes(new byte[16]);
        assertThat(blobStream.skip(data, 32L), is(32L));
        assertThat(blobStream.read(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4}), 4L), is(new byte[]{1, 2, 3, 4}));
    }
    
    private static void writeHeader(final ByteBuf data, final long batchBlobId, final int blobLength, final int bpbLength) {
        data.writeLong(batchBlobId);
        data.writeInt(blobLength);
        data.writeInt(bpbLength);
    }
    
    private static ByteBuf createRestoredHeader(final int batchBlobId, final int blobLength, final int bpbLength) {
        return Unpooled.buffer().writeIntLE(0).writeIntLE(batchBlobId).writeIntLE(blobLength).writeIntLE(bpbLength);
    }
}
