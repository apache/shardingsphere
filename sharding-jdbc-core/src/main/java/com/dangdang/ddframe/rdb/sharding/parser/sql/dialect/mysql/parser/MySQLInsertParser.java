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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLCharExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIgnoreExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Keyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.GeneralLiterals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.AbstractInsertParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * MySQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLInsertParser extends AbstractInsertParser {
    
    public MySQLInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void parseCustomizedInsert() {
        parseInsertSet();
    }
    
    private void parseInsertSet() {
        ParseContext parseContext = getParseContext();
        Collection<String> autoIncrementColumns = getShardingRule().getAutoIncrementColumns(getSqlContext().getTables().get(0).getName());
        do {
            getExprParser().getLexer().nextToken();
            Condition.Column column = getColumn(autoIncrementColumns);
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(Symbol.EQ);
            SQLExpr sqlExpr;
            if (getExprParser().getLexer().equalToken(GeneralLiterals.INT)) {
                sqlExpr = new SQLNumberExpr(Integer.parseInt(getExprParser().getLexer().getLiterals()));
            } else if (getExprParser().getLexer().equalToken(GeneralLiterals.FLOAT)) {
                sqlExpr = new SQLNumberExpr(Double.parseDouble(getExprParser().getLexer().getLiterals()));
            } else if (getExprParser().getLexer().equalToken(GeneralLiterals.CHARS)) {
                sqlExpr = new SQLCharExpr(getExprParser().getLexer().getLiterals());
            } else if (getExprParser().getLexer().equalToken(DefaultKeyword.NULL)) {
                sqlExpr = new SQLIgnoreExpr();
            } else if (getExprParser().getLexer().equalToken(Symbol.QUESTION)) {
                sqlExpr = new SQLPlaceholderExpr(getExprParser().getParametersIndex(), getExprParser().getParameters().get(getExprParser().getParametersIndex()));
                getExprParser().setParametersIndex(getExprParser().getParametersIndex() + 1);
            } else {
                throw new UnsupportedOperationException("");
            }
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().equalToken(Symbol.COMMA, DefaultKeyword.ON, Assist.EOF)) {
                parseContext.addCondition(column.getColumnName(), column.getTableName(), Condition.BinaryOperator.EQUAL, sqlExpr);
            } else {
                getExprParser().getLexer().skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (getExprParser().getLexer().equalToken(Symbol.COMMA));
        getSqlContext().getConditionContexts().add(parseContext.getCurrentConditionContext());
    }
    
    @Override
    protected Set<Keyword> getSkippedTokensBetweenTableAndValues() {
        return Sets.<Keyword>newHashSet(MySQLKeyword.PARTITION);
    }
    
    @Override
    protected Set<Keyword> getValuesKeywords() {
        return Sets.<Keyword>newHashSet(DefaultKeyword.VALUES, MySQLKeyword.VALUE);
    }
    
    @Override
    protected Set<Keyword> getCustomizedInsertTokens() {
        return Sets.<Keyword>newHashSet(DefaultKeyword.SET);
    }
}
