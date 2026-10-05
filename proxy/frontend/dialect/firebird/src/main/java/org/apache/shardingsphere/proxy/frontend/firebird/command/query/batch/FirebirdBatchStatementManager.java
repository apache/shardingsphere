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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import io.netty.buffer.ByteBufUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobBufferFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchDefaultBpbChangeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchBlobPolicyException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchBlobStreamCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSetBpbCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStreamBlob;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdBlobParameterBufferUtils;

import java.util.List;

/**
 * Batch statement manager for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdBatchStatementManager {
    
    private static final FirebirdBatchStatementManager INSTANCE = new FirebirdBatchStatementManager();
    
    /**
     * Get batch statement manager instance.
     *
     * @return batch statement manager instance
     */
    public static FirebirdBatchStatementManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        FirebirdBatchRegistry.getInstance().registerConnection(connectionId);
    }
    
    /**
     * Unregister connection and its batch statements.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        FirebirdBatchRegistry.getInstance().unregisterConnection(connectionId);
    }
    
    /**
     * Get batch statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return batch statement, or {@code null} if absent
     */
    public FirebirdBatchStatement getBatchStatement(final int connectionId, final int statementId) {
        return FirebirdBatchRegistry.getInstance().getBatchStatement(connectionId, statementId);
    }
    
    /**
     * Register batch statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param columnDescriptors column descriptors
     * @param bufferSize buffer size
     * @param recordCounts whether record counts are requested
     * @param multiError whether multiple errors are requested
     * @param blobStreamAllowed whether BLOB policy allows BLOB stream
     */
    public void registerBatchStatement(final int connectionId, final int statementId, final List<FirebirdBatchColumnDescriptor> columnDescriptors,
                                       final long bufferSize, final boolean recordCounts, final boolean multiError, final boolean blobStreamAllowed) {
        FirebirdBatchRegistry.getInstance().registerBatchStatement(connectionId, statementId,
                new FirebirdBatchStatement(statementId, columnDescriptors, bufferSize, recordCounts, multiError, blobStreamAllowed));
    }
    
    /**
     * Append batch message.
     *
     * @param connectionId connection ID
     * @param packet batch message packet
     * @throws InvalidBatchHandleException if batch statement does not exist
     * @throws BatchTooBigException if appending the message exceeds the batch buffer size
     */
    public void appendBatchMessage(final int connectionId, final FirebirdBatchMessageCommandPacket packet) {
        FirebirdBatchStatement batchStatement = getBatchStatement(connectionId, packet.getStatementHandle());
        ShardingSpherePreconditions.checkNotNull(batchStatement, () -> new InvalidBatchHandleException(packet.getStatementHandle()));
        int dataLength = packet.getDataLength();
        ShardingSpherePreconditions.checkState(batchStatement.getAccumulatedSize() + dataLength <= batchStatement.getBufferSize(),
                () -> new BatchTooBigException(packet.getStatementHandle(), batchStatement.getAccumulatedSize(), dataLength, batchStatement.getBufferSize()));
        for (List<Object> each : packet.readParameterValues(batchStatement.getColumnDescriptors())) {
            batchStatement.addParameterValues(each);
        }
        batchStatement.addSize(dataLength);
    }
    
    /**
     * Append BLOB stream portion.
     *
     * @param connectionId connection ID
     * @param packet batch BLOB stream packet
     * @throws InvalidBatchHandleException if batch statement does not exist
     * @throws InvalidBatchBlobPolicyException if BLOB policy of batch does not allow BLOB stream
     */
    void appendBlobStream(final int connectionId, final FirebirdBatchBlobStreamCommandPacket packet) {
        FirebirdBatchStatement batchStatement = getBatchStatement(connectionId, packet.getStatementHandle());
        ShardingSpherePreconditions.checkNotNull(batchStatement, () -> new InvalidBatchHandleException(packet.getStatementHandle()));
        ShardingSpherePreconditions.checkState(batchStatement.isBlobStreamAllowed(), () -> new InvalidBatchBlobPolicyException("addBlobStream"));
        batchStatement.appendBlobStream(packet.getData(), packet.getLength());
    }
    
    /**
     * Set default BLOB parameter buffer.
     *
     * @param connectionId connection ID
     * @param packet batch set default BLOB parameter buffer packet
     * @throws InvalidBatchHandleException if batch statement does not exist
     * @throws BatchDefaultBpbChangeException if batch already contains BLOB stream data
     */
    void setDefaultBpb(final int connectionId, final FirebirdBatchSetBpbCommandPacket packet) {
        FirebirdBatchStatement batchStatement = getBatchStatement(connectionId, packet.getStatementHandle());
        ShardingSpherePreconditions.checkNotNull(batchStatement, () -> new InvalidBatchHandleException(packet.getStatementHandle()));
        batchStatement.getBlobStream().setDefaultBpb(packet.getBpb());
        ShardingSpherePreconditions.checkState(0L == batchStatement.getBlobStreamSize(), () -> new BatchDefaultBpbChangeException(packet.getStatementHandle()));
        batchStatement.setDefaultBpb(ByteBufUtil.getBytes(packet.getBpb()));
    }
    
    /**
     * Register BLOBs of the BLOB buffer under their batch BLOB IDs, as Firebird does before executing batch messages, and clear the BLOB buffer.
     *
     * @param batchStatement batch statement
     * @param connectionCharsetId character set ID of the connection
     * @throws BatchSmallDataException if a BLOB header is incomplete
     * @throws BatchBlobBufferFormatException if a continuation has no BLOB to continue
     * @throws BatchBlobContinuationBpbException if a BLOB continuation contains a BLOB parameter buffer
     * @throws BatchBpbTooBigException if a BLOB parameter buffer is incomplete
     * @throws RepeatedBatchBlobIdException if batch BLOB ID is already registered
     * @throws BatchSegmentExceedsBlobException if a BLOB segment exceeds its BLOB
     * @throws BatchSegmentTooBigException if a BLOB segment is incomplete
     * @throws BlobFilterNotFoundException if Firebird has no BLOB filter for the sub type conversion of a BLOB parameter buffer
     * @throws UnsupportedBlobFilterException if a BLOB parameter buffer requires an unsupported BLOB filter conversion
     * @throws TransliterationFailedException if BLOB data cannot be transliterated as its BLOB parameter buffer requires
     */
    void registerStreamBlobs(final FirebirdBatchStatement batchStatement, final int connectionCharsetId) {
        for (FirebirdBatchStreamBlob each : batchStatement.readStreamBlobs()) {
            byte[] data = each.getData().toByteArray();
            byte[] content = FirebirdBlobParameterBufferUtils.createTransliterator(each.getBpb(), connectionCharsetId).map(transliterator -> transliterator.put(data)).orElse(data);
            batchStatement.getStreamBlobContents().put(each.getBatchBlobId(), content);
        }
        batchStatement.clearBlobStream();
    }
    
    /**
     * Reset batch statement.
     *
     * @param batchStatement batch statement
     */
    public void resetBatchStatement(final FirebirdBatchStatement batchStatement) {
        batchStatement.reset();
    }
    
    /**
     * Unregister batch statement.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterBatchStatement(final int connectionId, final int statementId) {
        FirebirdBatchRegistry.getInstance().unregisterBatchStatement(connectionId, statementId);
    }
}
