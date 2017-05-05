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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSelectParser {
    
    private static final String SHARDING_GEN_ALIAS = "sharding_gen_%s";
    
    private SQLParser exprParser;
    
    private final SelectSQLContext sqlContext;
    
    @Setter
    private int parametersIndex;
    
    private int derivedColumnOffset;
    
    private ItemsToken itemsToken;
    
    public AbstractSelectParser(final SQLParser exprParser) {
        this.exprParser = exprParser;
        sqlContext = new SelectSQLContext();
        sqlContext.setSqlBuilderContext(exprParser.getSqlBuilderContext());
    }
    
    /**
     * 解析查询.
     * 
     * @return 解析结果
     */
    public final SelectSQLContext parse() {
        query();
        sqlContext.getOrderByContexts().addAll(parseOrderBy(getSqlContext()));
        customizedSelect();
        if (!itemsToken.getItems().isEmpty()) {
            exprParser.getSqlBuilderContext().getSqlTokens().add(itemsToken);
        }
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
        if (getExprParser().equalAny(DefaultKeyword.DISTINCT, DefaultKeyword.DISTINCTROW, DefaultKeyword.UNION)) {
            sqlContext.setDistinct(true);
            getExprParser().getLexer().nextToken();
            if (hasDistinctOn() && getExprParser().equalAny(DefaultKeyword.ON)) {
                getExprParser().getLexer().nextToken();
                getExprParser().skipParentheses();
            }
        } else if (getExprParser().equalAny(DefaultKeyword.ALL)) {
            getExprParser().getLexer().nextToken();
        }
    }
    
    protected boolean hasDistinctOn() {
        return false;
    }
    
    protected final void parseSelectList() {
        int index = 1;
        do {
            SelectItemContext selectItemContext = exprParser.parseSelectItem(index);
            sqlContext.getItemContexts().add(selectItemContext);
            if (selectItemContext instanceof CommonSelectItemContext && ((CommonSelectItemContext) selectItemContext).isStar()) {
                sqlContext.setContainStar(true);
            }
            index++;
        } while (getExprParser().skipIfEqual(Symbol.COMMA));
        sqlContext.setSelectListLastPosition(getExprParser().getLexer().getCurrentToken().getEndPosition() - getExprParser().getLexer().getCurrentToken().getLiterals().length());
        itemsToken = new ItemsToken(sqlContext.getSelectListLastPosition());
        for (SelectItemContext each : sqlContext.getItemContexts()) {
            if (each instanceof AggregationSelectItemContext) {
                AggregationSelectItemContext aggregationSelectItemContext = (AggregationSelectItemContext) each;
                if (AggregationType.AVG.equals(aggregationSelectItemContext.getAggregationType())) {
                    AggregationSelectItemContext countSelectItemContext = new AggregationSelectItemContext(
                            aggregationSelectItemContext.getInnerExpression(), Optional.of(generateDerivedColumnAlias()), -1, AggregationType.COUNT);
                    AggregationSelectItemContext sumSelectItemContext = new AggregationSelectItemContext(
                            aggregationSelectItemContext.getInnerExpression(), Optional.of(generateDerivedColumnAlias()), -1, AggregationType.SUM);
                    aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().add(countSelectItemContext);
                    aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().add(sumSelectItemContext);
                    // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
                    itemsToken.getItems().add(countSelectItemContext.getExpression() + " AS " + countSelectItemContext.getAlias().get() + " ");
                    itemsToken.getItems().add(sumSelectItemContext.getExpression() + " AS " + sumSelectItemContext.getAlias().get() + " ");
                }
            }
        }
    }
    
    protected void queryRest() {
        if (getExprParser().equalAny(DefaultKeyword.UNION, DefaultKeyword.EXCEPT, DefaultKeyword.INTERSECT, DefaultKeyword.MINUS)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getCurrentToken().getType());
        }
    }
    
    protected final void parseWhere() {
        if (sqlContext.getTables().isEmpty()) {
            return;
        }
        exprParser.parseWhere(sqlContext);
        parametersIndex = exprParser.getParametersIndex();
    }
    
    /**
     * 解析排序.
     *
     * @param sqlContext SQL上下文
     * @return 排序上下文
     */
    public final List<OrderByContext> parseOrderBy(final SelectSQLContext sqlContext) {
        if (!exprParser.skipIfEqual(DefaultKeyword.ORDER)) {
            return Collections.emptyList();
        }
        List<OrderByContext> result = new LinkedList<>();
        exprParser.skipIfEqual(DefaultKeyword.SIBLINGS);
        exprParser.accept(DefaultKeyword.BY);
        do {
            Optional<OrderByContext> orderByContext = parseSelectOrderByItem(sqlContext);
            if (orderByContext.isPresent()) {
                result.add(orderByContext.get());
            }
        }
        while (exprParser.skipIfEqual(Symbol.COMMA));
        return result;
    }
    
    protected Optional<OrderByContext> parseSelectOrderByItem(final SelectSQLContext sqlContext) {
        SQLExpr expr = exprParser.parseExpression(sqlContext);
        OrderType orderByType = OrderType.ASC;
        if (exprParser.skipIfEqual(DefaultKeyword.ASC)) {
            orderByType = OrderType.ASC;
        } else if (exprParser.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        OrderByContext result;
        if (expr instanceof SQLNumberExpr) {
            result = new OrderByContext(((SQLNumberExpr) expr).getNumber().intValue(), orderByType);
        } else if (expr instanceof SQLIdentifierExpr) {
            result = new OrderByContext(SQLUtil.getExactlyValue(((SQLIdentifierExpr) expr).getName()), orderByType, getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpr) expr).getName())));
        } else if (expr instanceof SQLPropertyExpr) {
            SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) expr;
            result = new OrderByContext(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpr.getName()), orderByType, 
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpr.getName())));
        } else {
            return Optional.absent();
        }
        if (!result.getIndex().isPresent()) {
            boolean found = false;
            String orderByExpression = result.getOwner().isPresent() ? result.getOwner().get() + "." + result.getName().get() : result.getName().get();
            for (SelectItemContext context : sqlContext.getItemContexts()) {
                if (context.getExpression().equalsIgnoreCase(orderByExpression) || orderByExpression.equalsIgnoreCase(context.getAlias().orNull())) {
                    found = true;
                    break;
                }
            }
            // TODO 需重构,目前的做法是通过补列有别名则补列,如果不包含select item则生成别名,进而补列,这里逻辑不直观
            if (!found && result.getAlias().isPresent()) {
                itemsToken.getItems().add(orderByExpression + " AS " + result.getAlias().get() + " ");
            }
        }
        return Optional.of(result);
    }
    
    protected void parseGroupBy() {
        if (getExprParser().skipIfEqual(DefaultKeyword.GROUP)) {
            getExprParser().accept(DefaultKeyword.BY);
            while (true) {
                addGroupByItem(exprParser.parseExpression(sqlContext));
                if (!getExprParser().equalAny(Symbol.COMMA)) {
                    break;
                }
                getExprParser().getLexer().nextToken();
            }
            while (getExprParser().equalAny(DefaultKeyword.WITH) || getExprParser().getLexer().getCurrentToken().getLiterals().equalsIgnoreCase("ROLLUP")) {
                getExprParser().getLexer().nextToken();
            }
            if (getExprParser().skipIfEqual(DefaultKeyword.HAVING)) {
                exprParser.parseExpression(sqlContext);
            }
        } else if (getExprParser().skipIfEqual(DefaultKeyword.HAVING)) {
            exprParser.parseExpression(sqlContext);
        }
    }
    
    protected final void addGroupByItem(final SQLExpr sqlExpr) {
        OrderType orderByType = OrderType.ASC;
        if (getExprParser().equalAny(DefaultKeyword.ASC)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        GroupByContext groupByContext;
        if (sqlExpr instanceof SQLPropertyExpr) {
            SQLPropertyExpr expr = (SQLPropertyExpr) sqlExpr;
            groupByContext = new GroupByContext(Optional.of(SQLUtil.getExactlyValue(expr.getOwner().getName())), SQLUtil.getExactlyValue(expr.getName()), orderByType,
                    getAlias(SQLUtil.getExactlyValue(expr.getOwner() + "." + SQLUtil.getExactlyValue(expr.getName()))));
        } else if (sqlExpr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr expr = (SQLIdentifierExpr) sqlExpr;
            groupByContext = new GroupByContext(Optional.<String>absent(), SQLUtil.getExactlyValue(expr.getName()), orderByType, getAlias(SQLUtil.getExactlyValue(expr.getName())));
        } else {
            return;
        }
        sqlContext.getGroupByContexts().add(groupByContext);
    
        boolean found = false;
        String groupByExpression = groupByContext.getOwner().isPresent() ? groupByContext.getOwner().get() + "." + groupByContext.getName() : groupByContext.getName();
        for (SelectItemContext context : sqlContext.getItemContexts()) {
            if ((!context.getAlias().isPresent() && context.getExpression().equalsIgnoreCase(groupByExpression))
                    || (context.getAlias().isPresent() && context.getAlias().get().equalsIgnoreCase(groupByExpression))) {
                found = true;
                break;
            }
        }
        // TODO 需重构,目前的做法是通过补列有别名则补列,如果不包含select item则生成别名,进而补列,这里逻辑不直观
        if (!found && groupByContext.getAlias().isPresent()) {
            itemsToken.getItems().add(groupByExpression + " AS " + groupByContext.getAlias().get() + " ");
        }
    }
    
    private Optional<String> getAlias(final String name) {
        if (sqlContext.isContainStar()) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItemContext each : sqlContext.getItemContexts()) {
            if (rawName.equalsIgnoreCase(SQLUtil.getExactlyValue(each.getExpression()))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
            }
        }
        return Optional.of(generateDerivedColumnAlias());
    }
    
    private String generateDerivedColumnAlias() {
        return String.format(SHARDING_GEN_ALIAS, ++derivedColumnOffset);
    }
    
    public final void parseFrom() {
        if (getExprParser().skipIfEqual(DefaultKeyword.FROM)) {
            parseTable();
        }
    }
    
    public void parseTable() {
        if (getExprParser().equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        parseTableFactor();
        parseJoinTable();
    }
    
    protected final void parseTableFactor() {
        int beginPosition = getExprParser().getLexer().getCurrentToken().getEndPosition() - getExprParser().getLexer().getCurrentToken().getLiterals().length();
        String literals = getExprParser().getLexer().getCurrentToken().getLiterals();
        getExprParser().getLexer().nextToken();
        // TODO 包含Schema解析
        if (getExprParser().skipIfEqual(Symbol.DOT)) {
            getExprParser().getLexer().nextToken();
            getExprParser().parseAlias();
            return;
        }
        // FIXME 根据shardingRule过滤table
        exprParser.getSqlBuilderContext().getSqlTokens().add(new TableToken(beginPosition, literals, SQLUtil.getExactlyValue(literals)));
        sqlContext.getTables().add(new TableContext(literals, SQLUtil.getExactlyValue(literals), getExprParser().parseAlias()));
    }
    
    protected void parseJoinTable() {
        if (getExprParser().skipJoin()) {
            parseTable();
            if (getExprParser().skipIfEqual(DefaultKeyword.ON)) {
                do {
                    parseTableCondition(getExprParser().getLexer().getCurrentToken().getEndPosition());
                    getExprParser().accept(Symbol.EQ);
                    parseTableCondition(getExprParser().getLexer().getCurrentToken().getEndPosition() - getExprParser().getLexer().getCurrentToken().getLiterals().length());
                } while (getExprParser().skipIfEqual(DefaultKeyword.AND));
            } else if (getExprParser().skipIfEqual(DefaultKeyword.USING)) {
                getExprParser().skipParentheses();
            }
            parseJoinTable();
        }
    }
    
    private void parseTableCondition(final int startPosition) {
        SQLExpr sqlExpr = exprParser.parseExpression();
        if (!(sqlExpr instanceof SQLPropertyExpr)) {
            return;
        }
        SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
        for (TableContext each : sqlContext.getTables()) {
            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName()))) {
                exprParser.getSqlBuilderContext().getSqlTokens().add(
                        new TableToken(startPosition, sqlPropertyExpr.getOwner().getName(), SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName())));
            }
        }
    }
}
