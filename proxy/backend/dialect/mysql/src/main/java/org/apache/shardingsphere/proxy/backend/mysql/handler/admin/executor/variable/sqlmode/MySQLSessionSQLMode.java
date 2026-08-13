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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.sqlmode;

import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MySQL session SQL mode.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MySQLSessionSQLMode {
    
    /**
     * Default SQL mode.
     */
    public static final String DEFAULT_SQL_MODE = "ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION";
    
    private static final String NO_BACKSLASH_ESCAPES = "NO_BACKSLASH_ESCAPES";
    
    private static final AttributeKey<MySQLSessionSQLMode> ATTRIBUTE_KEY = AttributeKey.valueOf(MySQLSessionSQLMode.class.getName());
    
    private static final MySQLSessionSQLMode DEFAULT = new MySQLSessionSQLMode(DEFAULT_SQL_MODE, false);
    
    private final String value;
    
    private final boolean noBackslashEscapes;
    
    private static MySQLSessionSQLMode create(final String value) {
        if (DEFAULT_SQL_MODE.equalsIgnoreCase(value)) {
            return DEFAULT;
        }
        for (String each : value.split(",")) {
            if (NO_BACKSLASH_ESCAPES.equalsIgnoreCase(each.trim())) {
                return new MySQLSessionSQLMode(value, true);
            }
        }
        return new MySQLSessionSQLMode(value, false);
    }
    
    /**
     * Get MySQL session SQL mode.
     *
     * @param attributeMap attribute map
     * @return MySQL session SQL mode
     */
    public static MySQLSessionSQLMode get(final AttributeMap attributeMap) {
        if (!attributeMap.hasAttr(ATTRIBUTE_KEY)) {
            return DEFAULT;
        }
        MySQLSessionSQLMode result = attributeMap.attr(ATTRIBUTE_KEY).get();
        return null == result ? DEFAULT : result;
    }
    
    /**
     * Set MySQL session SQL mode.
     *
     * @param value SQL mode value
     * @param attributeMap attribute map
     */
    public static void set(final String value, final AttributeMap attributeMap) {
        MySQLSessionSQLMode sqlMode = create(value);
        attributeMap.attr(ATTRIBUTE_KEY).set(DEFAULT == sqlMode ? null : sqlMode);
    }
}
