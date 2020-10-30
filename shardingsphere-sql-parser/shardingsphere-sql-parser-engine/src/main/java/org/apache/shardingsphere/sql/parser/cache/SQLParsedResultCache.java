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

import java.util.Optional;

/**
 * SQL parsed result cache.
 * 
 * @param <T> type of parsed result
 */
public final class SQLParsedResultCache<T> {
    
    private final Cache<String, T> cache = CacheBuilder.newBuilder().softValues().initialCapacity(2000).maximumSize(65535).build();
    
    /**
     * Put SQL and parsed result into cache.
     *
     * @param sql SQL
     * @param parsedResult parsed result
     */
    public void put(final String sql, final T parsedResult) {
        cache.put(sql, parsedResult);
    }
    
    /**
     * Get parsed result.
     *
     * @param sql SQL
     * @return parsed result
     */
    public Optional<T> get(final String sql) {
        return Optional.ofNullable(cache.getIfPresent(sql));
    }
}
