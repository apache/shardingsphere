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

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * SQL hint utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLHintUtils {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere hint:";
    
    private static final String SQL_HINT_SPLIT = ",";
    
    private static final String SQL_HINT_VALUE_SPLIT = "=";
    
    private static final int SQL_HINT_VALUE_SIZE = 2;
    
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
        Collection<String> sqlHints = Splitter.on(SQL_HINT_SPLIT).trimResults().splitToList(comment.substring(startIndex, endIndex).trim());
        for (String each : sqlHints) {
            List<String> hintValues = Splitter.on(SQL_HINT_VALUE_SPLIT).trimResults().splitToList(each);
            if (SQL_HINT_VALUE_SIZE == hintValues.size()) {
                result.put(hintValues.get(0), hintValues.get(1));
            }
        }
        return result;
    }
}
