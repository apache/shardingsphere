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
import io.shardingjdbc.core.parsing.lexer.token.Literals;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.clause.SQLClauseParser;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import lombok.RequiredArgsConstructor;

/**
 * Offset clause parser for SQLServer.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLServerOffsetClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse offset.
     * 
     * @param selectStatement select statement
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(SQLServerKeyword.OFFSET)) {
            return;
        }
        int offsetValue = -1;
        int offsetIndex = -1;
        if (lexerEngine.equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(lexerEngine.getCurrentToken().getLiterals());
        } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
            offsetIndex = selectStatement.getParametersIndex();
            selectStatement.increaseParametersIndex();
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        lexerEngine.nextToken();
        Limit limit = new Limit(true);
        if (lexerEngine.skipIfEqual(DefaultKeyword.FETCH)) {
            lexerEngine.nextToken();
            int rowCountValue = -1;
            int rowCountIndex = -1;
            lexerEngine.nextToken();
            if (lexerEngine.equalAny(Literals.INT)) {
                rowCountValue = Integer.parseInt(lexerEngine.getCurrentToken().getLiterals());
            } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
                rowCountIndex = selectStatement.getParametersIndex();
                selectStatement.increaseParametersIndex();
            } else {
                throw new SQLParsingException(lexerEngine);
            }
            lexerEngine.nextToken();
            lexerEngine.nextToken();
            limit.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        } else {
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        }
        selectStatement.setLimit(limit);
    }
}
