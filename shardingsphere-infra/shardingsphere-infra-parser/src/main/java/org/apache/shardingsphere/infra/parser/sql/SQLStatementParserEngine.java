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

import com.google.common.cache.LoadingCache;
import org.apache.shardingsphere.infra.parser.cache.SQLStatementCacheBuilder;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * SQL statement parser engine.
 */
public final class SQLStatementParserEngine {
    
    private final SQLStatementParserExecutor sqlStatementParserExecutor;
    
    private final LoadingCache<String, SQLStatement> sqlStatementCache;
    
    public SQLStatementParserEngine(final String databaseType) {
        sqlStatementParserExecutor = new SQLStatementParserExecutor(databaseType);
        // TODO use props to configure cache option
        sqlStatementCache = SQLStatementCacheBuilder.build(new CacheOption(2000, 65535L, 4), databaseType);
    }
    
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @return SQL statement
     */
    public SQLStatement parse(final String sql, final boolean useCache) {
        return useCache ? sqlStatementCache.getUnchecked(sql) : sqlStatementParserExecutor.parse(sql);
    }
}
