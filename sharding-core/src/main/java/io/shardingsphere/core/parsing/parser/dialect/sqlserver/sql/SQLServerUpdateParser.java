/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.parser.dialect.sqlserver.sql;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.facade.SQLServerUpdateClauseParserFacade;
import io.shardingsphere.core.parsing.parser.sql.dml.update.AbstractUpdateParser;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Update parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerUpdateParser extends AbstractUpdateParser {
    
    public SQLServerUpdateParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new SQLServerUpdateClauseParserFacade(shardingRule, lexerEngine));
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordsBetweenUpdateAndTable() {
        return new Keyword[] {SQLServerKeyword.TOP};
    }
}
