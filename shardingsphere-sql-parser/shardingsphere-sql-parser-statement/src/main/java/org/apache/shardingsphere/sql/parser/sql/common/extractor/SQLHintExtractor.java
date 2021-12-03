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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * SQL hint extractor.
 */
public final class SQLHintExtractor {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere hint:";
    
    private static final String SQL_HINT_SPLIT = "=";
    
    private static final String SQL_HINT_DATASOURCE_NAME_KEY = "dataSourceName";
    
    private static final String SQL_HINT_WRITE_ROUTE_ONLY_KEY = "writeRouteOnly";
    
    private final Map<String, String> sqlHintMap;
    
    private final Set<String> supportedSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    public SQLHintExtractor(final SQLStatement sqlStatement) {
        supportedSet.add(SQL_HINT_DATASOURCE_NAME_KEY);
        supportedSet.add(SQL_HINT_WRITE_ROUTE_ONLY_KEY);
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
    public Map<String, String> extract(final AbstractSQLStatement statement) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (CommentSegment each : statement.getCommentSegments()) {
            extractFromComment(each.getText(), result);
        }
        return result;
    }
    
    private void extractFromComment(final String comment, final Map<String, String> result) {
        int startIndex = comment.toLowerCase().indexOf(SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return;
        }
        startIndex = startIndex + SQL_HINT_TOKEN.length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        String[] hintValue = comment.substring(startIndex, endIndex).trim().split(SQL_HINT_SPLIT);
        if (2 == hintValue.length && hintValue[0].trim().length() > 0 && hintValue[1].trim().length() > 0 && supportedSet.contains(hintValue[0].trim())) {
            result.put(hintValue[0].trim(), hintValue[1].trim());
        }
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
     * Judge whether is hint routed to write data source.
     *
     * @return whether is hint routed to write data source
     */
    public boolean isHintWriteRouteOnly() {
        return "true".equalsIgnoreCase(sqlHintMap.get(SQL_HINT_WRITE_ROUTE_ONLY_KEY));
    }
}
