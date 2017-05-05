/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.insert;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Insert语句解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractInsertParser {
    
    private final SQLParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final InsertSQLContext sqlContext;
    
    public AbstractInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLParser exprParser) {
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        sqlContext = new InsertSQLContext();
        sqlContext.setSqlBuilderContext(exprParser.getSqlBuilderContext());
    }
    
    /**
     * 解析Insert语句.
     * 
     * @return 解析结果
     */
    public final InsertSQLContext parse() {
        exprParser.getLexer().nextToken();
        parseInto();
        Collection<ShardingColumnContext> shardingColumnContexts = parseColumns();
        if (exprParser.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getValuesKeywords().contains(exprParser.getLexer().getCurrentToken().getType())) {
            parseValues(shardingColumnContexts);
        } else if (getCustomizedInsertKeywords().contains(exprParser.getLexer().getCurrentToken().getType())) {
            parseCustomizedInsert();
        }
        return sqlContext;
    }
    
    protected Set<TokenType> getUnsupportedKeywords() {
        return Collections.emptySet();
    }
    
    private void parseInto() {
        if (getUnsupportedKeywords().contains(exprParser.getLexer().getCurrentToken().getType())) {
            throw new ParserUnsupportedException(exprParser.getLexer().getCurrentToken().getType());
        }
        exprParser.skipUntil(DefaultKeyword.INTO);
        exprParser.getLexer().nextToken();
        exprParser.parseSingleTable(sqlContext);
        skipBetweenTableAndValues();
    }
    
    private void skipBetweenTableAndValues() {
        while (getSkippedKeywordsBetweenTableAndValues().contains(exprParser.getLexer().getCurrentToken().getType())) {
            exprParser.getLexer().nextToken();
            if (exprParser.equalAny(Symbol.LEFT_PAREN)) {
                exprParser.skipParentheses();
            }
        }
    }
    
    protected Set<TokenType> getSkippedKeywordsBetweenTableAndValues() {
        return Collections.emptySet();
    }
    
    private Collection<ShardingColumnContext> parseColumns() {
        Collection<ShardingColumnContext> result = new LinkedList<>();
        Collection<String> autoIncrementColumns = shardingRule.getAutoIncrementColumns(sqlContext.getTables().get(0).getName());
        if (exprParser.equalAny(Symbol.LEFT_PAREN)) {
            do {
                exprParser.getLexer().nextToken();
                result.add(getColumn(autoIncrementColumns));
                exprParser.getLexer().nextToken();
            } while (!exprParser.equalAny(Symbol.RIGHT_PAREN) && !exprParser.equalAny(Assist.END));
            ItemsToken itemsToken = new ItemsToken(exprParser.getLexer().getCurrentToken().getEndPosition() - exprParser.getLexer().getCurrentToken().getLiterals().length());
            for (String each : autoIncrementColumns) {
                itemsToken.getItems().add(each);
                result.add(new ShardingColumnContext(each, sqlContext.getTables().get(0).getName(), true));
            }
            if (!itemsToken.getItems().isEmpty()) {
                exprParser.getSqlBuilderContext().getSqlTokens().add(itemsToken);
            }
            exprParser.getLexer().nextToken();
        }
        return result;
    }
    
    protected final ShardingColumnContext getColumn(final Collection<String> autoIncrementColumns) {
        String columnName = SQLUtil.getExactlyValue(exprParser.getLexer().getCurrentToken().getLiterals());
        if (autoIncrementColumns.contains(columnName)) {
            autoIncrementColumns.remove(columnName);
        }
        return new ShardingColumnContext(columnName, sqlContext.getTables().get(0).getName());
    }
    
    protected Set<TokenType> getValuesKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.VALUES);
    }
    
    private void parseValues(final Collection<ShardingColumnContext> shardingColumnContexts) {
        boolean parsed = false;
        do {
            if (parsed) {
                throw new UnsupportedOperationException("Cannot support multiple insert");
            }
            exprParser.getLexer().nextToken();
            exprParser.accept(Symbol.LEFT_PAREN);
            List<SQLExpr> sqlExprs = new LinkedList<>();
            ConditionContext conditionContext = new ConditionContext();
            do {
                sqlExprs.add(exprParser.parseExpression());
            } while (exprParser.skipIfEqual(Symbol.COMMA));
            ItemsToken itemsToken = new ItemsToken(exprParser.getLexer().getCurrentToken().getEndPosition() - exprParser.getLexer().getCurrentToken().getLiterals().length());
            int count = 0;
            for (ShardingColumnContext each : shardingColumnContexts) {
                if (each.isAutoIncrement()) {
                    Number autoIncrementedValue = (Number) getShardingRule().findTableRule(sqlContext.getTables().get(0).getName()).generateId(each.getColumnName());
                    if (parameters.isEmpty()) {
                        itemsToken.getItems().add(autoIncrementedValue.toString());
                        sqlExprs.add(new SQLNumberExpr(autoIncrementedValue));
                    } else {
                        itemsToken.getItems().add("?");
                        parameters.add(autoIncrementedValue);
                        sqlExprs.add(new SQLPlaceholderExpr(parameters.size() - 1, autoIncrementedValue));
                    }
                    sqlContext.getGeneratedKeyContext().getColumns().add(each.getColumnName());
                    sqlContext.getGeneratedKeyContext().putValue(each.getColumnName(), autoIncrementedValue);
                }
                if (getShardingRule().isShardingColumn(each)) {
                    conditionContext.add(new ConditionContext.Condition(each, sqlExprs.get(count)));
                }
                count++;
            }
            if (!itemsToken.getItems().isEmpty()) {
                exprParser.getSqlBuilderContext().getSqlTokens().add(itemsToken);
            }
            exprParser.accept(Symbol.RIGHT_PAREN);
            parsed = true;
            sqlContext.setConditionContext(conditionContext);
        }
        while (exprParser.equalAny(Symbol.COMMA));
    }
    
    protected Set<TokenType> getCustomizedInsertKeywords() {
        return Collections.emptySet();
    }
    
    protected void parseCustomizedInsert() {
    }
}
