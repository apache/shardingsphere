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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIgnoreExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLTextExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.type.insert.AbstractInsertParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * MySQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLInsertParser extends AbstractInsertParser {
    
    public MySQLInsertParser(final ShardingRule shardingRule, final SQLParser sqlParser) {
        super(shardingRule, sqlParser);
    }
    
    @Override
    protected void parseCustomizedInsert() {
        parseInsertSet();
    }
    
    private void parseInsertSet() {
        ConditionContext conditionContext = new ConditionContext();
        do {
            getSqlParser().getLexer().nextToken();
            ShardingColumnContext shardingColumnContext = new ShardingColumnContext(
                    SQLUtil.getExactlyValue(getSqlParser().getLexer().getCurrentToken().getLiterals()), getSqlContext().getTables().get(0).getName());
            getSqlParser().getLexer().nextToken();
            getSqlParser().accept(Symbol.EQ);
            SQLExpr sqlExpr;
            if (getSqlParser().equalAny(Literals.INT)) {
                sqlExpr = new SQLNumberExpr(Integer.parseInt(getSqlParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getSqlParser().equalAny(Literals.FLOAT)) {
                sqlExpr = new SQLNumberExpr(Double.parseDouble(getSqlParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getSqlParser().equalAny(Literals.CHARS)) {
                sqlExpr = new SQLTextExpr(getSqlParser().getLexer().getCurrentToken().getLiterals());
            } else if (getSqlParser().equalAny(DefaultKeyword.NULL)) {
                sqlExpr = new SQLIgnoreExpr();
            } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                sqlExpr = new SQLPlaceholderExpr(getSqlParser().getParametersIndex());
                getSqlParser().setParametersIndex(getSqlParser().getParametersIndex() + 1);
            } else {
                throw new UnsupportedOperationException("");
            }
            getSqlParser().getLexer().nextToken();
            if (getSqlParser().equalAny(Symbol.COMMA, DefaultKeyword.ON, Assist.END)) {
                if (getShardingRule().isShardingColumn(shardingColumnContext)) {
                    conditionContext.add(new ConditionContext.Condition(shardingColumnContext, sqlExpr));
                }
            } else {
                getSqlParser().skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (getSqlParser().equalAny(Symbol.COMMA));
        getSqlContext().setConditionContext(conditionContext);
    }
    
    @Override
    protected Set<TokenType> getSkippedKeywordsBetweenTableAndValues() {
        return Sets.<TokenType>newHashSet(MySQLKeyword.PARTITION);
    }
    
    @Override
    protected Set<TokenType> getValuesKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.VALUES, MySQLKeyword.VALUE);
    }
    
    @Override
    protected Set<TokenType> getCustomizedInsertKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.SET);
    }
}
