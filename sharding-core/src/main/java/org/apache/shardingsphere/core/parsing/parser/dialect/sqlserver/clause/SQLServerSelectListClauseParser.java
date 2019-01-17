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

package org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.clause;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.OrderByClauseParser;
import org.apache.shardingsphere.core.parsing.parser.clause.SelectListClauseParser;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select list clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectListClauseParser extends SelectListClauseParser {
    
    private final OrderByClauseParser orderByClauseParser;
    
    public SQLServerSelectListClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
        orderByClauseParser = new SQLServerOrderByClauseParser(lexerEngine);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBeforeSelectItem() {
        return new Keyword[0];
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
