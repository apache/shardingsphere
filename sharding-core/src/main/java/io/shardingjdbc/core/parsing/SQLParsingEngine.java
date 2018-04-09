/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.cache.ParsingResultCache;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.LexerEngineFactory;
import io.shardingjdbc.core.parsing.parser.sql.SQLParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

/**
 * SQL parsing engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLParsingEngine {
    
    private final DatabaseType dbType;
    
    private final String sql;
    
    private final ShardingRule shardingRule;
    
    /**
     * Parse SQL.
     * 
     * @return parsed SQL statement
     */
    public SQLStatement parse() {
        SQLStatement cachedSQLStatement = ParsingResultCache.getInstance().getSQLStatement(sql);
        if (null != cachedSQLStatement) {
            return cachedSQLStatement;
        }
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(dbType, sql);
        lexerEngine.nextToken();
        SQLStatement result = SQLParserFactory.newInstance(dbType, lexerEngine.getCurrentToken().getType(), shardingRule, lexerEngine).parse();
        // TODO cannot cache InsertStatement here by generate key, should not modify original InsertStatement on router.  
        if (!findGeneratedKeyToken(result)) {
            ParsingResultCache.getInstance().put(sql, result);
        }
        return result;
    }
    
    private boolean findGeneratedKeyToken(final SQLStatement sqlStatement) {
        for (SQLToken each : sqlStatement.getSqlTokens()) {
            if (each instanceof GeneratedKeyToken) {
                return true;
            }
        }
        return false;
    }
}
