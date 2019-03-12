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

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert values clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public abstract class InsertValuesClauseParser implements SQLClauseParser {
    
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
            parseValues(insertStatement);
        }
    }
    
    protected abstract Keyword[] getSynonymousKeywordsForValues();
    
    /**
     * Parse insert values.
     *
     * @param insertStatement insert statement
     */
    private void parseValues(final InsertStatement insertStatement) {
        int endPosition;
        int startParametersIndex;
        insertStatement.getInsertValuesToken().setType(DefaultKeyword.VALUES);
        do {
            startParametersIndex = insertStatement.getParametersIndex();
            lexerEngine.accept(Symbol.LEFT_PAREN);
            List<SQLExpression> sqlExpressions = new LinkedList<>();
            int count = 0;
            do {
                sqlExpressions.add(basicExpressionParser.parse(insertStatement));
                skipsDoubleColon();
                count++;
            } while (lexerEngine.skipIfEqual(Symbol.COMMA));
            removeGenerateKeyColumn(insertStatement, count);
            count = 0;
            AndCondition andCondition = new AndCondition();
            for (Column each : insertStatement.getColumns()) {
                SQLExpression sqlExpression = sqlExpressions.get(count);
                if (shardingRule.isShardingColumn(each.getName(), each.getTableName())) {
                    if (!(sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression || sqlExpression instanceof SQLPlaceholderExpression)) {
                        throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", each.getName());
                    }
                    andCondition.getConditions().add(new Condition(each, sqlExpression));
                }
                Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
                if (generateKeyColumnName.isPresent() && generateKeyColumnName.get().equals(each.getName())) {
                    insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(each, sqlExpression));
                }
                count++;
            }
            endPosition = lexerEngine.getCurrentToken().getEndPosition();
            lexerEngine.accept(Symbol.RIGHT_PAREN);
            InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, insertStatement.getParametersIndex() - startParametersIndex);
            insertStatement.getInsertValues().getInsertValues().add(insertValue);
            insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        insertStatement.setInsertValuesListLastIndex(endPosition - 1);
    }
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final int valueCount) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && valueCount < insertStatement.getColumns().size()) {
            insertStatement.getColumns().remove(new Column(generateKeyColumnName.get(), insertStatement.getTables().getSingleTableName()));
        }
    }
    
    private GeneratedKeyCondition createGeneratedKeyCondition(final Column column, final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            return new GeneratedKeyCondition(column, ((SQLPlaceholderExpression) sqlExpression).getIndex(), null);
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return new GeneratedKeyCondition(column, -1, (Comparable<?>) ((SQLNumberExpression) sqlExpression).getNumber());
        }
        return new GeneratedKeyCondition(column, -1, ((SQLTextExpression) sqlExpression).getText());
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}