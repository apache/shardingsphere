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

package org.apache.shardingsphere.core.parse.old.parser.clause;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.old.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.Keyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.old.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
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
        Optional<InsertValuesToken> insertValuesToken = insertStatement.findSQLToken(InsertValuesToken.class);
        Preconditions.checkState(insertValuesToken.isPresent());
        insertStatement.getSQLTokens().remove(insertValuesToken.get());
        insertStatement.addSQLToken(new InsertValuesToken(insertValuesToken.get().getStartIndex()));
        do {
            lexerEngine.accept(Symbol.LEFT_PAREN);
            List<SQLExpression> sqlExpressions = new LinkedList<>();
            int count = 0;
            do {
                sqlExpressions.add(basicExpressionParser.parse(insertStatement));
                skipsDoubleColon();
                count++;
            } while (lexerEngine.skipIfEqual(Symbol.COMMA));
            Collection<String> columnNames = new ArrayList<>(insertStatement.getColumnNames());
            Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
            if (generateKeyColumnName.isPresent() && count != insertStatement.getColumnNames().size()) {
                columnNames.remove(generateKeyColumnName.get());
            }
            count = 0;
            AndCondition andCondition = new AndCondition();
            String tableName = insertStatement.getTables().getSingleTableName();
            for (String each : columnNames) {
                SQLExpression sqlExpression = sqlExpressions.get(count);
                if (shardingRule.isShardingColumn(each, tableName)) {
                    if (!(sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression || sqlExpression instanceof SQLPlaceholderExpression)) {
                        throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", each);
                    }
                    andCondition.getConditions().add(new Condition(new Column(each, tableName), sqlExpression));
                }
                count++;
            }
            lexerEngine.accept(Symbol.RIGHT_PAREN);
            InsertValue insertValue = new InsertValue(sqlExpressions);
            insertStatement.getValues().add(insertValue);
            insertStatement.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}
