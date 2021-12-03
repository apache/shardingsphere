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

package org.apache.shardingsphere.sql.parser.sql.common.extractor;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SQL hint extractor.
 */
public final class SQLHintExtractor {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere hint:";
    
    private static final String SQL_HINT_SPLIT = "=";
    
    private static final String SQL_HINT_DATASOURCE_NAME_KEY = "datasourcename";
    
    private static final String SQL_HINT_WRITE_ROUTE_ONLY_KEY = "writerouteonly";
    
    private final Map<String, String> sqlHintMap;
    
    public SQLHintExtractor(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof AbstractSQLStatement) {
            sqlHintMap = extract((AbstractSQLStatement) sqlStatement);
        } else {
            sqlHintMap = Collections.emptyMap();
        }
    }
    
    /**
     * Extract from statement.
     *
     * @param statement statement
     * @return sql hint map
     */
    public static Map<String, String> extract(final AbstractSQLStatement statement) {
        for (CommentSegment each : statement.getCommentSegments()) {
            Optional<Map<String, String>> map = extractFromComment(each.getText());
            if (map.isPresent()) {
                return map.get();
            }
        }
        return Collections.emptyMap();
    }
    
    private static Optional<Map<String, String>> extractFromComment(final String comment) {
        int startIndex = comment.toLowerCase().indexOf(SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return Optional.empty();
        }
        startIndex = startIndex + SQL_HINT_TOKEN.length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        String[] hintValue = comment.substring(startIndex, endIndex).trim().split(SQL_HINT_SPLIT);
        if (2 == hintValue.length && hintValue[0].trim().length() > 0 && hintValue[1].trim().length() > 0) {
            Map<String, String> result = new HashMap<>(1, 1);
            result.put(hintValue[0].trim().toLowerCase(), hintValue[1].trim());
            return Optional.of(result);
        }
        return Optional.empty();
    }
    
    /**
     * Find hint data source name.
     *
     * @return data source name
     */
    public Optional<String> findHintDataSourceName() {
        return Optional.ofNullable(sqlHintMap.get(SQL_HINT_DATASOURCE_NAME_KEY));
    }
    
    /**
     * Is hint write route only.
     *
     * @return boolean
     */
    public boolean isHintWriteRouteOnly() {
        return "true".equals(sqlHintMap.get(SQL_HINT_WRITE_ROUTE_ONLY_KEY));
    }
}
