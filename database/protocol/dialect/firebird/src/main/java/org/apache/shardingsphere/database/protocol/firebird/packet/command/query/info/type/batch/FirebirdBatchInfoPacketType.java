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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird batch info packet type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdBatchInfoPacketType implements FirebirdInfoPacketType {
    
    BUFFER_BYTES_SIZE(10),
    DATA_BYTES_SIZE(11),
    BLOBS_BYTES_SIZE(12),
    BLOB_ALIGNMENT(13),
    BLOB_HEADER(14);
    
    private static final Map<Integer, FirebirdBatchInfoPacketType> FIREBIRD_BATCH_INFO_TYPE_CACHE = new HashMap<>(values().length, 1F);
    
    private final int code;
    
    static {
        for (FirebirdBatchInfoPacketType each : values()) {
            FIREBIRD_BATCH_INFO_TYPE_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Creates info packet of this type.
     *
     * <p>An item other than a batch item, the end item or the length item is parsed as {@link FirebirdCommonInfoPacketType#ERROR}, because Firebird answers it with an info error.</p>
     *
     * @param payload Firebird packet payload
     * @return Firebird batch info packet
     */
    public static FirebirdInfoPacket createPacket(final FirebirdPacketPayload payload) {
        return new FirebirdInfoPacket(payload, FirebirdBatchInfoPacketType::toInfoItem);
    }
    
    private static FirebirdInfoPacketType toInfoItem(final int code) {
        if (FIREBIRD_BATCH_INFO_TYPE_CACHE.containsKey(code)) {
            return FIREBIRD_BATCH_INFO_TYPE_CACHE.get(code);
        }
        return FirebirdCommonInfoPacketType.END.getCode() == code || FirebirdCommonInfoPacketType.LENGTH.getCode() == code
                ? FirebirdCommonInfoPacketType.valueOf(code)
                : FirebirdCommonInfoPacketType.ERROR;
    }
    
    @Override
    public boolean isCommon() {
        return false;
    }
}
