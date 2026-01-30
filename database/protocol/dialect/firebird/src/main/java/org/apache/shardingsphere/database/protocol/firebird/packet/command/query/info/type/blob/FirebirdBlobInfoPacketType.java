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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird blob info packet type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdBlobInfoPacketType implements FirebirdInfoPacketType {
    
    NUM_SEGMENTS(4),
    MAX_SEGMENT(5),
    TOTAL_LENGTH(6),
    TYPE(7);
    
    private static final Map<Integer, FirebirdBlobInfoPacketType> FIREBIRD_BLOB_INFO_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    static {
        for (FirebirdBlobInfoPacketType each : values()) {
            FIREBIRD_BLOB_INFO_TYPE_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Value of.
     *
     * @param code blob info type code
     * @return Firebird blob info packet type
     */
    public static FirebirdBlobInfoPacketType valueOf(final int code) {
        return FIREBIRD_BLOB_INFO_TYPE_CACHE.get(code);
    }
    
    /**
     * Creates info packet of this type.
     *
     * @param payload Firebird packet payload
     * @return Firebird blob info packet
     */
    public static FirebirdInfoPacket createPacket(final FirebirdPacketPayload payload) {
        return new FirebirdInfoPacket(payload, FirebirdBlobInfoPacketType::valueOf);
    }
    
    @Override
    public boolean isCommon() {
        return false;
    }
}
