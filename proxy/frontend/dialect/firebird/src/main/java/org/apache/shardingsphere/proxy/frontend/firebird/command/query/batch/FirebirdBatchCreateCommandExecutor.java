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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchAlreadyOpenedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchParametersRequiredException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.sql.SQLException;
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
    
    // private static final int BLOB_STREAM = 3;
    
    private static final int WIDE_CLUMPLET_LENGTH_SIZE = 4;
    
    private static final int INTEGER_VALUE_LENGTH = 4;
    
    private final FirebirdBatchCreateCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        int connectionId = connectionSession.getConnectionId();
        int statementId = packet.getStatementHandle();
        if (null == connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId)) {
            throw new InvalidStatementHandleException(statementId);
        }
        if (null != FirebirdBatchRegistry.getInstance().getBatchStatement(connectionId, statementId)) {
            throw new BatchAlreadyOpenedException(statementId);
        }
        ByteBuf batchBlr = packet.getBatchBlr();
        int blrLength = batchBlr.readableBytes();
        FirebirdParseBatchBlr messageFormat = FirebirdParseBatchBlr.parse(batchBlr, blrLength);
        if (messageFormat.getFields().isEmpty()) {
            throw new BatchParametersRequiredException(statementId);
        }
        if (packet.getBatchMessageLength() != messageFormat.getMessageLength()) {
            throw new InvalidBatchMessageFormatException(
                    String.format("invalid message length: computed %d from BLR but client sent %d", messageFormat.getMessageLength(), packet.getBatchMessageLength()));
        }
        ByteBuf batchParametersBuffer = packet.getBatchParametersBuffer();
        BatchParameters batchParameters = BatchParameters.parse(batchParametersBuffer);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(connectionId, statementId,
                new FirebirdBatchStatement(statementId, messageFormat.getFields(), batchParameters.getBufferSize(), batchParameters.isRecordCounts(), batchParameters.isMultiError()));
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(statementId));
    }
    
    @Getter
    @RequiredArgsConstructor
    static final class BatchParameters {
        
        private final int version;
        
        private final long bufferSize;
        
        private final boolean recordCounts;
        
        private final boolean multiError;
        
        // private final int blobPolicy;
        
        static BatchParameters parse(final ByteBuf batchParametersBuffer) {
            if (null == batchParametersBuffer || !batchParametersBuffer.isReadable()) {
                // return new BatchParameters(BATCH_VERSION_1, DEFAULT_BUFFER_SIZE, false, false, BLOB_STREAM);
                return new BatchParameters(BATCH_VERSION_1, DEFAULT_BUFFER_SIZE, false, false);
            }
            ByteBuf reader = batchParametersBuffer.duplicate();
            int version = reader.readUnsignedByte();
            if (BATCH_VERSION_1 != version) {
                throw new InvalidBatchParameterVersionException(version, BATCH_VERSION_1);
            }
            long bufferSize = DEFAULT_BUFFER_SIZE;
            boolean recordCounts = false;
            boolean multiError = false;
            // int blobPolicy = BLOB_STREAM;
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
                    // int requestedBlobPolicy = readIntegerValue(reader, tag, valueLength);
                    // blobPolicy = BLOB_STREAM == requestedBlobPolicy ? requestedBlobPolicy : BLOB_STREAM;
                    // TODO Support BLOB policy after implementing the Firebird batch BLOB subprotocol.
                    throw new FirebirdProtocolException("BLOB policy is not supported in Firebird batch operations");
                } else {
                    reader.skipBytes(valueLength);
                }
            }
            // return new BatchParameters(version, bufferSize, recordCounts, multiError, blobPolicy);
            return new BatchParameters(version, bufferSize, recordCounts, multiError);
        }
        
        private static long getBufferSize(final int requestedBufferSize) {
            long result = Integer.toUnsignedLong(requestedBufferSize);
            return 0L == result ? MAX_BUFFER_SIZE : Math.min(result, MAX_BUFFER_SIZE);
        }
        
        private static void ensureClumpletHeaderReadable(final ByteBuf reader) {
            if (reader.readableBytes() < 1 + WIDE_CLUMPLET_LENGTH_SIZE) {
                throw new FirebirdProtocolException("Invalid batch parameters buffer");
            }
        }
        
        private static void ensureClumpletValueReadable(final ByteBuf reader, final int tag, final int valueLength) {
            if (valueLength < 0 || valueLength > reader.readableBytes()) {
                throw new FirebirdProtocolException("Invalid batch parameter length for tag %d: %d", tag, valueLength);
            }
        }
        
        private static int readIntegerValue(final ByteBuf reader, final int tag, final int valueLength) {
            if (INTEGER_VALUE_LENGTH != valueLength) {
                throw new FirebirdProtocolException("Invalid batch parameter integer length for tag %d: %d", tag, valueLength);
            }
            return reader.readIntLE();
        }
    }
}
