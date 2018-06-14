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
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Literals;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIgnoreExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import io.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

import java.util.Iterator;

/**
 * Insert set clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
public class InsertSetClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public InsertSetClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse insert set.
     *
     * @param insertStatement insert statement
     */
    public void parse(final InsertStatement insertStatement) {
        if (!lexerEngine.skipIfEqual(getCustomizedInsertKeywords())) {
            return;
        }
        removeUnnecessaryToken(insertStatement);
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        insertStatement.getSqlTokens().add(new InsertValuesToken(beginPosition, insertStatement.getTables().getSingleTableName()));
        int parametersCount = 0;
        do {
            SQLExpression sqlExpression = basicExpressionParser.parse(insertStatement);
            Column column = null;
            if (sqlExpression instanceof SQLPropertyExpression) {
                column = new Column(SQLUtil.getExactlyValue(((SQLPropertyExpression) sqlExpression).getName()), insertStatement.getTables().getSingleTableName());
            }
            if (sqlExpression instanceof SQLIdentifierExpression) {
                column = new Column(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()), insertStatement.getTables().getSingleTableName());
            }
            if (sqlExpression instanceof SQLIgnoreExpression) {
                column = new Column(SQLUtil.getExactlyValue(((SQLIgnoreExpression) sqlExpression).getExpression()), insertStatement.getTables().getSingleTableName());
            }
            lexerEngine.accept(Symbol.EQ);
            if (lexerEngine.equalAny(Literals.INT)) {
                sqlExpression = new SQLNumberExpression(Integer.parseInt(lexerEngine.getCurrentToken().getLiterals()));
            } else if (lexerEngine.equalAny(Literals.FLOAT)) {
                sqlExpression = new SQLNumberExpression(Double.parseDouble(lexerEngine.getCurrentToken().getLiterals()));
            } else if (lexerEngine.equalAny(Literals.CHARS)) {
                sqlExpression = new SQLTextExpression(lexerEngine.getCurrentToken().getLiterals());
            } else if (lexerEngine.equalAny(DefaultKeyword.NULL)) {
                sqlExpression = new SQLIgnoreExpression(DefaultKeyword.NULL.name());
            } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
                sqlExpression = new SQLPlaceholderExpression(insertStatement.getParametersIndex());
                insertStatement.increaseParametersIndex();
                parametersCount++;
            } else {
                throw new UnsupportedOperationException("");
            }
            lexerEngine.nextToken();
            if (lexerEngine.equalAny(Symbol.COMMA, DefaultKeyword.ON, Assist.END)) {
                insertStatement.getConditions().add(new Condition(column, sqlExpression), shardingRule);
            } else {
                lexerEngine.skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        int endPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        insertStatement.getInsertValues().getInsertValues().add(new InsertValue(DefaultKeyword.VALUES, lexerEngine.getInput().substring(beginPosition, endPosition), parametersCount));
        insertStatement.setInsertValuesListLastPosition(endPosition);
    }
    
    private void removeUnnecessaryToken(final InsertStatement insertStatement) {
        Iterator<SQLToken> sqlTokenIterable = insertStatement.getSqlTokens().iterator();
        while (sqlTokenIterable.hasNext()) {
            SQLToken sqlToken = sqlTokenIterable.next();
            if (sqlToken instanceof InsertColumnToken || sqlToken instanceof ItemsToken) {
                sqlTokenIterable.remove();
            }
        }
    }
    
    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[0];
    }
}
