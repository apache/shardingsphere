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
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Firebird batch send message command packet.
 */
@Getter
public final class FirebirdBatchSendMessageCommandPacket extends FirebirdCommandPacket {
    
    private static final int FIXED_BATCH_MSG_HEADER_LENGTH = 12;
    
    private final int statementHandle;
    
    private final long messageCount;
    
    private final ByteBuf data;
    
    private final int dataLength;
    
    private final Charset charset;
    
    public FirebirdBatchSendMessageCommandPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        statementHandle = payload.readInt4();
        messageCount = payload.readInt4Unsigned();
        charset = payload.getCharset();
        data = payload.getByteBuf().readSlice(payload.getByteBuf().readableBytes());
        dataLength = data.readableBytes();
    }
    
    /**
     * Get length of batch message packet by parsing every message.
     *
     * @param payload Firebird packet payload
     * @param connectionId connection ID
     * @return length of packet
     * @throws FirebirdProtocolException firebird protocol exception
     */
    public static int getLength(final FirebirdPacketPayload payload, final int connectionId) {
        return getLength(payload, connectionId, payload.getByteBuf().readerIndex(), payload.getByteBuf().readableBytes());
    }
    
    private static int getLength(final FirebirdPacketPayload payload, final int connectionId, final int startReaderIndex, final int availableBytes) {
        if (availableBytes < FIXED_BATCH_MSG_HEADER_LENGTH) {
            return -1;
        }
        payload.skipReserved(4);
        int statementHandle = payload.readInt4();
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(connectionId, statementHandle);
        if (null == batchStatement) {
            throw new FirebirdProtocolException("Batch statement not found for connectionId: " + connectionId + ", statement handle: " + statementHandle);
        }
        return parseBatchMessages(payload, startReaderIndex, payload.readInt4Unsigned(), batchStatement.getColumnDescriptors());
    }
    
    private static int parseBatchMessages(final FirebirdPacketPayload payload, final int startReaderIndex, final long messageCount,
                                          final List<FirebirdBatchColumnDescriptor> columnDescriptors) {
        for (long each = 0; each < messageCount; each++) {
            int messageStartIndex = payload.getByteBuf().readerIndex();
            try {
                parseSingleMessage(payload, columnDescriptors);
                payload.skipPadding(payload.getByteBuf().readerIndex() - messageStartIndex);
                // CHECKSTYLE:OFF
            } catch (final IndexOutOfBoundsException ex) {
                // CHECKSTYLE:ON
                payload.getByteBuf().readerIndex(messageStartIndex);
                return -1;
            }
        }
        return payload.getByteBuf().readerIndex() - startReaderIndex;
    }
    
    /**
     * Read batch parameter values from packet data.
     *
     * @param columnDescriptors column descriptors
     * @return batch parameter values
     */
    public List<List<Object>> readParameterValues(final List<FirebirdBatchColumnDescriptor> columnDescriptors) {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(data.duplicate(), charset);
        List<List<Object>> result = new ArrayList<>((int) Math.min(messageCount, Integer.MAX_VALUE));
        for (long each = 0; each < messageCount; each++) {
            int messageStartIndex = payload.getByteBuf().readerIndex();
            result.add(parseSingleMessage(payload, columnDescriptors));
            payload.skipPadding(payload.getByteBuf().readerIndex() - messageStartIndex);
        }
        return result;
    }
    
    private static List<Object> parseSingleMessage(final FirebirdPacketPayload payload, final List<FirebirdBatchColumnDescriptor> columnDescriptors) {
        List<Integer> nullBits = readNullBits(payload, columnDescriptors.size());
        List<Object> result = new ArrayList<>(columnDescriptors.size());
        for (int i = 0; i < columnDescriptors.size(); i++) {
            int nullBit = nullBits.get(i / 8);
            if (((nullBit >> i % 8) & 1) != 0) {
                result.add(null);
                continue;
            }
            FirebirdBatchColumnDescriptor descriptor = columnDescriptors.get(i);
            result.add(readValue(payload, descriptor));
        }
        return result;
    }
    
    private static Object readValue(final FirebirdPacketPayload payload, final FirebirdBatchColumnDescriptor descriptor) {
        if (FirebirdBinaryColumnType.BLOB == descriptor.getType()) {
            return payload.readInt8();
        }
        if (FirebirdBinaryColumnType.TEXT == descriptor.getType() || FirebirdBinaryColumnType.LEGACY_TEXT == descriptor.getType()) {
            String result = payload.readBytes(descriptor.getLength()).toString(payload.getCharset());
            payload.skipPadding(descriptor.getLength());
            return result;
        }
        FirebirdBinaryProtocolValue binaryProtocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(descriptor.getType());
        return binaryProtocolValue.read(payload);
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
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
}
