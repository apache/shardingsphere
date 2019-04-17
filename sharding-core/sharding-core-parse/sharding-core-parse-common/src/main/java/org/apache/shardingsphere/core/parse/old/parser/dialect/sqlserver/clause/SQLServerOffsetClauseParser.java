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

package org.apache.shardingsphere.core.parse.old.parser.dialect.sqlserver.clause;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.old.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.old.lexer.dialect.sqlserver.SQLServerKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.Literals;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.clause.SQLClauseParser;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingException;

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
            selectStatement.setParametersIndex(selectStatement.getParametersIndex() + 1);
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        lexerEngine.nextToken();
        Limit limit = new Limit();
        if (lexerEngine.skipIfEqual(DefaultKeyword.FETCH)) {
            lexerEngine.nextToken();
            int rowCountValue = -1;
            int rowCountIndex = -1;
            lexerEngine.nextToken();
            if (lexerEngine.equalAny(Literals.INT)) {
                rowCountValue = Integer.parseInt(lexerEngine.getCurrentToken().getLiterals());
            } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
                rowCountIndex = selectStatement.getParametersIndex();
                selectStatement.setParametersIndex(selectStatement.getParametersIndex() + 1);
            } else {
                throw new SQLParsingException(lexerEngine);
            }
            lexerEngine.nextToken();
            lexerEngine.nextToken();
            limit.setRowCount(new LimitValue(rowCountValue, rowCountIndex, false));
            limit.setOffset(new LimitValue(offsetValue, offsetIndex, true));
        } else {
            limit.setOffset(new LimitValue(offsetValue, offsetIndex, true));
        }
        selectStatement.setLimit(limit);
    }
}
