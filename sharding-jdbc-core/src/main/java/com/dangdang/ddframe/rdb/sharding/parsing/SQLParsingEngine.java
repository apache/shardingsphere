/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngineFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLParserFactory;
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
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(dbType, sql);
        lexerEngine.nextToken();
        return SQLParserFactory.newInstance(dbType, lexerEngine.getCurrentToken().getType(), shardingRule, lexerEngine).parse();
    }
}
