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
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobBufferFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBatchStatementTest {
    
    @Test
    void assertAddParameterValues() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.addParameterValues(Arrays.asList("foo", 1L));
        assertThat(batchStatement.getStatementHandle(), is(32));
        assertFalse(batchStatement.isRecordCounts());
        assertFalse(batchStatement.isMultiError());
        assertThat(batchStatement.getParameterValues().size(), is(1));
        assertThat(batchStatement.getParameterValues().get(0), is(Arrays.asList("foo", 1L)));
    }
    
    @Test
    void assertCreateWithRecordCounts() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32, Collections.emptyList(), 1024L, true, false, false);
        assertTrue(batchStatement.isRecordCounts());
        assertFalse(batchStatement.isMultiError());
        assertThat(batchStatement.getBufferSize(), is(1024L));
    }
    
    @Test
    void assertCreateWithMultiError() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32, Collections.emptyList(), 1024L, false, true, true);
        assertTrue(batchStatement.isMultiError());
        assertFalse(batchStatement.isRecordCounts());
        assertTrue(batchStatement.isBlobStreamAllowed());
    }
    
    @Test
    void assertHasBatchBlobIdColumn() {
        FirebirdBatchColumnDescriptor batchBlobIdColumn = new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0);
        batchBlobIdColumn.setBatchBlobId(true);
        assertTrue(new FirebirdBatchStatement(32, Collections.singletonList(batchBlobIdColumn), 1024L).hasBatchBlobIdColumn());
    }
    
    @Test
    void assertHasBatchBlobIdColumnWithQuadBlob() {
        FirebirdBatchColumnDescriptor quadBlobColumn = new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0);
        assertFalse(new FirebirdBatchStatement(32, Collections.singletonList(quadBlobColumn), 1024L).hasBatchBlobIdColumn());
    }
    
    @Test
    void assertAppendBlobStream() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.appendBlobStream(createBlobHeader(1L, 2, 0).writeBytes(new byte[]{1, 2}), 20L);
        assertThat(batchStatement.getBlobStreamSize(), is(20L));
        assertThat(batchStatement.getBlobBuffer().iterator().next(), is(new byte[]{0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0}));
    }
    
    @Test
    void assertReadStreamBlobs() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        ByteBuf data = createBlobHeader(1L, 2, 0).writeBytes(new byte[]{1, 2});
        data.writeBytes(createBlobHeader(0L, 1, 0)).writeByte(3);
        data.writeBytes(createBlobHeader(2L, 1, 0)).writeByte(4);
        batchStatement.appendBlobStream(data, 60L);
        List<FirebirdBatchStreamBlob> actual = new ArrayList<>(batchStatement.readStreamBlobs());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getBatchBlobId(), is(1L));
        assertThat(actual.get(0).getBpb(), is(new byte[]{1, 3, 1, 1}));
        assertFalse(actual.get(0).isSegmented());
        assertThat(actual.get(0).getData().toByteArray(), is(new byte[]{1, 2, 3}));
        assertThat(actual.get(1).getBatchBlobId(), is(2L));
        assertThat(actual.get(1).getData().toByteArray(), is(new byte[]{4}));
    }
    
    @Test
    void assertReadSegmentedStreamBlobContinuation() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        ByteBuf data = createBlobHeader(1L, 7, 4).writeBytes(new byte[]{1, 3, 1, 0}).writeInt(1).writeByte(1);
        data.writeBytes(createBlobHeader(0L, 9, 0)).writeBytes(new byte[]{3, 0, 2, 3, 4, 0, 1, 0, 5});
        batchStatement.appendBlobStream(data, 52L);
        FirebirdBatchStreamBlob actual = batchStatement.readStreamBlobs().iterator().next();
        assertTrue(actual.isSegmented());
        assertThat(actual.getData().toByteArray(), is(new byte[]{1, 2, 3, 4, 5}));
    }
    
    @Test
    void assertReadStreamBlobWithSegmentedContinuation() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.getBlobStream().setDefaultBpb(Unpooled.wrappedBuffer(new byte[]{1, 3, 1, 0}));
        ByteBuf data = createBlobHeader(1L, 6, 4).writeBytes(new byte[]{1, 3, 1, 1}).writeBytes(new byte[]{1, 2});
        data.writeBytes(createBlobHeader(0L, 4, 0)).writeInt(2).writeBytes(new byte[]{(byte) 0xAA, (byte) 0xBB});
        batchStatement.appendBlobStream(data, 44L);
        assertThat(batchStatement.readStreamBlobs().iterator().next().getData().toByteArray(), is(new byte[]{1, 2, 2, 0, (byte) 0xAA, (byte) 0xBB}));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("resetInsideBlobArguments")
    void assertReadStreamBlobsAfterResetInsideBlob(final String name, final int resetBlobLength, final ByteBuf data) {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.appendBlobStream(createBlobHeader(1L, resetBlobLength, 0).writeZero(16), 32L);
        batchStatement.reset();
        batchStatement.appendBlobStream(data, 20L);
        List<FirebirdBatchStreamBlob> actual = new ArrayList<>(batchStatement.readStreamBlobs());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getBatchBlobId(), is(2L));
        assertThat(actual.get(0).getData().toByteArray(), is(new byte[]{1, 2, 3}));
    }
    
    private static Stream<Arguments> resetInsideBlobArguments() {
        return Stream.of(
                Arguments.of("reset_blob_rest_longer_than_portion", 100, Unpooled.buffer().writeIntLE(0).writeIntLE(2).writeIntLE(3).writeIntLE(0).writeBytes(new byte[]{1, 2, 3, 0})),
                Arguments.of("reset_blob_rest_shorter_than_portion", 20, Unpooled.buffer().writeIntLE(0).writeInt(2).writeInt(3).writeInt(0).writeBytes(new byte[]{0, 3, 2, 1})));
    }
    
    @Test
    void assertReadStreamBlobWithBpbLongerThanBlob() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.appendBlobStream(createBlobHeader(1L, 2, 4).writeBytes(new byte[]{1, 3, 1, 1}).writeBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}), 32L);
        assertThat(batchStatement.readStreamBlobs().iterator().next().getData().toByteArray(), is(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}));
    }
    
    @Test
    void assertReadStreamBlobsWithIncompleteHeader() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.appendBlobStream(createBlobHeader(1L, 20, 0).writeZero(16), 32L);
        batchStatement.reset();
        batchStatement.appendBlobStream(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4}), 4L);
        assertThat(assertThrows(BatchSmallDataException.class, batchStatement::readStreamBlobs).getBufferName(), is("BLOB"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStreamBlobArguments")
    void assertReadInvalidStreamBlobs(final String name, final ByteBuf data, final long length, final Class<? extends RuntimeException> expectedException) {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.getBlobIds().put(3L, 30L);
        batchStatement.appendBlobStream(data, length);
        assertThrows(expectedException, batchStatement::readStreamBlobs);
    }
    
    private static Stream<Arguments> invalidStreamBlobArguments() {
        return Stream.of(
                Arguments.of("continuation_with_bpb", createBlobHeader(0L, 4, 4).writeBytes(new byte[]{1, 3, 1, 1}), 20L, BatchBlobContinuationBpbException.class),
                Arguments.of("continuation_without_blob", createBlobHeader(0L, 2, 0).writeBytes(new byte[]{1, 2}), 20L, BatchBlobBufferFormatException.class),
                Arguments.of("incomplete_bpb", createBlobHeader(1L, 12, 8).writeBytes(new byte[]{1, 3, 1, 0}), 20L, BatchBpbTooBigException.class),
                Arguments.of("segment_exceeds_blob", createBlobHeader(1L, 4, 4).writeBytes(new byte[]{1, 3, 1, 0}).writeBytes(createBlobHeader(0L, 4, 0)).writeBytes(new byte[]{9, 0, 1, 2}), 40L,
                        BatchSegmentExceedsBlobException.class),
                Arguments.of("incomplete_segment", createBlobHeader(1L, 12, 4).writeBytes(new byte[]{1, 3, 1, 0}).writeInt(6).writeBytes(new byte[]{1, 2}), 24L, BatchSegmentTooBigException.class),
                Arguments.of("registered_blob_id", createBlobHeader(3L, 0, 0), 16L, RepeatedBatchBlobIdException.class),
                Arguments.of("repeated_stream_blob_id", createBlobHeader(1L, 0, 0).writeBytes(createBlobHeader(1L, 0, 0)), 32L, RepeatedBatchBlobIdException.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("clearBlobStreamArguments")
    void assertClearBlobStream(final String name, final int blobLength, final long expectedBlobStreamSize) {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(32);
        batchStatement.appendBlobStream(createBlobHeader(1L, blobLength, 0).writeZero(blobLength), 16L + blobLength);
        batchStatement.getStreamBlobContents().put(2L, new byte[]{1});
        batchStatement.clearBlobStream();
        assertTrue(batchStatement.getBlobBuffer().isEmpty());
        assertThat(batchStatement.getBlobStreamSize(), is(expectedBlobStreamSize));
        assertThat(batchStatement.getStreamBlobContents().size(), is(1));
    }
    
    private static Stream<Arguments> clearBlobStreamArguments() {
        return Stream.of(Arguments.of("empty_blob", 0, 0L), Arguments.of("memory_cache_limit", 131056, 0L), Arguments.of("temporary_file", 131064, 131080L));
    }
    
    @Test
    void assertReset() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(100);
        batchStatement.addParameterValues(Collections.singletonList("foo"));
        batchStatement.addSize(10L);
        batchStatement.getBlobIds().put(1L, 2L);
        batchStatement.getStreamBlobContents().put(3L, new byte[]{1});
        batchStatement.appendBlobStream(createBlobHeader(4L, 0, 0), 16L);
        FirebirdBatchStatement otherBatchStatement = new FirebirdBatchStatement(101);
        otherBatchStatement.getBlobIds().put(1L, 3L);
        batchStatement.reset();
        assertTrue(batchStatement.getParameterValues().isEmpty());
        assertThat(batchStatement.getAccumulatedSize(), is(0L));
        assertTrue(batchStatement.getBlobIds().isEmpty());
        assertTrue(batchStatement.getStreamBlobContents().isEmpty());
        assertTrue(batchStatement.getBlobBuffer().isEmpty());
        assertThat(batchStatement.getBlobStreamSize(), is(0L));
        assertThat(otherBatchStatement.getBlobIds(), is(Collections.singletonMap(1L, 3L)));
    }
    
    private static ByteBuf createBlobHeader(final long batchBlobId, final int blobLength, final int bpbLength) {
        return Unpooled.buffer().writeLong(batchBlobId).writeInt(blobLength).writeInt(bpbLength);
    }
}
