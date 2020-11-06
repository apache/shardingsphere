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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * SQL statement parser engine.
 */
public final class SQLStatementParserEngine {
    
    private final SQLParserEngine parserEngine;
    
    private final SQLVisitorEngine visitorEngine;
    
    private final Cache<String, SQLStatement> cache = CacheBuilder.newBuilder().softValues().initialCapacity(2000).maximumSize(65535).build();
    
    public SQLStatementParserEngine(final String databaseTypeName) {
        parserEngine = new SQLParserEngine(databaseTypeName);
        visitorEngine = new SQLVisitorEngine(databaseTypeName, "STATEMENT");
    }
    
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @return SQL statement
     */
    public SQLStatement parse(final String sql, final boolean useCache) {
        if (!useCache) {
            return parse(sql);
        }
        Optional<SQLStatement> statement = Optional.ofNullable(cache.getIfPresent(sql));
        if (statement.isPresent()) {
            return statement.get();
        }
        SQLStatement result = parse(sql);
        cache.put(sql, result);
        return result;
    }
    
    private SQLStatement parse(final String sql) {
        return visitorEngine.visit(parserEngine.parse(sql, false));
    }
}
