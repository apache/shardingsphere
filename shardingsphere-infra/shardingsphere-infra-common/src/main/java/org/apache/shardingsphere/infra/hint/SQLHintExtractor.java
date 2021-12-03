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

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;
import java.util.Properties;

/**
 * SQL hint extractor.
 */
public final class SQLHintExtractor {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere hint:";
    
    private static final String SQL_HINT_SPLIT = "=";
    
    private final SQLHintProperties sqlHintProperties;
    
    public SQLHintExtractor(final SQLStatement sqlStatement) {
        sqlHintProperties = sqlStatement instanceof AbstractSQLStatement ? extract((AbstractSQLStatement) sqlStatement) : new SQLHintProperties(new Properties());
    }
    
    /**
     * Extract from statement.
     *
     * @param statement statement
     * @return sql hint properties
     */
    public SQLHintProperties extract(final AbstractSQLStatement statement) {
        Properties properties = new Properties();
        for (CommentSegment each : statement.getCommentSegments()) {
            appendHintProperties(each.getText(), properties);
        }
        return new SQLHintProperties(properties);
    }
    
    private void appendHintProperties(final String comment, final Properties properties) {
        int startIndex = comment.toLowerCase().indexOf(SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return;
        }
        startIndex = startIndex + SQL_HINT_TOKEN.length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        String[] hintValue = comment.substring(startIndex, endIndex).trim().split(SQL_HINT_SPLIT);
        if (2 == hintValue.length && hintValue[0].trim().length() > 0 && hintValue[1].trim().length() > 0) {
            if (SQLHintPropertiesKey.DATASOURCE_NAME_KEY.getKey().equalsIgnoreCase(hintValue[0].trim())) {
                properties.setProperty(SQLHintPropertiesKey.DATASOURCE_NAME_KEY.getKey(), hintValue[1].trim());
            }
            if (SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY.getKey().equalsIgnoreCase(hintValue[0].trim())) {
                properties.setProperty(SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY.getKey(), hintValue[1].trim());
            }
        }
    }
    
    /**
     * Find hint data source name.
     *
     * @return data source name
     */
    public Optional<String> findHintDataSourceName() {
        String result = sqlHintProperties.getValue(SQLHintPropertiesKey.DATASOURCE_NAME_KEY);
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    /**
     * Judge whether is hint routed to write data source or not.
     *
     * @return whether is hint routed to write data source or not
     */
    public boolean isHintWriteRouteOnly() {
        return sqlHintProperties.getValue(SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY);
    }
}
