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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Firebird open blob command packet.
 */
@Getter
public final class FirebirdOpenBlobCommandPacket extends FirebirdCommandPacket {
    
    private final byte[] blobParameterBuffer;
    
    private final int transactionId;
    
    private final long blobId;
    
    public FirebirdOpenBlobCommandPacket(final FirebirdCommandPacketType commandType, final FirebirdPacketPayload payload) {
        Preconditions.checkArgument(FirebirdCommandPacketType.OPEN_BLOB == commandType
                || FirebirdCommandPacketType.OPEN_BLOB2 == commandType, "Unsupported blob command type: %s", commandType);
        payload.skipReserved(4);
        if (FirebirdCommandPacketType.OPEN_BLOB2 == commandType) {
            ByteBuf buffer = payload.readBuffer();
            blobParameterBuffer = new byte[buffer.readableBytes()];
            buffer.readBytes(blobParameterBuffer);
        } else {
            blobParameterBuffer = new byte[0];
        }
        transactionId = payload.readInt4();
        blobId = payload.readInt8();
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param commandType command packet type for Firebird
     * @param payload Firebird packet payload
     * @return length of packet
     */
    public static int getLength(final FirebirdCommandPacketType commandType, final FirebirdPacketPayload payload) {
        Preconditions.checkArgument(
                FirebirdCommandPacketType.OPEN_BLOB == commandType
                        || FirebirdCommandPacketType.OPEN_BLOB2 == commandType,
                "Unsupported blob command type: %s", commandType);
        // reserved (4)
        int length = 4;
        if (FirebirdCommandPacketType.OPEN_BLOB2 == commandType) {
            // + blob parameter buffer
            length += payload.getBufferLength(length);
        }
        // transactionId (4)
        length += 4;
        // blobId (8)
        length += 8;
        return length;
    }
}
