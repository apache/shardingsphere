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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Firebird batch segments with blob command packet.
 *
 * <p>Similar to Put segment, but allows to send multiple segments.</p>
 */
@Getter
public final class FirebirdBatchBlobSegmentsCommandPacket extends FirebirdCommandPacket {
    
    private final int blobHandle;
    
    private final int segmentLength;
    
    private final Collection<byte[]> segments;
    
    public FirebirdBatchBlobSegmentsCommandPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        blobHandle = payload.readBlobHandle();
        segmentLength = payload.readInt4();
        segments = readSegments(payload.readBuffer());
    }
    
    private Collection<byte[]> readSegments(final ByteBuf buffer) {
        Collection<byte[]> result = new LinkedList<>();
        while (buffer.readableBytes() >= Short.BYTES) {
            int segmentLength = buffer.readUnsignedShortLE();
            byte[] segmentData = new byte[segmentLength];
            buffer.readBytes(segmentData);
            result.add(segmentData);
        }
        return result;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @return length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        int length = 12;
        length += payload.getBufferLength(length);
        return length;
    }
}
