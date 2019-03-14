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

package org.apache.shardingsphere.core.parse.parser.clause;

import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parse.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parse.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Update set items clause parser.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class UpdateSetItemsClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public UpdateSetItemsClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse set items.
     *
     * @param updateStatement DML statement
     */
    public void parse(final DMLStatement updateStatement) {
        lexerEngine.accept(DefaultKeyword.SET);
        do {
            parseSetItem(updateStatement);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem(final DMLStatement updateStatement) {
        parseSetColumn(updateStatement);
        lexerEngine.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        parseSetValue(updateStatement);
        skipsDoubleColon();
    }
    
    private void parseSetColumn(final DMLStatement updateStatement) {
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(updateStatement);
            return;
        }
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            if (updateStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                updateStatement.addSQLToken(new TableToken(beginPosition - literals.length(), 
                        0, SQLUtil.getExactlyValue(literals), SQLUtil.getLeftDelimiter(literals), SQLUtil.getRightDelimiter(literals)));
            }
            lexerEngine.nextToken();
        }
    }
    
    private void parseSetValue(final DMLStatement updateStatement) {
        basicExpressionParser.parse(updateStatement);
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}
