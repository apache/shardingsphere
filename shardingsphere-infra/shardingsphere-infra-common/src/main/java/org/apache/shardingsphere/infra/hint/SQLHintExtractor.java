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
    
    private final SQLHintProperties sqlHintProperties;
    
    public SQLHintExtractor(final SQLStatement sqlStatement) {
        sqlHintProperties = sqlStatement instanceof AbstractSQLStatement ? extract((AbstractSQLStatement) sqlStatement) : new SQLHintProperties(new Properties());
    }
    
    private SQLHintProperties extract(final AbstractSQLStatement statement) {
        Properties properties = new Properties();
        for (CommentSegment each : statement.getCommentSegments()) {
            properties.putAll(SQLHintUtils.getSQLHintProps(each.getText()));
        }
        return new SQLHintProperties(properties);
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
