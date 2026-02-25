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

import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSendMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload.FirebirdBlobParameterBinder;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Batch message command executor for Firebird.
 */
@RequiredArgsConstructor
@Slf4j
public final class FirebirdSendBatchMessageCommandExecutor implements CommandExecutor {
    
    private final FirebirdBatchSendMessageCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(connectionSession.getConnectionId(), packet.getStatementHandle());
        batchStatement.setBatchMessageCount(packet.getBatchMessageCount());
        batchStatement.setBatchData(packet.getBatchData());
        batchStatement.setParameterValues(getBatchParameterValues(batchStatement));
        return Collections.singleton(new FirebirdGenericResponsePacket());
    }
    
    private List<List<Object>> getBatchParameterValues(final FirebirdBatchStatement batchStatement) {
        final List<FirebirdBinaryColumnType> parameterTypes = batchStatement.getBatchBlr().getColumnTypes();
        final List<List<Object>> result = new ArrayList<>();
        final FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.wrappedBuffer(batchStatement.getBatchData()), StandardCharsets.UTF_8);
        for (int index = 0; index < batchStatement.getBatchMessageCount() && payload.getByteBuf().isReadable(); index++) {
            final int startRowIndex = payload.getByteBuf().readerIndex();
            List<Object> rowParams = readParameterValues(payload, parameterTypes);
            if (FirebirdBlobParameterBinder.containsBlob(parameterTypes)) {
                rowParams = FirebirdBlobParameterBinder.bindBlobParameters(connectionSession.getConnectionId(), rowParams, parameterTypes).getParams();
            }
            result.add(rowParams);
            final int rowSize = payload.getByteBuf().readerIndex() - startRowIndex;
            final int alignedRowSize = align4(rowSize);
            final int rowEndIndex = Math.min(startRowIndex + alignedRowSize, payload.getByteBuf().writerIndex());
            payload.getByteBuf().readerIndex(rowEndIndex);
        }
        return result;
    }
    
    private int align4(final int length) {
        return length + (4 - length % 4) % 4;
    }
    
    private List<Object> readParameterValues(final FirebirdPacketPayload payload, final List<FirebirdBinaryColumnType> parameterTypes) {
        final List<Integer> nullBits = new ArrayList<>();
        final int nullBitsLength = (parameterTypes.size() + 7) / 8;
        for (int index = 0; index < nullBitsLength; index++) {
            nullBits.add(payload.readInt1());
        }
        payload.skipPadding(nullBitsLength);
        final List<Object> result = new ArrayList<>(parameterTypes.size());
        for (int index = 0; index < parameterTypes.size(); index++) {
            final int nullBit = nullBits.get(index / 8);
            if (((nullBit >> index % 8) & 1) == 0) {
                final FirebirdBinaryColumnType parameterType = parameterTypes.get(index);
                final FirebirdBinaryProtocolValue binaryProtocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(parameterType);
                if (FirebirdBinaryColumnType.BLOB == parameterType) {
                    result.add(payload.readInt8());
                } else {
                    result.add(binaryProtocolValue.read(payload));
                }
            } else {
                result.add(null);
            }
        }
        return result;
    }
}
