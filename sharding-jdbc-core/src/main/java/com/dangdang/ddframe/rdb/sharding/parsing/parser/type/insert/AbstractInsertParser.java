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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.type.insert;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.type.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.GeneratedKeyToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
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
public abstract class AbstractInsertParser implements SQLParser {
    
    private final com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser sqlParser;
    
    private final ShardingRule shardingRule;
    
    private final InsertSQLContext sqlContext;
    
    public AbstractInsertParser(final ShardingRule shardingRule, final com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        this.shardingRule = shardingRule;
        sqlContext = new InsertSQLContext();
    }
    
    @Override
    public final InsertSQLContext parse() {
        sqlParser.getLexer().nextToken();
        parseInto();
        parseColumns();
        if (sqlParser.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getValuesKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            parseValues();
        } else if (getCustomizedInsertKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            parseCustomizedInsert();
        }
        appendGenerateKey();
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
    
    private void parseColumns() {
        Collection<ShardingColumnContext> result = new LinkedList<>();
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            do {
                sqlParser.getLexer().nextToken();
                result.add(new ShardingColumnContext(SQLUtil.getExactlyValue(sqlParser.getLexer().getCurrentToken().getLiterals()), sqlContext.getTables().get(0).getName()));
                sqlParser.getLexer().nextToken();
            } while (!sqlParser.equalAny(Symbol.RIGHT_PAREN) && !sqlParser.equalAny(Assist.END));
            sqlContext.setColumnsListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
            sqlParser.getLexer().nextToken();
        }
        sqlContext.getShardingColumnContexts().addAll(result);
    }
    
    protected Set<TokenType> getValuesKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.VALUES);
    }
    
    private void parseValues() {
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
            sqlContext.setValuesListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
            int count = 0;
            for (ShardingColumnContext each : sqlContext.getShardingColumnContexts()) {
                if (getShardingRule().isShardingColumn(each)) {
                    conditionContext.add(new ConditionContext.Condition(each, sqlExprs.get(count)));
                }
                count++;
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
    
    private void appendGenerateKey() {
        String tableName = sqlContext.getTables().get(0).getName();
        ItemsToken columnsToken = new ItemsToken(sqlContext.getColumnsListLastPosition());
        GeneratedKeyToken generatedKeyToken = new GeneratedKeyToken(sqlContext.getValuesListLastPosition());
        Optional<String> autoGeneratedKeyColumn = shardingRule.getGenerateKeyColumn(tableName);
        if (autoGeneratedKeyColumn.isPresent()) {
            if (!sqlContext.hasColumn(autoGeneratedKeyColumn.get())) {
                columnsToken.getItems().add(autoGeneratedKeyColumn.get());
                sqlContext.setGeneratedKeyContext(new GeneratedKeyContext(autoGeneratedKeyColumn.get()));
            }
        }
        if (!columnsToken.getItems().isEmpty()) {
            sqlContext.getSqlTokens().add(columnsToken);
            sqlContext.getSqlTokens().add(generatedKeyToken);
        }
    }
}
