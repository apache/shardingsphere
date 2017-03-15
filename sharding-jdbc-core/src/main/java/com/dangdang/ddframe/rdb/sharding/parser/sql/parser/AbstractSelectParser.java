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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableToken;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSelectParser {
    
    private SQLExprParser exprParser;
    
    private final SelectSQLContext sqlContext;
    
    @Setter
    private int parametersIndex;
    
    public AbstractSelectParser(final SQLExprParser exprParser) {
        this.exprParser = exprParser;
        sqlContext = new SelectSQLContext(getExprParser().getLexer().getInput());
    }
    
    /**
     * 解析查询.
     * 
     * @return 解析结果
     */
    public final SelectSQLContext parse() {
        query();
        sqlContext.getOrderByContexts().addAll(exprParser.parseOrderBy(getSqlContext()));
        customizedSelect();
        return sqlContext;
    }
    
    protected void customizedSelect() {
    }
    
    protected void query() {
        getExprParser().accept(DefaultKeyword.SELECT);
        parseDistinct();
        parseSelectList();
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
    }
    
    protected final void parseDistinct() {
        if (getExprParser().equal(DefaultKeyword.DISTINCT, DefaultKeyword.DISTINCTROW, DefaultKeyword.UNION)) {
            sqlContext.setDistinct(true);
            getExprParser().getLexer().nextToken();
            if (hasDistinctOn() && getExprParser().equal(DefaultKeyword.ON)) {
                getExprParser().getLexer().nextToken();
                getExprParser().skipParentheses();
            }
        } else if (getExprParser().equal(DefaultKeyword.ALL)) {
            getExprParser().getLexer().nextToken();
        }
    }
    
    protected boolean hasDistinctOn() {
        return false;
    }
    
    protected final void parseSelectList() {
        int index = 1;
        do {
            SelectItemContext selectItemContext = exprParser.parseSelectItem(index, sqlContext);
            sqlContext.getItemContexts().add(selectItemContext);
            if (selectItemContext instanceof CommonSelectItemContext && ((CommonSelectItemContext) selectItemContext).isStar()) {
                sqlContext.setContainStar(true);
            }
            index++;
        } while (getExprParser().skipIfEqual(Symbol.COMMA));
        sqlContext.setSelectListLastPosition(getExprParser().getLexer().getToken().getEndPosition() - getExprParser().getLexer().getToken().getLiterals().length());
    }
    
    protected void queryRest() {
        if (getExprParser().equal(DefaultKeyword.UNION, DefaultKeyword.EXCEPT, DefaultKeyword.INTERSECT, DefaultKeyword.MINUS)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken().getType());
        }
    }
    
    protected final void parseWhere() {
        if (sqlContext.getTables().isEmpty()) {
            return;
        }
        Optional<ConditionContext> conditionContext = exprParser.parseWhere(sqlContext);
        if (conditionContext.isPresent()) {
            sqlContext.getConditionContexts().add(conditionContext.get());
        }
        parametersIndex = exprParser.getParametersIndex();
    }
    
    protected void parseGroupBy() {
        if (getExprParser().skipIfEqual(DefaultKeyword.GROUP)) {
            getExprParser().accept(DefaultKeyword.BY);
            while (true) {
                addGroupByItem(exprParser.parseExpr(sqlContext));
                if (!getExprParser().equal(Symbol.COMMA)) {
                    break;
                }
                getExprParser().getLexer().nextToken();
            }
            while (getExprParser().equal(DefaultKeyword.WITH) || getExprParser().getLexer().getToken().getLiterals().equalsIgnoreCase("ROLLUP")) {
                getExprParser().getLexer().nextToken();
            }
            if (getExprParser().skipIfEqual(DefaultKeyword.HAVING)) {
                exprParser.parseExpr(sqlContext);
            }
        } else if (getExprParser().skipIfEqual(DefaultKeyword.HAVING)) {
            exprParser.parseExpr(sqlContext);
        }
    }
    
    protected final void addGroupByItem(final SQLExpr sqlExpr) {
        OrderByColumn.OrderByType orderByType = OrderByColumn.OrderByType.ASC;
        if (getExprParser().equal(DefaultKeyword.ASC)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderByColumn.OrderByType.DESC;
        }
        if (sqlExpr instanceof SQLPropertyExpr) {
            SQLPropertyExpr expr = (SQLPropertyExpr) sqlExpr;
            sqlContext.getGroupByContexts().add(new GroupByContext(Optional.of(SQLUtil.getExactlyValue(expr.getOwner().getName())), SQLUtil.getExactlyValue(expr.getName()), orderByType));
        } else if (sqlExpr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr expr = (SQLIdentifierExpr) sqlExpr;
            sqlContext.getGroupByContexts().add(new GroupByContext(Optional.<String>absent(), SQLUtil.getExactlyValue(expr.getName()), orderByType));
        }
    }
    
    public final void parseFrom() {
        if (getExprParser().skipIfEqual(DefaultKeyword.FROM)) {
            parseTable();
        }
    }
    
    public List<TableContext> parseTable() {
        if (getExprParser().equal(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        parseTableFactor();
        parseJoinTable();
        return sqlContext.getTables();
    }
    
    protected final void parseTableFactor() {
        int beginPosition = getExprParser().getLexer().getToken().getEndPosition() - getExprParser().getLexer().getToken().getLiterals().length();
        String literals = getExprParser().getLexer().getToken().getLiterals();
        getExprParser().getLexer().nextToken();
        if (getExprParser().skipIfEqual(Symbol.DOT)) {
            getExprParser().getLexer().nextToken();
            getExprParser().as();
            return;
        }
        // FIXME 根据shardingRule过滤table
        sqlContext.getSqlTokens().add(new TableToken(beginPosition, literals, SQLUtil.getExactlyValue(literals)));
        sqlContext.getTables().add(new TableContext(literals, SQLUtil.getExactlyValue(literals), getExprParser().as()));
    }
    
    protected void parseJoinTable() {
        if (getExprParser().isJoin()) {
            parseTable();
            if (getExprParser().skipIfEqual(DefaultKeyword.ON)) {
                do {
                    parseTableCondition(getExprParser().getLexer().getToken().getEndPosition());
                    getExprParser().accept(Symbol.EQ);
                    parseTableCondition(getExprParser().getLexer().getToken().getEndPosition() - getExprParser().getLexer().getToken().getLiterals().length());
                } while (getExprParser().skipIfEqual(DefaultKeyword.AND));
            } else if (getExprParser().skipIfEqual(DefaultKeyword.USING)) {
                getExprParser().skipParentheses();
            }
            parseJoinTable();
        }
    }
    
    private void parseTableCondition(final int startPosition) {
        SQLExpr sqlExpr = exprParser.parseExpr();
        if (sqlExpr instanceof SQLPropertyExpr) {
            SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
            for (TableContext each : sqlContext.getTables()) {
                if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName()))) {
                    sqlContext.getSqlTokens().add(new TableToken(startPosition, sqlPropertyExpr.getOwner().getName(), SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName())));
                }
            }
        }
    }
}
