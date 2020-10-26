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

import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL Parsed result caches.
 */
public final class SQLParsedResultCaches {
    
    private final Map<SQLVisitorType, SQLParsedResultCache> cacheMap = new ConcurrentHashMap<>();
    
    /**
     * Get SQL parsed result cache.
     *
     * @param type  type
     * @return SQL parsed result cache
     */
    public SQLParsedResultCache getCache(final SQLVisitorType type) {
        if (cacheMap.containsKey(type)) {
            return cacheMap.get(type);
        }
        synchronized (cacheMap) {
            if (cacheMap.containsKey(type)) {
                return cacheMap.get(type);
            }
            SQLParsedResultCache result = new SQLParsedResultCache<>();
            cacheMap.put(type, result);
            return result;
        }
    }
}
