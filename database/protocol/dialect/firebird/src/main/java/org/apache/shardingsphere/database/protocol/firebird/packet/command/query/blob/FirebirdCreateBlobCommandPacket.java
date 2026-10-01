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

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdBlobParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Firebird create blob command packet.
 */
@Getter
public final class FirebirdCreateBlobCommandPacket extends FirebirdCommandPacket {
    
    private static final int BLOB_TYPE_STREAM = 1;
    
    private final FirebirdParameterBuffer bpb = FirebirdBlobParameterBufferType.createBuffer();
    
    private final int transactionId;
    
    private final long requestedBlobId;
    
    public FirebirdCreateBlobCommandPacket(final FirebirdCommandPacketType commandType, final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        if (FirebirdCommandPacketType.CREATE_BLOB2 == commandType) {
            ByteBuf buffer = payload.readBuffer();
            if (buffer.isReadable()) {
                bpb.parseBuffer(buffer);
            }
        }
        transactionId = payload.readInt4();
        requestedBlobId = payload.readInt8();
    }
    
    /**
     * Is stream blob.
     *
     * <p>A BLOB without a blob parameter buffer, with an empty one, or with one that omits the type item, is a segmented BLOB.</p>
     *
     * @return stream blob or not
     */
    public boolean isStreamBlob() {
        Integer blobType = bpb.getValue(FirebirdBlobParameterBufferType.TYPE);
        return null != blobType && BLOB_TYPE_STREAM == blobType;
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
        int length = 4;
        if (FirebirdCommandPacketType.CREATE_BLOB2 == commandType) {
            length += payload.getBufferLength(length);
        }
        length += 4;
        length += 8;
        return length;
    }
    
}
