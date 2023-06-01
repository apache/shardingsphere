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

package org.apache.shardingsphere.data.pipeline.postgresql.ddlgenerator;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Column type for PostgreSQL.
 */
@RequiredArgsConstructor
public enum PostgreSQLColumnType {
    
    NUMERIC(new Long[]{1231L, 1700L}),
    
    DATE(new Long[]{1083L, 1114L, 1115L, 1183L, 1184L, 1185L, 1186L, 1187L, 1266L, 1270L}),
    
    VARCHAR(new Long[]{1560L, 1561L, 1562L, 1563L, 1042L, 1043L, 1014L, 1015L}),
    
    UNKNOWN(new Long[]{});
    
    private final Long[] values;
    
    /**
     * Get value of column type.
     * 
     * @param elemoid elemoid
     * @return value of column type
     */
    public static PostgreSQLColumnType valueOf(final Long elemoid) {
        if (0 == elemoid) {
            return UNKNOWN;
        }
        for (PostgreSQLColumnType each : values()) {
            if (Arrays.asList(each.values).contains(elemoid)) {
                return each;
            }
        }
        return UNKNOWN;
    }
}
