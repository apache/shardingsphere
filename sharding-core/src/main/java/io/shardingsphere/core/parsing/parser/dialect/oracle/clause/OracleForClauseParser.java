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

package io.shardingsphere.core.parsing.parser.dialect.oracle.clause;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.SQLClauseParser;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;

/**
 * Select for clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleForClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public OracleForClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse for.
     * 
     * @param selectStatement select statement
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.FOR)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.UPDATE);
        if (lexerEngine.skipIfEqual(DefaultKeyword.OF)) {
            do {
                basicExpressionParser.parse(selectStatement);
            } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        }
        if (lexerEngine.equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            lexerEngine.nextToken();
        } else if (lexerEngine.skipIfEqual(OracleKeyword.SKIP)) {
            lexerEngine.accept(OracleKeyword.LOCKED);
        }
    }
}
