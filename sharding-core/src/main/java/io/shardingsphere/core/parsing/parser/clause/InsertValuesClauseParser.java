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

import com.google.common.base.Optional;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.rule.ShardingRule;

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
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        int endPosition;
        int startParametersIndex;
        insertStatement.addSQLToken(new InsertValuesToken(beginPosition, insertStatement.getTables().getSingleTableName()));
        do {
            beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
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
                if (shardingRule.isShardingColumn(each)) {
                    if (!(sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression || sqlExpression instanceof SQLPlaceholderExpression)) {
                        throw new SQLParsingException("INSERT INTO can not support complex expression value on sharding column '%s'.", each.getName());
                    }
                    andCondition.getConditions().add(new Condition(each, sqlExpression));
                }
                if (insertStatement.getGenerateKeyColumnIndex() == count) {
                    insertStatement.getGeneratedKeyConditions().add(createGeneratedKeyCondition(each, sqlExpression));
                }
                count++;
            }
            endPosition = lexerEngine.getCurrentToken().getEndPosition();
            lexerEngine.accept(Symbol.RIGHT_PAREN);
            InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, lexerEngine.getInput().substring(beginPosition, endPosition), insertStatement.getParametersIndex() - startParametersIndex);
            insertStatement.getInsertValues().getInsertValues().add(insertValue);
            insertStatement.getConditions().getOrCondition().getAndConditions().add(andCondition);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        insertStatement.setInsertValuesListLastPosition(endPosition);
    }
    
    private void removeGenerateKeyColumn(final InsertStatement insertStatement, final int valueCount) {
        Optional<Column> generateKeyColumn = shardingRule.getGenerateKeyColumn(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumn.isPresent() && valueCount < insertStatement.getColumns().size()) {
            List<ItemsToken> itemsTokens = insertStatement.getItemsTokens();
            insertStatement.getColumns().remove(new Column(generateKeyColumn.get().getName(), insertStatement.getTables().getSingleTableName()));
            for (ItemsToken each : itemsTokens) {
                each.getItems().remove(generateKeyColumn.get().getName());
                insertStatement.setGenerateKeyColumnIndex(-1);
            }
        }
    }
    
    private GeneratedKeyCondition createGeneratedKeyCondition(final Column column, final SQLExpression sqlExpression) {
        GeneratedKeyCondition result;
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            result = new GeneratedKeyCondition(column, ((SQLPlaceholderExpression) sqlExpression).getIndex(), null);
        } else if (sqlExpression instanceof SQLNumberExpression) {
            result = new GeneratedKeyCondition(column, -1, ((SQLNumberExpression) sqlExpression).getNumber());
        } else {
            throw new ShardingException("Generated key only support number.");
        }
        return result;
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}
