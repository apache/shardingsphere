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

package io.shardingjdbc.core.parsing.parser.dialect.postgresql.sql;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.sql.ddl.drop.AbstractDropParser;

/**
 * Drop parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLDropParser extends AbstractDropParser {
    
    public PostgreSQLDropParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    protected Keyword[] getSkippedKeywordsBetweenDropIndexAndIndexName() {
        return new Keyword[] {DefaultKeyword.IF, DefaultKeyword.EXISTS};
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenDropTableAndTableName() {
        return new Keyword[] {DefaultKeyword.IF, DefaultKeyword.EXISTS};
    }
}
