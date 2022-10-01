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

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Properties;

/**
 * SQL hint extractor.
 */
@Getter
public final class SQLHintExtractor {
    
    private static final SQLHintProperties DEFAULT_SQL_HINT_PROPERTIES = new SQLHintProperties(new Properties());
    
    private final SQLHintProperties sqlHintProperties;
    
    public SQLHintExtractor(final SQLStatement sqlStatement) {
        sqlHintProperties = sqlStatement instanceof AbstractSQLStatement && !((AbstractSQLStatement) sqlStatement).getCommentSegments().isEmpty()
                ? extract((AbstractSQLStatement) sqlStatement)
                : DEFAULT_SQL_HINT_PROPERTIES;
    }
    
    private SQLHintProperties extract(final AbstractSQLStatement statement) {
        Properties props = new Properties();
        for (CommentSegment each : statement.getCommentSegments()) {
            props.putAll(SQLHintUtils.getSQLHintProps(each.getText()));
        }
        return new SQLHintProperties(props);
    }
    
    /**
     * Judge whether is hint routed to write data source or not.
     *
     * @return whether is hint routed to write data source or not
     */
    public boolean isHintWriteRouteOnly() {
        return sqlHintProperties.getValue(SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY);
    }
    
    /**
     * Judge whether hint skip encrypt rewrite or not.
     *
     * @return whether hint skip encrypt rewrite or not
     */
    public boolean isHintSkipEncryptRewrite() {
        return sqlHintProperties.getValue(SQLHintPropertiesKey.SKIP_ENCRYPT_REWRITE_KEY);
    }
    
    /**
     * Find hint disable audit names.
     *
     * @return disable audit names
     */
    public Collection<String> findDisableAuditNames() {
        return SQLHintUtils.getSplitterSQLHintValue(sqlHintProperties.getValue(SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY));
    }
    
    /**
     * Get hint sharding database value.
     *
     * @return sharding database value
     */
    public int getHintShardingDatabaseValue() {
        return sqlHintProperties.getValue(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY);
    }
    
    /**
     * Get hint sharding database value.
     *
     * @param tableName table name
     * @return sharding database value
     */
    public int getHintShardingDatabaseValue(final String tableName) {
        String key = String.join(".", tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
        return sqlHintProperties.getProps().containsKey(key)
                ? Integer.parseInt(sqlHintProperties.getProps().getProperty(key))
                : sqlHintProperties.getValue(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY);
    }
    
    /**
     * Get hint sharding table value.
     *
     * @return sharding table value
     */
    public int getHintShardingTableValue() {
        return sqlHintProperties.getValue(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY);
    }
    
    /**
     * Get hint sharding table value.
     *
     * @param tableName table name
     * @return sharding table value
     */
    public int getHintShardingTableValue(final String tableName) {
        String key = String.join(".", tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
        return sqlHintProperties.getProps().containsKey(key)
                ? Integer.parseInt(sqlHintProperties.getProps().getProperty(key))
                : sqlHintProperties.getValue(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY);
    }
}
