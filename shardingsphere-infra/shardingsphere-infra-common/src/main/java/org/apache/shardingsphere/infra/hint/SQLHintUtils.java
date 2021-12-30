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

package org.apache.shardingsphere.infra.hint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 * SQL hint utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLHintUtils {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere hint:";
    
    private static final String SQL_HINT_SPLIT = "=";
    
    /**
     * Get SQL hint props.
     *
     * @param comment SQL comment
     * @return SQL hint props
     */
    public static Properties getSQLHintProps(final String comment) {
        Properties result = new Properties();
        int startIndex = comment.toLowerCase().indexOf(SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return result;
        }
        startIndex = startIndex + SQL_HINT_TOKEN.length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        String[] hintValue = comment.substring(startIndex, endIndex).trim().split(SQL_HINT_SPLIT);
        if (2 == hintValue.length && hintValue[0].trim().length() > 0 && hintValue[1].trim().length() > 0) {
            result.put(hintValue[0].trim(), hintValue[1].trim());
        }
        return result;
    }
}
