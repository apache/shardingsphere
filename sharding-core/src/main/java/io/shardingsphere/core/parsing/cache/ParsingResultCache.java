/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.cache;

import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Parsing result cache.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ParsingResultCache {
    
    private static final ParsingResultCache INSTANCE = new ParsingResultCache();
    
    private final Map<String, SQLStatement> cache = new WeakHashMap<>(65535, 1);
    
    /**
     * Get parsing result cache instance.
     * 
     * @return parsing result cache instance
     */
    public static ParsingResultCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put SQL and parsing result into cache.
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
    public SQLStatement getSQLStatement(final String sql) {
        return cache.get(sql);
    }
    
    /**
     * Clear cache.
     */
    public synchronized void clear() {
        cache.clear();
    }
}
