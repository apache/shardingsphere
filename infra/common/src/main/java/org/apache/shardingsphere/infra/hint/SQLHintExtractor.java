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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * SQL hint extractor.
 */
@Getter
public final class SQLHintExtractor {
    
    private final HintValueContext hintValueContext;
    
    public SQLHintExtractor(final String sqlComment) {
        hintValueContext = Strings.isNullOrEmpty(sqlComment) ? new HintValueContext() : SQLHintUtils.extractHint(sqlComment);
    }
    
    public SQLHintExtractor(final SQLStatement sqlStatement) {
        this(sqlStatement, new HintValueContext());
    }
    
    public SQLHintExtractor(final SQLStatement sqlStatement, final HintValueContext hintValueContext) {
        this.hintValueContext = sqlStatement instanceof AbstractSQLStatement && !((AbstractSQLStatement) sqlStatement).getCommentSegments().isEmpty()
                ? SQLHintUtils.extractHint(((AbstractSQLStatement) sqlStatement).getCommentSegments().iterator().next().getText())
                : hintValueContext;
    }
    
    /**
     * Find hint data source name.
     *
     * @return data source name
     */
    public Optional<String> findHintDataSourceName() {
        String result = hintValueContext.getDataSourceName();
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    /**
     * Judge whether is hint routed to write data source or not.
     *
     * @return whether is hint routed to write data source or not
     */
    public boolean isHintWriteRouteOnly() {
        return hintValueContext.isWriteRouteOnly();
    }
    
    /**
     * Judge whether hint skip encrypt rewrite or not.
     *
     * @return whether hint skip encrypt rewrite or not
     */
    public boolean isHintSkipEncryptRewrite() {
        return hintValueContext.isSkipEncryptRewrite();
    }
    
    /**
     * Judge whether is hint routed to shadow data source or not.
     *
     * @return whether is hint routed to shadow data source or not
     */
    public boolean isShadow() {
        return hintValueContext.isShadow();
    }
    
    /**
     * Find hint disable audit names.
     *
     * @return disable audit names
     */
    public Collection<String> findDisableAuditNames() {
        return SQLHintUtils.getSplitterSQLHintValue(hintValueContext.getDisableAuditNames());
    }
    
    /**
     * Get hint sharding database value.
     *
     * @param tableName table name
     * @return sharding database value
     */
    public Collection<Comparable<?>> getHintShardingDatabaseValue(final String tableName) {
        String key = String.join(".", tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
        return hintValueContext.getShardingDatabaseValues().containsKey(key)
                ? hintValueContext.getShardingDatabaseValues().get(key)
                : hintValueContext.getShardingDatabaseValues().get(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
    }
    
    /**
     * Judge contains hint sharding databases value or not.
     *
     * @param tableName table name
     * @return contains hint sharding databases value or not
     */
    public boolean containsHintShardingDatabaseValue(final String tableName) {
        String key = Joiner.on(".").join(tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
        return hintValueContext.getShardingDatabaseValues().containsKey(key) || hintValueContext.getShardingDatabaseValues().containsKey(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
    }
    
    /**
     * Get hint sharding table value.
     *
     * @param tableName table name
     * @return sharding table value
     */
    public Collection<Comparable<?>> getHintShardingTableValue(final String tableName) {
        String key = String.join(".", tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
        return hintValueContext.getShardingTableValues().containsKey(key)
                ? hintValueContext.getShardingTableValues().get(key)
                : hintValueContext.getShardingTableValues().get(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
    }
    
    /**
     * Judge contains hint sharding table value or not.
     *
     * @param tableName table name
     * @return Contains hint sharding table value or not
     */
    public boolean containsHintShardingTableValue(final String tableName) {
        String key = Joiner.on(".").join(tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
        return hintValueContext.getShardingTableValues().containsKey(key) || hintValueContext.getShardingTableValues().containsKey(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
    }
    
    /**
     * Judge contains hint sharding value or not.
     *
     * @param tableName table name
     * @return Contains hint sharding value or not
     */
    public boolean containsHintShardingValue(final String tableName) {
        return containsHintShardingDatabaseValue(tableName) || containsHintShardingTableValue(tableName);
    }
}
