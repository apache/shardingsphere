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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.Optional;

/**
 * SQL hint extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLHintExtractor {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere hint:";
    
    private static final String SQL_HINT_SPLIT = "=";
    
    private static final String SQL_HINT_DATASOURCE_NAME_KEY = "datasourcename";
    
    /**
     * Find hint data source name.
     *
     * @param statement statement
     * @return data source name
     */
    public static Optional<String> findHintDataSourceName(final AbstractSQLStatement statement) {
        for (CommentSegment each : statement.getCommentSegments()) {
            Optional<String> result = findDataSourceNameFromComment(each.getText());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private static Optional<String> findDataSourceNameFromComment(final String comment) {
        int startIndex = comment.toLowerCase().indexOf(SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return Optional.empty();
        }
        startIndex = startIndex + SQL_HINT_TOKEN.length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        String[] hintValue = comment.substring(startIndex, endIndex).trim().split(SQL_HINT_SPLIT);
        if (2 == hintValue.length && SQL_HINT_DATASOURCE_NAME_KEY.equalsIgnoreCase(hintValue[0].trim()) && hintValue[1].trim().length() > 0) {
            return Optional.of(hintValue[1].trim());
        }
        return Optional.empty();
    }
}
