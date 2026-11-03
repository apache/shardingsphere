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

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchAlreadyOpenedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchParametersRequiredException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.util.Collection;
import java.util.Collections;

/**
 * Create Batch command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdBatchCreateCommandExecutor implements CommandExecutor {
    
    private static final long DEFAULT_BUFFER_SIZE = 16L * 1024 * 1024;
    
    private static final long MAX_BUFFER_SIZE = 256L * 1024 * 1024;
    
    private static final int BATCH_VERSION_1 = 1;
    
    private static final int TAG_MULTIERROR = 1;
    
    private static final int TAG_RECORD_COUNTS = 2;
    
    private static final int TAG_BUFFER_BYTES_SIZE = 3;
    
    private static final int TAG_BLOB_POLICY = 4;
    
    private static final int WIDE_CLUMPLET_LENGTH_SIZE = 4;
    
    private static final int INTEGER_VALUE_LENGTH = 4;
    
    private final FirebirdBatchCreateCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        int statementId = packet.getStatementHandle();
        ShardingSpherePreconditions.checkNotNull(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId), () -> new InvalidStatementHandleException(statementId));
        int connectionId = connectionSession.getConnectionId();
        ShardingSpherePreconditions.checkState(null == FirebirdBatchStatementManager.getInstance().getBatchStatement(connectionId, statementId), () -> new BatchAlreadyOpenedException(statementId));
        ByteBuf batchBlr = packet.getBatchBlr();
        FirebirdParseBatchBlr messageFormat = FirebirdParseBatchBlr.parse(batchBlr, batchBlr.readableBytes());
        ShardingSpherePreconditions.checkNotEmpty(messageFormat.getFields(), () -> new BatchParametersRequiredException(statementId));
        if (packet.getBatchMessageLength() != messageFormat.getMessageLength()) {
            throw new InvalidBatchMessageFormatException(
                    String.format("invalid message length: computed %d from BLR but client sent %d", messageFormat.getMessageLength(), packet.getBatchMessageLength()));
        }
        BatchParameters batchParameters = BatchParameters.parse(packet.getBatchParametersBuffer());
        FirebirdBatchStatementManager.getInstance().registerBatchStatement(
                connectionId, statementId, messageFormat.getFields(), batchParameters.getBufferSize(), batchParameters.isRecordCounts(), batchParameters.isMultiError());
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(statementId));
    }
    
    @RequiredArgsConstructor
    @Getter
    static final class BatchParameters {
        
        private final int version;
        
        private final long bufferSize;
        
        private final boolean recordCounts;
        
        private final boolean multiError;
        
        static BatchParameters parse(final ByteBuf batchParametersBuffer) {
            if (!batchParametersBuffer.isReadable()) {
                return new BatchParameters(BATCH_VERSION_1, DEFAULT_BUFFER_SIZE, false, false);
            }
            ByteBuf reader = batchParametersBuffer.duplicate();
            int version = reader.readUnsignedByte();
            ShardingSpherePreconditions.checkState(BATCH_VERSION_1 == version, () -> new InvalidBatchParameterVersionException(version, BATCH_VERSION_1));
            long bufferSize = DEFAULT_BUFFER_SIZE;
            boolean recordCounts = false;
            boolean multiError = false;
            while (reader.isReadable()) {
                ensureClumpletHeaderReadable(reader);
                int tag = reader.readUnsignedByte();
                int valueLength = reader.readIntLE();
                ensureClumpletValueReadable(reader, tag, valueLength);
                if (TAG_MULTIERROR == tag) {
                    multiError = 0 != readIntegerValue(reader, tag, valueLength);
                } else if (TAG_RECORD_COUNTS == tag) {
                    recordCounts = 0 != readIntegerValue(reader, tag, valueLength);
                } else if (TAG_BUFFER_BYTES_SIZE == tag) {
                    bufferSize = getBufferSize(readIntegerValue(reader, tag, valueLength));
                } else if (TAG_BLOB_POLICY == tag) {
                    // TODO Support BLOB policy after implementing the Firebird batch BLOB subprotocol.
                    throw new DatabaseProtocolException("BLOB policy is not supported in Firebird batch operations");
                } else {
                    reader.skipBytes(valueLength);
                }
            }
            return new BatchParameters(version, bufferSize, recordCounts, multiError);
        }
        
        private static long getBufferSize(final int requestedBufferSize) {
            long result = Integer.toUnsignedLong(requestedBufferSize);
            return 0L == result ? MAX_BUFFER_SIZE : Math.min(result, MAX_BUFFER_SIZE);
        }
        
        private static void ensureClumpletHeaderReadable(final ByteBuf reader) {
            ShardingSpherePreconditions.checkState(reader.readableBytes() >= 1 + WIDE_CLUMPLET_LENGTH_SIZE, () -> new DatabaseProtocolException("Invalid batch parameters buffer"));
        }
        
        private static void ensureClumpletValueReadable(final ByteBuf reader, final int tag, final int valueLength) {
            ShardingSpherePreconditions.checkState(valueLength >= 0 && valueLength <= reader.readableBytes(), () -> new DatabaseProtocolException("Invalid batch parameter length for tag %d: %d", tag,
                    valueLength));
        }
        
        private static int readIntegerValue(final ByteBuf reader, final int tag, final int valueLength) {
            ShardingSpherePreconditions.checkState(INTEGER_VALUE_LENGTH == valueLength, () -> new DatabaseProtocolException("Invalid batch parameter integer length for tag %d: %d", tag, valueLength));
            return reader.readIntLE();
        }
    }
}
