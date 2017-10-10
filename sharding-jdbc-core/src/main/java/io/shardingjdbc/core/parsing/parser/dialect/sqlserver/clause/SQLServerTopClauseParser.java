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

package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.ExpressionClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SQLClauseParser;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;

/**
 * Top clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerTopClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public SQLServerTopClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * Parse top.
     * 
     * @param selectStatement select statement
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(SQLServerKeyword.TOP)) {
            return;
        }
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        if (!lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
            beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        }
        SQLExpression sqlExpression = expressionClauseParser.parse(selectStatement);
        lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
        LimitValue rowCountValue;
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            rowCountValue = new LimitValue(rowCount, -1);
            selectStatement.getSqlTokens().add(new RowCountToken(beginPosition, rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            rowCountValue = new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        lexerEngine.unsupportedIfEqual(SQLServerKeyword.PERCENT);
        lexerEngine.skipIfEqual(DefaultKeyword.WITH, SQLServerKeyword.TIES);
        if (null == selectStatement.getLimit()) {
            Limit limit = new Limit(false);
            limit.setRowCount(rowCountValue);
            selectStatement.setLimit(limit);
        } else {
            selectStatement.getLimit().setRowCount(rowCountValue);
        }
    }
}
