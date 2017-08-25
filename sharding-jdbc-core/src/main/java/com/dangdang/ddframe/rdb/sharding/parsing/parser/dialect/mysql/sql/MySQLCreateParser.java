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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.sql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.ddl.create.AbstractCreateParser;

/**
 * Create parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLCreateParser extends AbstractCreateParser {
    
    public MySQLCreateParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenCreateAndKeyword() {
        return new Keyword[] {DefaultKeyword.TEMPORARY};
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenCreateTableAndTableName() {
        return new Keyword[] {DefaultKeyword.IF, DefaultKeyword.NOT, DefaultKeyword.EXISTS};
    }
}
