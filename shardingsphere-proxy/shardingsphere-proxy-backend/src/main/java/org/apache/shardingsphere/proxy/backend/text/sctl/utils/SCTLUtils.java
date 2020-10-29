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

package org.apache.shardingsphere.proxy.backend.text.sctl.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SCTL utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SCTLUtils {
    
    private static final String COMMENT_PREFIX = "/*";
    
    private static final String COMMENT_SUFFIX = "*/";
    
    private static final String SQL_END = ";";
    
    /**
     * Trim the comment of sql.
     *
     * @param sql SQL to be trim
     * @return remove comment from SQL
     */
    public static String trimComment(final String sql) {
        String result = sql;
        if (sql.startsWith(COMMENT_PREFIX)) {
            result = result.substring(sql.indexOf(COMMENT_SUFFIX) + 2);
        }
        if (sql.endsWith(SQL_END)) {
            result = result.substring(0, result.length() - 1);
        }
        return result.trim();
    }
}
