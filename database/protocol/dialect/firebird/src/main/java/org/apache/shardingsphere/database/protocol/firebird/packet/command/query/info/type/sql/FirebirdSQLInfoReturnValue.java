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

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird SQL info return value.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdSQLInfoReturnValue {
    
    SELECT(1, true, true),
    INSERT(2, false, true),
    UPDATE(3, false, true),
    DELETE(4, false, true),
    DDL(5),
    GET_SEGMENT(6),
    PUT_SEGMENT(7),
    EXEC_PROCEDURE(8, true, true),
    START_TRANS(9),
    COMMIT(10),
    ROLLBACK(11),
    SELECT_FOR_UPD(12),
    SET_GENERATOR(13),
    SAVEPOINT(14);
    
    private static final Map<Integer, FirebirdSQLInfoReturnValue> FIREBIRD_DATABASE_INFO_RETURN_VALUES_CACHE = new HashMap<>();
    
    private final int code;
    
    private final boolean selectDescribable;
    
    private final boolean bindDescribable;
    
    static {
        for (FirebirdSQLInfoReturnValue each : values()) {
            FIREBIRD_DATABASE_INFO_RETURN_VALUES_CACHE.put(each.code, each);
        }
    }
    
    FirebirdSQLInfoReturnValue(final int code) {
        this.code = code;
        selectDescribable = false;
        bindDescribable = false;
    }
    
    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird dpb type
     */
    public static FirebirdSQLInfoReturnValue valueOf(final int code) {
        return FIREBIRD_DATABASE_INFO_RETURN_VALUES_CACHE.get(code);
    }
}
