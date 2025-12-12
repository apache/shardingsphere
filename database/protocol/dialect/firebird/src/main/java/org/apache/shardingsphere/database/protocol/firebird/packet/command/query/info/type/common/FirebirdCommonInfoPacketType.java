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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird common info packet type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdCommonInfoPacketType implements FirebirdInfoPacketType {
    
    END(1),
    TRUNCATED(2),
    ERROR(3),
    DATA_NOT_READY(4),
    LENGTH(126),
    FLAG_END(127);
    
    private static final Map<Integer, FirebirdCommonInfoPacketType> FIREBIRD_INFO_COMMON_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    static {
        for (FirebirdCommonInfoPacketType each : values()) {
            FIREBIRD_INFO_COMMON_TYPE_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird dpb type
     */
    public static FirebirdCommonInfoPacketType valueOf(final int code) {
        FirebirdCommonInfoPacketType result = FIREBIRD_INFO_COMMON_TYPE_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find code '%d' in common info type", code);
        return result;
    }
    
    /**
     * Parse and write Firebird common information packet based on the given type.
     *
     * @param data payload to write data to
     * @param type type of common info packet
     * @throws FirebirdProtocolException if the common info packet type is unknown
     */
    public static void parseCommonInfo(final FirebirdPacketPayload data, final FirebirdCommonInfoPacketType type) {
        // TODO implement other request types handle
        switch (type) {
            case END:
                data.writeInt1(END.getCode());
                break;
            default:
                throw new FirebirdProtocolException("Unknown common information request type %d", type.getCode());
        }
    }
    
    @Override
    public boolean isCommon() {
        return true;
    }
}
