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

package org.apache.shardingsphere.sql.parser.engine.api;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.database.cache.ParseTreeCacheBuilder;
import org.apache.shardingsphere.sql.parser.engine.core.database.parser.SQLParserExecutor;

/**
 * SQL parser engine.
 */
public final class SQLParserEngine {
    
    private final SQLParserExecutor sqlParserExecutor;
    
    private final LoadingCache<String, ParseASTNode> parseTreeCache;
    
    public SQLParserEngine(final DatabaseType databaseType, final CacheOption cacheOption) {
        sqlParserExecutor = new SQLParserExecutor(databaseType);
        parseTreeCache = ParseTreeCacheBuilder.build(cacheOption, databaseType);
    }
    
    public SQLParserEngine(final String databaseType, final CacheOption cacheOption) {
        this(TypedSPILoader.getService(DatabaseType.class, databaseType), cacheOption);
    }
    
    /**
     * Update cache option.
     *
     * @param cacheOption cache option
     */
    public void updateCacheOption(final CacheOption cacheOption) {
        parseTreeCache.policy().eviction().ifPresent(eviction -> eviction.setMaximum(cacheOption.getMaximumSize()));
    }
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @param useCache whether to use cache
     * @return parse AST node
     */
    public ParseASTNode parse(final String sql, final boolean useCache) {
        return useCache ? parseTreeCache.get(sql) : sqlParserExecutor.parse(sql);
    }
}
