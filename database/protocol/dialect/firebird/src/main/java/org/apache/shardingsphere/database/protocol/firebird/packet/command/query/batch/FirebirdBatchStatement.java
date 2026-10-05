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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobBufferFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.firebirdsql.gds.ISCConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebird batch statement metadata.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdBatchStatement {
    
    private static final long BLOB_BUFFER_MEMORY_CACHE_SIZE = 128L * 1024L;
    
    private final int statementHandle;
    
    private final List<FirebirdBatchColumnDescriptor> columnDescriptors;
    
    private final long bufferSize;
    
    private final boolean recordCounts;
    
    private final boolean multiError;
    
    private final boolean blobStreamAllowed;
    
    private final List<List<Object>> parameterValues = new ArrayList<>();
    
    private long accumulatedSize;
    
    private final Map<Long, Long> blobIds = new HashMap<>();
    
    private final FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
    
    private final Collection<byte[]> blobBuffer = new ArrayList<>();
    
    private final Map<Long, byte[]> streamBlobContents = new HashMap<>();
    
    private long blobStreamSize;
    
    @Setter
    private byte[] defaultBpb = {ISCConstants.isc_bpb_version1, ISCConstants.isc_bpb_type, 1, ISCConstants.isc_bpb_type_stream};
    
    public FirebirdBatchStatement(final int statementHandle) {
        this(statementHandle, Collections.emptyList(), 0L, false, false, false);
    }
    
    public FirebirdBatchStatement(final int statementHandle, final List<FirebirdBatchColumnDescriptor> columnDescriptors, final long bufferSize) {
        this(statementHandle, columnDescriptors, bufferSize, false, false, false);
    }
    
    /**
     * Add batched parameter values.
     * @param values parameter values to add
     */
    public void addParameterValues(final List<Object> values) {
        parameterValues.add(values);
    }
    
    /**
     * Add accumulated batch message size in bytes.
     * @param size size in bytes to add
     */
    public void addSize(final long size) {
        accumulatedSize += size;
    }
    
    /**
     * Judge whether batch message contains BLOB fields that carry batch BLOB IDs.
     *
     * @return whether batch message contains BLOB fields that carry batch BLOB IDs
     */
    public boolean hasBatchBlobIdColumn() {
        for (FirebirdBatchColumnDescriptor each : columnDescriptors) {
            if (each.isBatchBlobId()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Append one portion of the BLOB stream to the BLOB buffer as Firebird restores it.
     *
     * @param data packet data starting at the stream portion
     * @param length declared length of the stream portion
     */
    public void appendBlobStream(final ByteBuf data, final long length) {
        byte[] portion = blobStream.read(data, length);
        blobBuffer.add(portion);
        blobStreamSize += portion.length;
    }
    
    /**
     * Read BLOBs from the BLOB buffer as Firebird does before executing batch messages.
     *
     * <p>The BLOB buffer is read from its start regardless of the BLOB stream state, which cancelling the batch keeps inside a cancelled BLOB.
     * A BLOB header with batch BLOB ID zero continues the previous BLOB in its segmentation mode.
     * Lengths are unsigned 32-bit numbers, so a BLOB parameter buffer longer than its BLOB makes the BLOB take the rest of the BLOB buffer.</p>
     *
     * @return BLOBs in the order of the BLOB buffer
     * @throws BatchSmallDataException if a BLOB header is incomplete
     * @throws BatchBlobBufferFormatException if a continuation has no BLOB to continue
     * @throws BatchBlobContinuationBpbException if a BLOB continuation contains a BLOB parameter buffer
     * @throws BatchBpbTooBigException if a BLOB parameter buffer is incomplete
     * @throws DatabaseProtocolException if a BLOB parameter buffer is invalid
     * @throws RepeatedBatchBlobIdException if batch BLOB ID is already registered
     * @throws BatchSegmentExceedsBlobException if a BLOB segment exceeds its BLOB
     * @throws BatchSegmentTooBigException if a BLOB segment is incomplete
     */
    public Collection<FirebirdBatchStreamBlob> readStreamBlobs() {
        ByteBuf buffer = Unpooled.wrappedBuffer(blobBuffer.toArray(new byte[0][]));
        Map<Long, FirebirdBatchStreamBlob> result = new LinkedHashMap<>();
        FirebirdBatchStreamBlob currentBlob = null;
        long blobRemaining = 0L;
        while (buffer.isReadable()) {
            if (0L != blobRemaining) {
                blobRemaining = Integer.toUnsignedLong((int) (blobRemaining - readBlobData(buffer, currentBlob, blobRemaining)));
                continue;
            }
            int padding = FirebirdBatchBlobStream.getPadding(buffer.readerIndex(), FirebirdBatchBlobStream.ALIGNMENT);
            if (padding > 0) {
                buffer.skipBytes(padding);
                continue;
            }
            ShardingSpherePreconditions.checkState(buffer.readableBytes() >= FirebirdBatchBlobStream.HEADER_LENGTH, () -> new BatchSmallDataException("BLOB"));
            long batchBlobId = buffer.readUnsignedIntLE() << 32 | buffer.readUnsignedIntLE();
            blobRemaining = buffer.readUnsignedIntLE();
            long bpbLength = buffer.readUnsignedIntLE();
            if (0L == batchBlobId) {
                ShardingSpherePreconditions.checkState(0L == bpbLength, () -> new BatchBlobContinuationBpbException(bpbLength));
                ShardingSpherePreconditions.checkNotNull(currentBlob, () -> new BatchBlobBufferFormatException(statementHandle));
                continue;
            }
            ShardingSpherePreconditions.checkState(bpbLength <= buffer.readableBytes(), () -> new BatchBpbTooBigException(bpbLength, buffer.readableBytes()));
            byte[] bpb = 0L == bpbLength ? defaultBpb : ByteBufUtil.getBytes(buffer.readSlice((int) bpbLength));
            blobRemaining = Integer.toUnsignedLong((int) (blobRemaining - bpbLength));
            currentBlob = new FirebirdBatchStreamBlob(batchBlobId, bpb, FirebirdBatchBlobStream.isSegmented(bpb));
            ShardingSpherePreconditions.checkState(!blobIds.containsKey(batchBlobId) && !streamBlobContents.containsKey(batchBlobId) && !result.containsKey(batchBlobId),
                    () -> new RepeatedBatchBlobIdException(batchBlobId));
            result.put(batchBlobId, currentBlob);
        }
        return result.values();
    }
    
    private int readBlobData(final ByteBuf buffer, final FirebirdBatchStreamBlob blob, final long blobRemaining) {
        if (!blob.isSegmented()) {
            return readBytes(buffer, blob, (int) Math.min(blobRemaining, buffer.readableBytes()));
        }
        int padding = FirebirdBatchBlobStream.getPadding(buffer.readerIndex(), FirebirdBatchBlobStream.SEGMENT_ALIGNMENT);
        if (padding > 0) {
            buffer.skipBytes(padding);
            return padding;
        }
        int segmentLength = buffer.readUnsignedShortLE();
        long segmentBlobRemaining = Integer.toUnsignedLong((int) blobRemaining - FirebirdBatchBlobStream.SEGMENT_LENGTH_SIZE);
        ShardingSpherePreconditions.checkState(segmentLength <= segmentBlobRemaining, () -> new BatchSegmentExceedsBlobException(segmentLength, (int) segmentBlobRemaining));
        ShardingSpherePreconditions.checkState(segmentLength <= buffer.readableBytes(), () -> new BatchSegmentTooBigException(segmentLength, buffer.readableBytes()));
        return FirebirdBatchBlobStream.SEGMENT_LENGTH_SIZE + readBytes(buffer, blob, segmentLength);
    }
    
    private int readBytes(final ByteBuf buffer, final FirebirdBatchStreamBlob blob, final int length) {
        blob.appendData(ByteBufUtil.getBytes(buffer.readSlice(length)));
        return length;
    }
    
    /**
     * Clear BLOB buffer after its BLOBs are read, which keeps the BLOB stream state.
     *
     * <p>Firebird moves a BLOB buffer that exceeds its memory cache to a temporary file and keeps counting it until the batch is reset,
     * so the BLOB stream size is kept for such BLOB buffer.</p>
     */
    public void clearBlobStream() {
        blobBuffer.clear();
        if (blobStreamSize <= BLOB_BUFFER_MEMORY_CACHE_SIZE) {
            blobStreamSize = 0L;
        }
    }
    
    /**
     * Reset accumulated batch state.
     */
    public void reset() {
        parameterValues.clear();
        accumulatedSize = 0;
        blobIds.clear();
        streamBlobContents.clear();
        blobBuffer.clear();
        blobStreamSize = 0L;
    }
}
