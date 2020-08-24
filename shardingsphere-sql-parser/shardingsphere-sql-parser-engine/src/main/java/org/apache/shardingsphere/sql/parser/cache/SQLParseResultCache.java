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

package org.apache.shardingsphere.sql.parser.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.util.Optional;

/**
 * SQL parse result cache.
 */
public final class SQLParseResultCache {
    
    private final Cache<String, SQLStatement> cache = CacheBuilder.newBuilder().softValues().initialCapacity(2000).maximumSize(65535).build();
    
    /**
     * Put SQL and parse result into cache.
     * 
     * @param sql SQL
     * @param sqlStatement SQL statement
     */
    public void put(final String sql, final SQLStatement sqlStatement) {
        cache.put(sql, sqlStatement);
    }
    
    /**
     * Get SQL statement.
     *
     * @param sql SQL
     * @return SQL statement
     */
    public Optional<SQLStatement> getSQLStatement(final String sql) {
        return Optional.ofNullable(cache.getIfPresent(sql));
    }
    
    /**
     * Clear cache.
     */
    public synchronized void clear() {
        cache.invalidateAll();
    }
}
