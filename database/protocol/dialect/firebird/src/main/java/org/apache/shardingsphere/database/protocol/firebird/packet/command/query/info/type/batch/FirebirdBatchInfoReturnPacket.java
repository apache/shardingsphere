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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.batch;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchBlobStream;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

/**
 * Batch info return data packet for Firebird.
 *
 * <p>Mirrors {@code DsqlBatch::info} behind the remote server: the items are written within the requested buffer length, which also has to hold the total length
 * item the server requests and removes, and a truncated or length-less response occupies the whole requested buffer.</p>
 */
@RequiredArgsConstructor
public final class FirebirdBatchInfoReturnPacket extends FirebirdPacket {
    
    private static final int INT_ITEM_LENGTH = 7;
    
    private static final int MIN_BUFFER_LENGTH = 3;
    
    private final List<FirebirdInfoPacketType> infoItems;
    
    private final FirebirdBatchStatement batchStatement;
    
    private final int maxLength;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        if (maxLength < MIN_BUFFER_LENGTH) {
            writeShortBuffer(payload);
            return;
        }
        int startWriterIndex = payload.getByteBuf().writerIndex();
        int capacity = maxLength - 1;
        int length = 0;
        for (Entry<Integer, Integer> entry : getValues()) {
            if (length + INT_ITEM_LENGTH > capacity) {
                payload.writeInt1(FirebirdCommonInfoPacketType.TRUNCATED.getCode());
                if (length <= maxLength - 2) {
                    payload.writeInt1(FirebirdCommonInfoPacketType.END.getCode());
                }
                fillBuffer(payload, startWriterIndex);
                return;
            }
            writeIntValue(payload, entry.getKey(), entry.getValue());
            length += INT_ITEM_LENGTH;
        }
        payload.writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        if (length + 1 + INT_ITEM_LENGTH > capacity) {
            fillBuffer(payload, startWriterIndex);
        }
    }
    
    private void writeShortBuffer(final FirebirdPacketPayload payload) {
        if (maxLength > 0) {
            payload.writeInt1(FirebirdCommonInfoPacketType.TRUNCATED.getCode());
        }
        if (maxLength > 1) {
            payload.writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        }
    }
    
    private Collection<Entry<Integer, Integer>> getValues() {
        Collection<Entry<Integer, Integer>> result = new ArrayList<>(infoItems.size());
        for (FirebirdInfoPacketType each : infoItems) {
            if (FirebirdCommonInfoPacketType.END == each) {
                break;
            }
            if (FirebirdCommonInfoPacketType.LENGTH == each || FirebirdBatchInfoPacketType.BLOBS_BYTES_SIZE == each && 0L == batchStatement.getBlobStreamSize()) {
                continue;
            }
            if (each.isCommon()) {
                result.add(new SimpleImmutableEntry<>(FirebirdCommonInfoPacketType.ERROR.getCode(), ISCConstants.isc_infunk));
            } else {
                result.add(new SimpleImmutableEntry<>(each.getCode(), getValue((FirebirdBatchInfoPacketType) each)));
            }
        }
        return result;
    }
    
    private int getValue(final FirebirdBatchInfoPacketType type) {
        switch (type) {
            case BUFFER_BYTES_SIZE:
                return (int) batchStatement.getBufferSize();
            case DATA_BYTES_SIZE:
                return (int) batchStatement.getAccumulatedSize();
            case BLOBS_BYTES_SIZE:
                return (int) batchStatement.getBlobStreamSize();
            case BLOB_ALIGNMENT:
                return FirebirdBatchBlobStream.ALIGNMENT;
            default:
                return FirebirdBatchBlobStream.HEADER_LENGTH;
        }
    }
    
    private void fillBuffer(final FirebirdPacketPayload payload, final int startWriterIndex) {
        payload.getByteBuf().writeZero(maxLength - (payload.getByteBuf().writerIndex() - startWriterIndex));
    }
    
    private void writeIntValue(final FirebirdPacketPayload payload, final int code, final int value) {
        payload.writeInt1(code);
        payload.writeInt2LE(4);
        payload.writeInt4LE(value);
    }
}
