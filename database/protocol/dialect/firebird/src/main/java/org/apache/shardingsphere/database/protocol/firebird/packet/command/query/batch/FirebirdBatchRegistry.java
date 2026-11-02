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

import com.google.common.base.Suppliers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Batch registry for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdBatchRegistry {
    
    private static final FirebirdBatchRegistry INSTANCE = new FirebirdBatchRegistry();
    
    private final Map<Integer, ConnectionBatches> batchRegistry = new ConcurrentHashMap<>();
    
    /**
     * Get batch registry instance.
     *
     * @return batch registry instance
     */
    public static FirebirdBatchRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        batchRegistry.put(connectionId, new ConnectionBatches());
    }
    
    /**
     * Register batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @param batchStatement batch statement
     * @throws IllegalStateException if connection is not registered
     */
    public void registerBatchStatement(final int connectionId, final int statementId, final FirebirdBatchStatement batchStatement) {
        ConnectionBatches batches = batchRegistry.get(connectionId);
        ShardingSpherePreconditions.checkNotNull(batches, () -> new IllegalStateException("Connection [" + connectionId + "] is not registered."));
        batches.statements.put(statementId, batchStatement);
        batches.rejectedFormats.remove(statementId);
    }
    
    /**
     * Get batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return batch statement or null if not found
     */
    public FirebirdBatchStatement getBatchStatement(final int connectionId, final int statementId) {
        ConnectionBatches batches = batchRegistry.get(connectionId);
        return null == batches ? null : batches.statements.get(statementId);
    }
    
    /**
     * Register framing metadata for a rejected batch without making it executable.
     *
     * <p>The connection and prepared statement must already exist. The BLR is copied before its receive buffer is released.</p>
     *
     * @param connectionId registered connection ID
     * @param statementId prepared statement ID
     * @param batchBlr batch BLR buffer
     * @throws IllegalStateException if connection is not registered
     */
    public void registerRejectedBatchFormat(final int connectionId, final int statementId, final ByteBuf batchBlr) {
        ConnectionBatches batches = batchRegistry.get(connectionId);
        ShardingSpherePreconditions.checkNotNull(batches, () -> new IllegalStateException("Connection [" + connectionId + "] is not registered."));
        byte[] snapshot = ByteBufUtil.getBytes(batchBlr);
        batches.rejectedFormats.put(statementId, Suppliers.memoize(() -> parseBatchFormat(snapshot)));
    }
    
    private List<FirebirdBatchColumnDescriptor> parseBatchFormat(final byte[] batchBlr) {
        ByteBuf buffer = Unpooled.wrappedBuffer(batchBlr);
        try {
            return FirebirdParseBatchBlr.parseForFraming(buffer, batchBlr.length).getFields();
        } finally {
            buffer.release();
        }
    }
    
    /**
     * Get the message format for framing, preferring an executable batch over a rejected batch.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     * @return message format, or {@code null} if no format is available
     */
    public List<FirebirdBatchColumnDescriptor> getMessageFormat(final int connectionId, final int statementId) {
        ConnectionBatches batches = batchRegistry.get(connectionId);
        if (null == batches) {
            return null;
        }
        FirebirdBatchStatement batchStatement = batches.statements.get(statementId);
        if (null != batchStatement) {
            return batchStatement.getColumnDescriptors();
        }
        Supplier<List<FirebirdBatchColumnDescriptor>> format = batches.rejectedFormats.get(statementId);
        return null == format ? null : format.get();
    }
    
    /**
     * Unregister batch metadata.
     *
     * @param connectionId connection ID
     * @param statementId statement ID
     */
    public void unregisterBatchStatement(final int connectionId, final int statementId) {
        ConnectionBatches batches = batchRegistry.get(connectionId);
        if (null != batches) {
            batches.statements.remove(statementId);
            batches.rejectedFormats.remove(statementId);
        }
    }
    
    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        batchRegistry.remove(connectionId);
    }
    
    private static final class ConnectionBatches {
        
        private final Map<Integer, FirebirdBatchStatement> statements = new ConcurrentHashMap<>();
        
        private final Map<Integer, Supplier<List<FirebirdBatchColumnDescriptor>>> rejectedFormats = new ConcurrentHashMap<>();
    }
}
