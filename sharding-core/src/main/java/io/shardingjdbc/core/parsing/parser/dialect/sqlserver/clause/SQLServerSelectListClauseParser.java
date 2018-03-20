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

package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingjdbc.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.rule.ShardingRule;

/**
 * Select list clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectListClauseParser extends SelectListClauseParser {
    
    private OrderByClauseParser orderByClauseParser;
    
    public SQLServerSelectListClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
        orderByClauseParser = new SQLServerOrderByClauseParser(lexerEngine);
    }
    
    @Override
    protected boolean isRowNumberSelectItem() {
        return getLexerEngine().skipIfEqual(SQLServerKeyword.ROW_NUMBER);
    }
    
    @Override
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        getLexerEngine().skipParentheses(selectStatement);
        getLexerEngine().accept(DefaultKeyword.OVER);
        getLexerEngine().accept(Symbol.LEFT_PAREN);
        getLexerEngine().unsupportedIfEqual(SQLServerKeyword.PARTITION);
        orderByClauseParser.parse(selectStatement);
        getLexerEngine().accept(Symbol.RIGHT_PAREN);
        return new CommonSelectItem(SQLServerKeyword.ROW_NUMBER.name(), getAliasExpressionParser().parseSelectItemAlias());
    }
}
