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

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserExecutor;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * SQL statement cache loader.
 */
public final class SQLStatementCacheLoader implements CacheLoader<String, SQLStatement> {
    
    private final SQLStatementParserExecutor sqlStatementParserExecutor;
    
    public SQLStatementCacheLoader(final DatabaseType databaseType, final CacheOption parseTreeCacheOption) {
        sqlStatementParserExecutor = new SQLStatementParserExecutor(databaseType, parseTreeCacheOption);
    }
    
    /**
     * Update cache option.
     *
     * @param parseTreeCacheOption parse tree cache option
     */
    public void updateCacheOption(final CacheOption parseTreeCacheOption) {
        sqlStatementParserExecutor.updateCacheOption(parseTreeCacheOption);
    }
    
    @Override
    public SQLStatement load(final String sql) {
        return sqlStatementParserExecutor.parse(sql);
    }
}
