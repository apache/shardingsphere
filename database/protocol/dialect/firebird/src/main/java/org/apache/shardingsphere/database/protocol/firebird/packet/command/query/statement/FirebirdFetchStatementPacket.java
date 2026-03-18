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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird allocate statement packet.
 */
@Getter
public final class FirebirdFetchStatementPacket extends FirebirdCommandPacket {
    
    private final int statementId;
    
    private final List<FirebirdBinaryColumnType> parameterTypes;
    
    private final int message;
    
    private final int fetchSize;
    
    public FirebirdFetchStatementPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        statementId = payload.readInt4();
        parameterTypes = parseBLR(payload.readBuffer());
        message = payload.readInt4();
        fetchSize = payload.readInt4();
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
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @return Length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        int length = 8;
        length += payload.getBufferLength(length);
        return length + 8;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
}
