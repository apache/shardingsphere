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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum FirebirdSQLInfoReturnValue {
    
    SELECT(1),
    INSERT(2),
    UPDATE(3),
    DELETE(4),
    DDL(5),
    GET_SEGMENT(6),
    PUT_SEGMENT(7),
    EXEC_PROCEDURE(8),
    START_TRANS(9),
    COMMIT(10),
    ROLLBACK(11),
    SELECT_FOR_UPD(12),
    SET_GENERATOR(13),
    SAVEPOINT(14);
    
    private static final Map<Integer, FirebirdSQLInfoReturnValue> FIREBIRD_DATABASE_INFO_RETURN_VALUES_CACHE = new HashMap<>();
    
    private static final List<FirebirdSQLInfoReturnValue> FIREBIRD_DESCRIBE_SELECT_OPERATIONS = new ArrayList<>();
    
    private static final List<FirebirdSQLInfoReturnValue> FIREBIRD_DESCRIBE_BIND_OPERATIONS = new ArrayList<>();
    
    private final int code;
    
    static {
        for (FirebirdSQLInfoReturnValue each : values()) {
            FIREBIRD_DATABASE_INFO_RETURN_VALUES_CACHE.put(each.code, each);
        }
        FIREBIRD_DESCRIBE_SELECT_OPERATIONS.add(EXEC_PROCEDURE);
        FIREBIRD_DESCRIBE_SELECT_OPERATIONS.add(SELECT);
        FIREBIRD_DESCRIBE_BIND_OPERATIONS.add(EXEC_PROCEDURE);
        FIREBIRD_DESCRIBE_BIND_OPERATIONS.add(SELECT);
        FIREBIRD_DESCRIBE_BIND_OPERATIONS.add(INSERT);
        FIREBIRD_DESCRIBE_BIND_OPERATIONS.add(UPDATE);
        FIREBIRD_DESCRIBE_BIND_OPERATIONS.add(DELETE);
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
    
    /**
     * Return true if code is select describable.
     *
     * @param code arch type code
     * @return Is select describable
     */
    public static boolean isSelectDescribable(final int code) {
        return FIREBIRD_DESCRIBE_SELECT_OPERATIONS.contains(valueOf(code));
    }
    
    /**
     * Return true if code is bind describable.
     *
     * @param code arch type code
     * @return Is bind describable
     */
    public static boolean isBindDescribable(final int code) {
        return FIREBIRD_DESCRIBE_BIND_OPERATIONS.contains(valueOf(code));
    }
}
