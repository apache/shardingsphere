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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLIdentifierExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPropertyExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertColumnToken;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.util.SQLUtil;

import java.util.Iterator;

/**
 * Insert set clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public abstract class InsertSetClauseParser implements SQLClauseParser {
    
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
        insertStatement.getInsertValuesToken().setType(DefaultKeyword.SET);
        do {
            SQLExpression left = basicExpressionParser.parse(insertStatement);
            Column column = null;
            if (left instanceof SQLPropertyExpression) {
                column = new Column(SQLUtil.getExactlyValue(((SQLPropertyExpression) left).getName()), insertStatement.getTables().getSingleTableName());
            }
            if (left instanceof SQLIdentifierExpression) {
                column = new Column(SQLUtil.getExactlyValue(((SQLIdentifierExpression) left).getName()), insertStatement.getTables().getSingleTableName());
            }
            if (left instanceof SQLIgnoreExpression) {
                column = new Column(SQLUtil.getExactlyValue(((SQLIgnoreExpression) left).getExpression()), insertStatement.getTables().getSingleTableName());
            }
            Preconditions.checkNotNull(column);
            lexerEngine.accept(Symbol.EQ);
            SQLExpression right = basicExpressionParser.parse(insertStatement);
            if (shardingRule.isShardingColumn(column.getName(), column.getTableName()) 
                    && (right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression)) {
                insertStatement.getRouteConditions().add(new Condition(column, right));
            }
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        int endPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, insertStatement.getParametersIndex());
        insertStatement.getInsertValues().getInsertValues().add(insertValue);
        insertStatement.setInsertValuesListLastIndex(endPosition - 1);
    }
    
    private void removeUnnecessaryToken(final InsertStatement insertStatement) {
        Iterator<SQLToken> sqlTokens = insertStatement.getSQLTokens().iterator();
        while (sqlTokens.hasNext()) {
            SQLToken sqlToken = sqlTokens.next();
            if (sqlToken instanceof InsertColumnToken || sqlToken instanceof ItemsToken) {
                sqlTokens.remove();
            }
        }
    }
    
    protected abstract Keyword[] getCustomizedInsertKeywords();
}
