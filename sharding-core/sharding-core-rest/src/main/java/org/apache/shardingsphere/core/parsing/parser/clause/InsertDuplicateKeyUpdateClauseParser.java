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

package org.apache.shardingsphere.core.parsing.parser.clause;

import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.util.SQLUtil;

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
            if (shardingRule.isShardingColumn(column.getName(), column.getTableName())) {
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
