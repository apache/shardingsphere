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

package org.apache.shardingsphere.core.parsing;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.cache.ParsingResultCache;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngineFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * SQL parsing engine.
 *
 * @author zhangliang
 */
public final class SQLParsingEngine {
    
    private final DatabaseType dbType;
    
    private final String sql;
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final ParsingResultCache parsingResultCache;
    
    public SQLParsingEngine(final DatabaseType dbType, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        this.dbType = dbType;
        this.sql = sql;
        this.shardingRule = shardingRule;
        this.shardingTableMetaData = shardingTableMetaData;
        parsingResultCache = shardingRule.getParsingResultCache();
    }
    
    /**
     * Parse SQL.
     *
     * @param useCache use cache or not
     * @return parsed SQL statement
     */
    public SQLStatement parse(final boolean useCache) {
        Optional<SQLStatement> cachedSQLStatement = getSQLStatementFromCache(useCache);
        if (cachedSQLStatement.isPresent()) {
            return cachedSQLStatement.get();
        }
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(dbType, sql);
        SQLStatement result = SQLParserFactory.newInstance(dbType, shardingRule, lexerEngine, shardingTableMetaData, sql).parse();
        if (result instanceof AbstractSQLStatement) {
            ((AbstractSQLStatement) result).setLogicSQL(sql);
        }
        if (useCache) {
            parsingResultCache.put(sql, result);
        }
        return result;
    }
    
    private Optional<SQLStatement> getSQLStatementFromCache(final boolean useCache) {
        return useCache ? Optional.fromNullable(parsingResultCache.getSQLStatement(sql)) : Optional.<SQLStatement>absent();
    }
}
