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

package org.apache.shardingsphere.infra.parser.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * SQL statement cache builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementCacheBuilder {
    
    /**
     * Build SQL statement cache.
     *
     * @param sqlStatementCacheOption SQL statement cache option
     * @param parseTreeCacheOption parse tree cache option
     * @param databaseType database type
     * @return built SQL statement cache
     */
    public static CacheManager<String, SQLStatement> build(final DatabaseType databaseType, final CacheOption sqlStatementCacheOption,
                                                           final CacheOption parseTreeCacheOption) {
        SQLStatementCacheLoader sqlStatementCacheLoader = new SQLStatementCacheLoader(databaseType, parseTreeCacheOption);
        LoadingCache<String, SQLStatement> loadingCache =
                Caffeine.newBuilder().softValues().initialCapacity(sqlStatementCacheOption.getInitialCapacity()).maximumSize(sqlStatementCacheOption.getMaximumSize()).build(sqlStatementCacheLoader);
        return new CacheManager<>(loadingCache, sqlStatementCacheLoader);
    }
}
