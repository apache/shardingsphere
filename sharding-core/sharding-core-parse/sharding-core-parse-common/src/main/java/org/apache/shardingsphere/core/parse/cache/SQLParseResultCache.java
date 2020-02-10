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

package org.apache.shardingsphere.core.parse.cache;

import com.google.common.base.Optional;
import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Map;

/**
 * SQL parse result cache.
 *
 * @author zhangliang
 * @author zhaojun
 */
public final class SQLParseResultCache {
    
    private final Map<String, SQLStatement> cache = new ReferenceMap<>(AbstractReferenceMap.ReferenceStrength.SOFT, AbstractReferenceMap.ReferenceStrength.SOFT, 65535, 1);
    
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
        return Optional.fromNullable(cache.get(sql));
    }
    
    /**
     * Clear cache.
     */
    public synchronized void clear() {
        cache.clear();
    }
}
