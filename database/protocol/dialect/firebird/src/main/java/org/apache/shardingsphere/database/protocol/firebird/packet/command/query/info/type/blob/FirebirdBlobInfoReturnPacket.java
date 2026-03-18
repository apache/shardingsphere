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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.List;

/**
 * Blob info return data packet for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdBlobInfoReturnPacket extends FirebirdPacket {
    
    private static final int BLOB_TYPE_SEGMENTED = 0;
    
    private final List<FirebirdInfoPacketType> infoItems;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        for (FirebirdInfoPacketType type : infoItems) {
            if (type.isCommon()) {
                FirebirdCommonInfoPacketType.parseCommonInfo(payload, (FirebirdCommonInfoPacketType) type);
            } else {
                parseBlobInfo(payload, (FirebirdBlobInfoPacketType) type);
            }
        }
    }
    
    private void parseBlobInfo(final FirebirdPacketPayload payload, final FirebirdBlobInfoPacketType type) {
        switch (type) {
            case NUM_SEGMENTS:
                writeIntValue(payload, type, getSegmentCount());
                return;
            case MAX_SEGMENT:
                writeIntValue(payload, type, getSegmentLength());
                return;
            case TOTAL_LENGTH:
                writeIntValue(payload, type, getSegmentLength());
                return;
            case TYPE:
                writeIntValue(payload, type, BLOB_TYPE_SEGMENTED);
                return;
            default:
                throw new FirebirdProtocolException("Unknown blob information request type %d", type.getCode());
        }
    }
    
    private void writeIntValue(final FirebirdPacketPayload payload, final FirebirdBlobInfoPacketType type, final int value) {
        payload.writeInt1(type.getCode());
        payload.writeInt2LE(4);
        payload.writeInt4LE(value);
    }
    
    private int getSegmentLength() {
        byte[] segment = FirebirdBlobRegistry.getSegment();
        return segment == null ? 0 : segment.length;
    }
    
    private int getSegmentCount() {
        return getSegmentLength() == 0 ? 0 : 1;
    }
}
