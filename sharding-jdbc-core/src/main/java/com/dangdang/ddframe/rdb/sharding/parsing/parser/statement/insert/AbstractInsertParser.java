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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
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
public abstract class AbstractInsertParser implements SQLStatementParser {
    
    private final SQLParser sqlParser;
    
    private final ShardingRule shardingRule;
    
    private final InsertSQLContext sqlContext;
    
    public AbstractInsertParser(final ShardingRule shardingRule, final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        this.shardingRule = shardingRule;
        sqlContext = new InsertSQLContext();
        sqlContext.setSqlBuilderContext(sqlParser.getSqlBuilderContext());
    }
    
    @Override
    public final InsertSQLContext parse() {
        sqlParser.getLexer().nextToken();
        parseInto();
        Collection<ShardingColumnContext> shardingColumnContexts = parseColumns();
        if (sqlParser.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getValuesKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            parseValues(shardingColumnContexts);
        } else if (getCustomizedInsertKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            parseCustomizedInsert();
        }
        return sqlContext;
    }
    
    protected Set<TokenType> getUnsupportedKeywords() {
        return Collections.emptySet();
    }
    
    private void parseInto() {
        if (getUnsupportedKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        sqlParser.skipUntil(DefaultKeyword.INTO);
        sqlParser.getLexer().nextToken();
        sqlParser.parseSingleTable(sqlContext);
        skipBetweenTableAndValues();
    }
    
    private void skipBetweenTableAndValues() {
        while (getSkippedKeywordsBetweenTableAndValues().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            sqlParser.getLexer().nextToken();
            if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
                sqlParser.skipParentheses();
            }
        }
    }
    
    protected Set<TokenType> getSkippedKeywordsBetweenTableAndValues() {
        return Collections.emptySet();
    }
    
    private Collection<ShardingColumnContext> parseColumns() {
        Collection<ShardingColumnContext> result = new LinkedList<>();
        Collection<String> autoIncrementColumns = shardingRule.getAutoIncrementColumns(sqlContext.getTables().get(0).getName());
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            do {
                sqlParser.getLexer().nextToken();
                result.add(getColumn(autoIncrementColumns));
                sqlParser.getLexer().nextToken();
            } while (!sqlParser.equalAny(Symbol.RIGHT_PAREN) && !sqlParser.equalAny(Assist.END));
            ItemsToken itemsToken = new ItemsToken(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
            for (String each : autoIncrementColumns) {
                itemsToken.getItems().add(each);
                result.add(new ShardingColumnContext(each, sqlContext.getTables().get(0).getName(), true));
            }
            if (!itemsToken.getItems().isEmpty()) {
                sqlParser.getSqlBuilderContext().getSqlTokens().add(itemsToken);
            }
            sqlParser.getLexer().nextToken();
        }
        return result;
    }
    
    protected final ShardingColumnContext getColumn(final Collection<String> autoIncrementColumns) {
        String columnName = SQLUtil.getExactlyValue(sqlParser.getLexer().getCurrentToken().getLiterals());
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
            sqlParser.getLexer().nextToken();
            sqlParser.accept(Symbol.LEFT_PAREN);
            List<SQLExpr> sqlExprs = new LinkedList<>();
            ConditionContext conditionContext = new ConditionContext();
            do {
                sqlExprs.add(sqlParser.parseExpression());
            } while (sqlParser.skipIfEqual(Symbol.COMMA));
            ItemsToken itemsToken = new ItemsToken(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
            int count = 0;
            int offset = 0;
            for (ShardingColumnContext each : shardingColumnContexts) {
                if (each.isAutoIncrement()) {
                    Number generatedId = getShardingRule().findTableRule(sqlContext.getTables().get(0).getName()).generateId(each.getColumnName());
                    if (0 == sqlParser.getParametersIndex()) {
                        itemsToken.getItems().add(generatedId.toString());
                        sqlExprs.add(new SQLNumberExpr(generatedId));
                    } else {
                        itemsToken.getItems().add("?");
                        offset++;
                        sqlExprs.add(new SQLPlaceholderExpr(sqlParser.getParametersIndex() + offset - 1));
                    }
                    sqlContext.getGeneratedKeyContext().getColumns().add(each.getColumnName());
                    sqlContext.getGeneratedKeyContext().putValue(each.getColumnName(), generatedId);
                }
                if (getShardingRule().isShardingColumn(each)) {
                    conditionContext.add(new ConditionContext.Condition(each, sqlExprs.get(count)));
                }
                count++;
            }
            if (!itemsToken.getItems().isEmpty()) {
                sqlParser.getSqlBuilderContext().getSqlTokens().add(itemsToken);
            }
            sqlParser.accept(Symbol.RIGHT_PAREN);
            parsed = true;
            sqlContext.setConditionContext(conditionContext);
        }
        while (sqlParser.equalAny(Symbol.COMMA));
    }
    
    protected Set<TokenType> getCustomizedInsertKeywords() {
        return Collections.emptySet();
    }
    
    protected void parseCustomizedInsert() {
    }
}
