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
import lombok.Getter;
import lombok.Setter;

import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Firebird batch message command packet.
 */
@Getter
public final class FirebirdBatchSendMessageCommandPacket extends FirebirdCommandPacket {
    
    private static final int FIXED_PACKET_LENGTH = 12;
    
    private static final Map<Integer, BatchMessageLengthContext> BATCH_MESSAGE_LENGTH_CONTEXT_CACHE = new ConcurrentHashMap<>();
    
    public FirebirdBatchSendMessageCommandPacket() {
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    static void registerBatchColumnTypes(final int connectionId, final List<FirebirdBinaryColumnType> columnTypes) {
        BATCH_MESSAGE_LENGTH_CONTEXT_CACHE.put(connectionId, new BatchMessageLengthContext(new ArrayList<>(columnTypes), 0, 0));
    }
    
    static void clearBatchMetadataCache(final int connectionId) {
        BATCH_MESSAGE_LENGTH_CONTEXT_CACHE.remove(connectionId);
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @param connectionId connection ID
     * @return length of packet
     * @throws FirebirdProtocolException firebird protocol exception
     */
    public static int getLength(final FirebirdPacketPayload payload, final int connectionId) {
        ByteBuf byteBuf = payload.getByteBuf();
        if (byteBuf.readableBytes() < FIXED_PACKET_LENGTH) {
            return -1;
        }
        BatchMessageLengthContext batchMessageLengthContext = BATCH_MESSAGE_LENGTH_CONTEXT_CACHE.get(connectionId);
        if (null == batchMessageLengthContext) {
            throw new FirebirdProtocolException("column types not specified");
        }
        if (0 == batchMessageLengthContext.getStatementHandle() && 0 == batchMessageLengthContext.getBatchMessageCount()) {
            payload.skipReserved(4);
            batchMessageLengthContext.setStatementHandle(payload.readInt4());
            batchMessageLengthContext.setBatchMessageCount(payload.readInt4Unsigned());
        }
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(connectionId, batchMessageLengthContext.getStatementHandle());
        if (null == batchStatement) {
            throw new FirebirdProtocolException("Batch statement does not exist for statement handle: " + batchMessageLengthContext.getStatementHandle());
        }
        return parseBatchMessageLength(payload, batchMessageLengthContext.getColumnTypes(), batchMessageLengthContext.getBatchMessageCount(), batchStatement);
    }
    
    private static int parseBatchMessageLength(final FirebirdPacketPayload payload, final List<FirebirdBinaryColumnType> columnTypes, final long batchMessageCount,
                                               final FirebirdBatchStatement batchStatement) {
        ByteBuf data = payload.getByteBuf();
        FirebirdPacketPayload dataPayload = new FirebirdPacketPayload(data, payload.getCharset());
        int initialReaderIndex = dataPayload.getByteBuf().readerIndex();
        int lastCompleteMessageReaderIndex = initialReaderIndex;
        for (int i = 0; i < batchMessageCount; i++) {
            int startReaderIndex = dataPayload.getByteBuf().readerIndex();
            try {
                if (batchStatement.getParameterValues().size() == batchMessageCount) {
                    return lastCompleteMessageReaderIndex;
                }
                List<Object> parameterValues = parseSingleMessage(dataPayload, columnTypes);
                if (columnTypes.size() != parameterValues.size()) {
                    int result = (initialReaderIndex == lastCompleteMessageReaderIndex) ? -1 : lastCompleteMessageReaderIndex;
                    return (result != -1) ? -result : result;
                }
                int messageLength = dataPayload.getByteBuf().readerIndex() - startReaderIndex;
                dataPayload.getByteBuf().skipBytes(getPaddingLength(messageLength));
                batchStatement.addParameterValues(parameterValues);
                lastCompleteMessageReaderIndex = dataPayload.getByteBuf().readerIndex();
            } catch (final IndexOutOfBoundsException ex) {
                int result = (initialReaderIndex == lastCompleteMessageReaderIndex) ? -1 : lastCompleteMessageReaderIndex;
                return (result != -1) ? -result : result;
            }
        }
        return lastCompleteMessageReaderIndex;
    }
    
    private static List<Object> parseSingleMessage(final FirebirdPacketPayload payload, final List<FirebirdBinaryColumnType> columnTypes) {
        List<Integer> nullBits = readNullBits(payload, columnTypes.size());
        List<Object> result = new ArrayList<>(columnTypes.size());
        for (int i = 0; i < columnTypes.size(); i++) {
            Integer nullBit = nullBits.get(i / 8);
            if (((nullBit >> i % 8) & 1) != 0) {
                result.add(null);
                continue;
            }
            FirebirdBinaryColumnType columnType = columnTypes.get(i);
            FirebirdBinaryProtocolValue binaryProtocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(columnType);
            result.add(FirebirdBinaryColumnType.BLOB == columnType ? payload.readInt8() : binaryProtocolValue.read(payload));
        }
        return result;
    }
    
    private static int getPaddingLength(final int messageLength) {
        return (4 - messageLength % 4) % 4;
    }
    
    private static List<Integer> readNullBits(final FirebirdPacketPayload payload, final int columnCount) {
        int nullBitsLength = (columnCount + 7) / 8;
        if (0 == nullBitsLength) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>(nullBitsLength);
        for (int i = 0; i < nullBitsLength; i++) {
            result.add(payload.readInt1Unsigned());
        }
        payload.skipPadding(nullBitsLength);
        return result;
    }
    
    /**
     * Unregister connection metadata cache.
     *
     * @param connectionId connection ID
     */
    public static void unregisterConnection(final int connectionId) {
        clearBatchMetadataCache(connectionId);
    }
    
    @Getter
    public static final class BatchMessageLengthContext {
        
        private final List<FirebirdBinaryColumnType> columnTypes;
        
        @Setter
        private int statementHandle;
        
        @Setter
        private long batchMessageCount;
        
        private BatchMessageLengthContext(final List<FirebirdBinaryColumnType> columnTypes, final int statementHandle, final long batchMessageCount) {
            this.columnTypes = columnTypes;
            this.statementHandle = statementHandle;
            this.batchMessageCount = batchMessageCount;
        }
    }
}
