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

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.cache.SQLStatementCacheBuilder;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * SQL statement parser engine.
 */
public final class SQLStatementParserEngine {
    
    private final SQLStatementParserExecutor sqlStatementParserExecutor;
    
    private final LoadingCache<String, SQLStatement> sqlStatementCache;
    
    @Getter
    private final CacheOption sqlStatementCacheOption;
    
    @Getter
    private final CacheOption parseTreeCacheOption;
    
    @Getter
    private final boolean isParseComment;
    
    public SQLStatementParserEngine(final DatabaseType databaseType, final CacheOption sqlStatementCacheOption, final CacheOption parseTreeCacheOption, final boolean isParseComment) {
        sqlStatementParserExecutor = new SQLStatementParserExecutor(databaseType, parseTreeCacheOption, isParseComment);
        sqlStatementCache = SQLStatementCacheBuilder.build(databaseType, sqlStatementCacheOption, parseTreeCacheOption, isParseComment);
        this.sqlStatementCacheOption = sqlStatementCacheOption;
        this.parseTreeCacheOption = parseTreeCacheOption;
        this.isParseComment = isParseComment;
    }
    
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @param useCache whether to use cache
     * @return SQL statement
     */
    public SQLStatement parse(final String sql, final boolean useCache) {
        return useCache ? sqlStatementCache.get(sql) : sqlStatementParserExecutor.parse(sql);
    }
}
