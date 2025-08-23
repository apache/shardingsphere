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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird SQL info packet type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdSQLInfoPacketType implements FirebirdInfoPacketType {
    
    SELECT(4),
    BIND(5),
    NUM_VARIABLES(6),
    DESCRIBE_VARS(7),
    DESCRIBE_END(8),
    SQLDA_SEQ(9),
    MESSAGE_SEQ(10),
    TYPE(11),
    SUB_TYPE(12),
    SCALE(13),
    LENGTH(14),
    NULL_IND(15),
    FIELD(16),
    RELATION(17),
    OWNER(18),
    ALIAS(19),
    SQLDA_START(20),
    STMT_TYPE(21),
    GET_PLAN(22),
    RECORDS(23),
    BATCH_FETCH(24),
    RELATION_ALIAS(25),
    EXPLAIN_PLAN(26),
    STMT_FLAGS(27),
    STMT_TIMEOUT_USER(28),
    STMT_TIMEOUT_RUN(29),
    STMT_BLOB_ALIGN(30);
    
    private static final Map<Integer, FirebirdSQLInfoPacketType> FIREBIRD_DATABASE_INFO_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    static {
        for (FirebirdSQLInfoPacketType each : values()) {
            FIREBIRD_DATABASE_INFO_TYPE_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird dpb type
     */
    public static FirebirdSQLInfoPacketType valueOf(final int code) {
        return FIREBIRD_DATABASE_INFO_TYPE_CACHE.get(code);
    }
    
    /**
     * Creates info packet of this type.
     *
     * @param payload Firebird packet payload
     * @return Firebird database info packet
     */
    public static FirebirdInfoPacket createPacket(final FirebirdPacketPayload payload) {
        return new FirebirdInfoPacket(payload, FirebirdSQLInfoPacketType::valueOf);
    }
    
    @Override
    public boolean isCommon() {
        return false;
    }
}
