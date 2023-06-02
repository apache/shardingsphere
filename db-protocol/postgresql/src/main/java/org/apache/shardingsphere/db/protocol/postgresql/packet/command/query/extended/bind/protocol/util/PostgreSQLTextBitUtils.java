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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Text bit utility class of PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostgreSQLTextBitUtils {
    
    /**
     * Get bit Text value in PostgreSQL text format.
     * 
     * @param jdbcBitValue bit value for jdbc
     * @return bit text value in PostgreSQL text format
     */
    public static String getTextValue(final Object jdbcBitValue) {
        if (null == jdbcBitValue) {
            return null;
        }
        return (Boolean) jdbcBitValue ? "1" : "0";
    }
}
