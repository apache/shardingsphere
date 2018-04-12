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

package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingjdbc.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import io.shardingjdbc.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.parsing.parser.token.MultipleInsertValuesToken;
import io.shardingjdbc.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert values clause parser.
 *
 * @author zhangliang
 */
public class InsertValuesClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public InsertValuesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse insert values.
     *
     * @param insertStatement insert statement
     */
    public void parse(final InsertStatement insertStatement) {
        Collection<Keyword> valueKeywords = new LinkedList<>();
        valueKeywords.add(DefaultKeyword.VALUES);
        valueKeywords.addAll(Arrays.asList(getSynonymousKeywordsForValues()));
        if (lexerEngine.skipIfEqual(valueKeywords.toArray(new Keyword[valueKeywords.size()]))) {
            insertStatement.setAfterValuesPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
            parseValues(insertStatement);
            if (lexerEngine.equalAny(Symbol.COMMA)) {
                parseMultipleValues(insertStatement);
            }
        }
    }
    
    protected Keyword[] getSynonymousKeywordsForValues() {
        return new Keyword[0];
    }
    
    private void parseValues(final InsertStatement insertStatement) {
        lexerEngine.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        do {
            sqlExpressions.add(basicExpressionParser.parse(insertStatement));
            skipsDoubleColon();
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        insertStatement.setValuesListLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
        int count = 0;
        for (Column each : insertStatement.getColumns()) {
            SQLExpression sqlExpression = sqlExpressions.get(count);
            insertStatement.getConditions().add(new Condition(each, sqlExpression), shardingRule);
            if (insertStatement.getGenerateKeyColumnIndex() == count) {
                insertStatement.setGeneratedKeyCondition(createGeneratedKeyCondition(each, sqlExpression));
            }
            count++;
        }
        lexerEngine.accept(Symbol.RIGHT_PAREN);
    }
    
    private GeneratedKeyCondition createGeneratedKeyCondition(final Column column, final SQLExpression sqlExpression) {
        GeneratedKeyCondition result;
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            result = new GeneratedKeyCondition(column.getName(), ((SQLPlaceholderExpression) sqlExpression).getIndex(), null);
        } else if (sqlExpression instanceof SQLNumberExpression) {
            result = new GeneratedKeyCondition(column.getName(), -1, ((SQLNumberExpression) sqlExpression).getNumber());
        } else {
            throw new ShardingJdbcException("Generated key only support number.");
        }
        return result;
    }
    
    private void parseMultipleValues(final InsertStatement insertStatement) {
        insertStatement.getMultipleConditions().add(new Conditions(insertStatement.getConditions()));
        MultipleInsertValuesToken valuesToken = new MultipleInsertValuesToken(insertStatement.getAfterValuesPosition());
        valuesToken.getValues().add(
                lexerEngine.getInput().substring(insertStatement.getAfterValuesPosition(), lexerEngine.getCurrentToken().getEndPosition() - Symbol.COMMA.getLiterals().length()));
        while (lexerEngine.skipIfEqual(Symbol.COMMA)) {
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
            parseValues(insertStatement);
            insertStatement.getMultipleConditions().add(new Conditions(insertStatement.getConditions()));
            int endPosition = lexerEngine.equalAny(Symbol.COMMA)
                    ? lexerEngine.getCurrentToken().getEndPosition() - Symbol.COMMA.getLiterals().length() : lexerEngine.getCurrentToken().getEndPosition();
            valuesToken.getValues().add(lexerEngine.getInput().substring(beginPosition, endPosition));
        }
        insertStatement.getSqlTokens().add(valuesToken);
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}
