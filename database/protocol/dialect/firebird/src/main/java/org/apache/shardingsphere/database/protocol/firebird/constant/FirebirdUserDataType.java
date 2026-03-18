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

package org.apache.shardingsphere.database.protocol.firebird.constant;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird user data type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdUserDataType {
    
    CNCT_USER(1),
    CNCT_PASSWD(2),
    CNCT_HOST(4),
    CNCT_GROUP(5),
    CNCT_USER_VERIFICATION(6),
    CNCT_SPECIFIC_DATA(7),
    CNCT_PLUGIN_NAME(8),
    CNCT_LOGIN(9),
    CNCT_PLUGIN_LIST(10),
    CNCT_CLIENT_CRYPT(11);
    
    private static final Map<Integer, FirebirdUserDataType> FIREBIRD_USER_DATA_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    static {
        for (FirebirdUserDataType each : values()) {
            FIREBIRD_USER_DATA_TYPE_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Value of.
     *
     * @param code user data type code
     * @return Firebird user data type
     */
    public static FirebirdUserDataType valueOf(final int code) {
        FirebirdUserDataType result = FIREBIRD_USER_DATA_TYPE_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find '%d' in user data type", code);
        return result;
    }
}
