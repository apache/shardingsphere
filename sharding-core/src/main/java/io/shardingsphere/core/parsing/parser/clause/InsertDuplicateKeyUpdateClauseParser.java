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

package io.shardingsphere.core.parsing.parser.clause;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Insert duplicate key update clause parser.
 *
 * @author maxiaoguang
 */
public abstract class InsertDuplicateKeyUpdateClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public InsertDuplicateKeyUpdateClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse insert duplicate key update.
     *
     * @param insertStatement insert statement
     */
    public void parse(final InsertStatement insertStatement) {
        if (!lexerEngine.skipIfEqual(getCustomizedInsertKeywords())) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.DUPLICATE);
        lexerEngine.accept(DefaultKeyword.KEY);
        lexerEngine.accept(DefaultKeyword.UPDATE);
        do {
            Column column = new Column(SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals()), insertStatement.getTables().getSingleTableName());
            if (shardingRule.isShardingColumn(column)) {
                throw new SQLParsingException("INSERT INTO .... ON DUPLICATE KEY UPDATE can not support on sharding column, token is '%s', literals is '%s'.",
                        lexerEngine.getCurrentToken().getType(), lexerEngine.getCurrentToken().getLiterals());
            }
            basicExpressionParser.parse(insertStatement);
            lexerEngine.accept(Symbol.EQ);
            if (lexerEngine.skipIfEqual(DefaultKeyword.VALUES)) {
                lexerEngine.accept(Symbol.LEFT_PAREN);
                basicExpressionParser.parse(insertStatement);
                lexerEngine.accept(Symbol.RIGHT_PAREN);
            } else {
                lexerEngine.nextToken();
            }
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    protected abstract Keyword[] getCustomizedInsertKeywords();
}
