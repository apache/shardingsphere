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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird execute statement packet.
 */
@Getter
public final class FirebirdExecuteStatementPacket extends FirebirdCommandPacket {
    
    private final FirebirdCommandPacketType type;
    
    private final int statementId;
    
    private final int transactionId;
    
    private final List<FirebirdBinaryColumnType> parameterTypes;
    
    private final int message;
    
    private final List<Object> parameterValues = new ArrayList<>();
    
    private final List<FirebirdBinaryColumnType> returnColumns = new ArrayList<>();
    
    private int outputMessageNumber;
    
    private long statementTimeout;
    
    private long cursorFlags;
    
    private long maxBlobSize;
    
    public FirebirdExecuteStatementPacket(final FirebirdPacketPayload payload, final FirebirdProtocolVersion protocolVersion) {
        type = FirebirdCommandPacketType.valueOf(payload.readInt4());
        statementId = payload.readInt4();
        transactionId = payload.readInt4();
        parameterTypes = parseBLR(payload.readBuffer());
        message = payload.readInt4();
        int msgCount = payload.readInt4();
        List<Integer> nullBits = new ArrayList<>();
        if (msgCount > 0) {
            int length = (parameterTypes.size() + 7) / 8;
            for (int i = 0; i < length; i++) {
                nullBits.add(payload.readInt1());
            }
            payload.skipPadding(length);
        }
        
        for (int i = 0; i < parameterTypes.size(); i++) {
            Integer nullBit = nullBits.get(i / 8);
            if (((nullBit >> i % 8) & 1) == 0) {
                FirebirdBinaryColumnType parameterType = parameterTypes.get(i);
                FirebirdBinaryProtocolValue binaryProtocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(parameterType);
                if (parameterType == FirebirdBinaryColumnType.BLOB) {
                    FirebirdPacketPayload blobPayload = FirebirdBlobRegistry.buildSegmentPayload(payload.getByteBuf().alloc(), payload.getCharset());
                    parameterValues.add(blobPayload == null ? null : binaryProtocolValue.read(blobPayload));
                } else {
                    parameterValues.add(binaryProtocolValue.read(payload));
                }
            } else {
                parameterValues.add(null);
            }
        }
        
        if (isStoredProcedure()) {
            returnColumns.addAll(parseBLR(payload.readBuffer()));
            outputMessageNumber = payload.readInt4();
        }
        
        if (protocolVersion.getCode() >= FirebirdProtocolVersion.PROTOCOL_VERSION16.getCode()) {
            statementTimeout = payload.readInt4Unsigned();
        }
        
        if (protocolVersion.getCode() >= FirebirdProtocolVersion.PROTOCOL_VERSION18.getCode()) {
            cursorFlags = payload.readInt4Unsigned();
        }
        
        if (protocolVersion.getCode() >= FirebirdProtocolVersion.PROTOCOL_VERSION19.getCode()) {
            maxBlobSize = payload.readInt4Unsigned();
        }
    }
    
    private List<FirebirdBinaryColumnType> parseBLR(final ByteBuf blrBuffer) {
        if (!blrBuffer.isReadable()) {
            return new ArrayList<>(0);
        }
        blrBuffer.skipBytes(4);
        int length = blrBuffer.readUnsignedByte();
        length += 256 * blrBuffer.readUnsignedByte();
        List<FirebirdBinaryColumnType> result = new ArrayList<>(length / 2);
        int blrType = blrBuffer.readUnsignedByte();
        while (blrType != BlrConstants.blr_end) {
            FirebirdBinaryColumnType type = FirebirdBinaryColumnType.valueOfBLRType(blrType);
            result.add(type);
            blrBuffer.skipBytes(getSkipCount(type) + 2);
            blrType = blrBuffer.readUnsignedByte();
        }
        return result;
    }
    
    private int getSkipCount(final FirebirdBinaryColumnType type) {
        switch (type) {
            case VARYING:
            case TEXT:
                return 4;
            case NULL:
            case LEGACY_TEXT:
            case LEGACY_VARYING:
                return 2;
            case BLOB:
            case ARRAY:
            case LONG:
            case SHORT:
            case INT64:
            case QUAD:
            case INT128:
                return 1;
            default:
                return 0;
        }
    }
    
    /**
     * Returns true if, and only if, operation is a stored procedure.
     *
     * @return Whether the operation is a stored procedure
     */
    public boolean isStoredProcedure() {
        return type == FirebirdCommandPacketType.EXECUTE2;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @param protocolVersion Firebird protocol version
     * @return length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload, final FirebirdProtocolVersion protocolVersion) {
        // because parameter type is send without length it is easier to just parse whole packet
        new FirebirdExecuteStatementPacket(payload, protocolVersion);
        int length = payload.getByteBuf().readerIndex();
        payload.getByteBuf().resetReaderIndex();
        return length;
    }
}
