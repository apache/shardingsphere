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

package org.apache.shardingsphere.infra.parser.sql;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.cache.CacheManager;
import org.apache.shardingsphere.infra.parser.cache.SQLStatementCacheBuilder;
import org.apache.shardingsphere.infra.parser.cache.SQLStatementCacheLoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * SQL statement parser engine.
 */
public final class SQLStatementParserEngine {
    
    private final SQLStatementParserExecutor sqlStatementParserExecutor;
    
    private final CacheManager<String, SQLStatement> sqlStatementCacheManager;
    
    @Getter
    private final CacheOption sqlStatementCacheOption;
    
    @Getter
    private final CacheOption parseTreeCacheOption;
    
    public SQLStatementParserEngine(final DatabaseType databaseType, final CacheOption sqlStatementCacheOption, final CacheOption parseTreeCacheOption) {
        sqlStatementParserExecutor = new SQLStatementParserExecutor(databaseType, parseTreeCacheOption);
        sqlStatementCacheManager = SQLStatementCacheBuilder.build(databaseType, sqlStatementCacheOption, parseTreeCacheOption);
        this.sqlStatementCacheOption = sqlStatementCacheOption;
        this.parseTreeCacheOption = parseTreeCacheOption;
    }
    
    /**
     * Update cache option.
     *
     * @param sqlStatementCacheOption SQL statement cache option
     * @param parseTreeCacheOption parse tree cache option
     */
    public void updateCacheOption(final CacheOption sqlStatementCacheOption, final CacheOption parseTreeCacheOption) {
        sqlStatementCacheManager.getCache().policy().eviction().ifPresent(eviction -> eviction.setMaximum(sqlStatementCacheOption.getMaximumSize()));
        ((SQLStatementCacheLoader) sqlStatementCacheManager.getCacheLoader()).updateCacheOption(parseTreeCacheOption);
        sqlStatementParserExecutor.updateCacheOption(parseTreeCacheOption);
    }
    
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @param useCache whether to use cache
     * @return SQL statement
     */
    public SQLStatement parse(final String sql, final boolean useCache) {
        return useCache ? sqlStatementCacheManager.getCache().get(sql) : sqlStatementParserExecutor.parse(sql);
    }
}
