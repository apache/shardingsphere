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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;

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
     */
    public void registerBatchStatement(final int connectionId, final int statementId, final List<FirebirdBatchColumnDescriptor> columnDescriptors,
                                       final long bufferSize, final boolean recordCounts, final boolean multiError) {
        FirebirdBatchRegistry.getInstance().registerBatchStatement(connectionId, statementId,
                new FirebirdBatchStatement(statementId, columnDescriptors, bufferSize, recordCounts, multiError));
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
        if (null == batchStatement) {
            throw new InvalidBatchHandleException(packet.getStatementHandle());
        }
        if (batchStatement.getAccumulatedSize() + packet.getDataLength() > batchStatement.getBufferSize()) {
            throw new BatchTooBigException(packet.getStatementHandle(), batchStatement.getAccumulatedSize(), packet.getDataLength(), batchStatement.getBufferSize());
        }
        for (List<Object> each : packet.readParameterValues(batchStatement.getColumnDescriptors())) {
            batchStatement.addParameterValues(each);
        }
        batchStatement.addSize(packet.getDataLength());
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
