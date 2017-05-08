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
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSelectParser implements SQLStatementParser {
    
    private static final String SHARDING_GEN_ALIAS = "sharding_gen_%s";
    
    private SQLParser sqlParser;
    
    private final SelectSQLContext sqlContext;
    
    @Setter
    private int parametersIndex;
    
    private int derivedColumnOffset;
    
    private ItemsToken itemsToken;
    
    public AbstractSelectParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        sqlContext = new SelectSQLContext();
        sqlContext.setSqlBuilderContext(sqlParser.getSqlBuilderContext());
    }
    
    @Override
    public final SelectSQLContext parse() {
        query();
        sqlContext.getOrderByContexts().addAll(parseOrderBy(getSqlContext()));
        customizedSelect();
        if (!itemsToken.getItems().isEmpty()) {
            sqlParser.getSqlBuilderContext().getSqlTokens().add(itemsToken);
        }
        return sqlContext;
    }
    
    protected void customizedSelect() {
    }
    
    protected void query() {
        getSqlParser().accept(DefaultKeyword.SELECT);
        parseDistinct();
        parseSelectList();
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
    }
    
    protected final void parseDistinct() {
        if (getSqlParser().equalAny(DefaultKeyword.DISTINCT, DefaultKeyword.DISTINCTROW, DefaultKeyword.UNION)) {
            sqlContext.setDistinct(true);
            getSqlParser().getLexer().nextToken();
            if (hasDistinctOn() && getSqlParser().equalAny(DefaultKeyword.ON)) {
                getSqlParser().getLexer().nextToken();
                getSqlParser().skipParentheses();
            }
        } else if (getSqlParser().equalAny(DefaultKeyword.ALL)) {
            getSqlParser().getLexer().nextToken();
        }
    }
    
    protected boolean hasDistinctOn() {
        return false;
    }
    
    protected final void parseSelectList() {
        int index = 1;
        do {
            SelectItemContext selectItemContext = sqlParser.parseSelectItem(index);
            sqlContext.getItemContexts().add(selectItemContext);
            if (selectItemContext instanceof CommonSelectItemContext && ((CommonSelectItemContext) selectItemContext).isStar()) {
                sqlContext.setContainStar(true);
            }
            index++;
        } while (getSqlParser().skipIfEqual(Symbol.COMMA));
        sqlContext.setSelectListLastPosition(getSqlParser().getLexer().getCurrentToken().getEndPosition() - getSqlParser().getLexer().getCurrentToken().getLiterals().length());
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
        if (getSqlParser().equalAny(DefaultKeyword.UNION, DefaultKeyword.EXCEPT, DefaultKeyword.INTERSECT, DefaultKeyword.MINUS)) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
    }
    
    protected final void parseWhere() {
        if (sqlContext.getTables().isEmpty()) {
            return;
        }
        sqlParser.parseWhere(sqlContext);
        parametersIndex = sqlParser.getParametersIndex();
    }
    
    /**
     * 解析排序.
     *
     * @param sqlContext SQL上下文
     * @return 排序上下文
     */
    public final List<OrderByContext> parseOrderBy(final SelectSQLContext sqlContext) {
        if (!sqlParser.skipIfEqual(DefaultKeyword.ORDER)) {
            return Collections.emptyList();
        }
        List<OrderByContext> result = new LinkedList<>();
        sqlParser.skipIfEqual(DefaultKeyword.SIBLINGS);
        sqlParser.accept(DefaultKeyword.BY);
        do {
            Optional<OrderByContext> orderByContext = parseSelectOrderByItem(sqlContext);
            if (orderByContext.isPresent()) {
                result.add(orderByContext.get());
            }
        }
        while (sqlParser.skipIfEqual(Symbol.COMMA));
        return result;
    }
    
    protected Optional<OrderByContext> parseSelectOrderByItem(final SelectSQLContext sqlContext) {
        SQLExpr expr = sqlParser.parseExpression(sqlContext);
        OrderType orderByType = OrderType.ASC;
        if (sqlParser.skipIfEqual(DefaultKeyword.ASC)) {
            orderByType = OrderType.ASC;
        } else if (sqlParser.skipIfEqual(DefaultKeyword.DESC)) {
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
        if (getSqlParser().skipIfEqual(DefaultKeyword.GROUP)) {
            getSqlParser().accept(DefaultKeyword.BY);
            while (true) {
                addGroupByItem(sqlParser.parseExpression(sqlContext));
                if (!getSqlParser().equalAny(Symbol.COMMA)) {
                    break;
                }
                getSqlParser().getLexer().nextToken();
            }
            while (getSqlParser().equalAny(DefaultKeyword.WITH) || getSqlParser().getLexer().getCurrentToken().getLiterals().equalsIgnoreCase("ROLLUP")) {
                getSqlParser().getLexer().nextToken();
            }
            if (getSqlParser().skipIfEqual(DefaultKeyword.HAVING)) {
                sqlParser.parseExpression(sqlContext);
            }
        } else if (getSqlParser().skipIfEqual(DefaultKeyword.HAVING)) {
            sqlParser.parseExpression(sqlContext);
        }
    }
    
    protected final void addGroupByItem(final SQLExpr sqlExpr) {
        OrderType orderByType = OrderType.ASC;
        if (getSqlParser().equalAny(DefaultKeyword.ASC)) {
            getSqlParser().getLexer().nextToken();
        } else if (getSqlParser().skipIfEqual(DefaultKeyword.DESC)) {
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
        if (getSqlParser().skipIfEqual(DefaultKeyword.FROM)) {
            parseTable();
        }
    }
    
    public void parseTable() {
        if (getSqlParser().equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        parseTableFactor();
        parseJoinTable();
    }
    
    protected final void parseTableFactor() {
        int beginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition() - getSqlParser().getLexer().getCurrentToken().getLiterals().length();
        String literals = getSqlParser().getLexer().getCurrentToken().getLiterals();
        getSqlParser().getLexer().nextToken();
        // TODO 包含Schema解析
        if (getSqlParser().skipIfEqual(Symbol.DOT)) {
            getSqlParser().getLexer().nextToken();
            getSqlParser().parseAlias();
            return;
        }
        // FIXME 根据shardingRule过滤table
        sqlParser.getSqlBuilderContext().getSqlTokens().add(new TableToken(beginPosition, literals, SQLUtil.getExactlyValue(literals)));
        sqlContext.getTables().add(new TableContext(literals, SQLUtil.getExactlyValue(literals), getSqlParser().parseAlias()));
    }
    
    protected void parseJoinTable() {
        if (getSqlParser().skipJoin()) {
            parseTable();
            if (getSqlParser().skipIfEqual(DefaultKeyword.ON)) {
                do {
                    parseTableCondition(getSqlParser().getLexer().getCurrentToken().getEndPosition());
                    getSqlParser().accept(Symbol.EQ);
                    parseTableCondition(getSqlParser().getLexer().getCurrentToken().getEndPosition() - getSqlParser().getLexer().getCurrentToken().getLiterals().length());
                } while (getSqlParser().skipIfEqual(DefaultKeyword.AND));
            } else if (getSqlParser().skipIfEqual(DefaultKeyword.USING)) {
                getSqlParser().skipParentheses();
            }
            parseJoinTable();
        }
    }
    
    private void parseTableCondition(final int startPosition) {
        SQLExpr sqlExpr = sqlParser.parseExpression();
        if (!(sqlExpr instanceof SQLPropertyExpr)) {
            return;
        }
        SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
        for (TableContext each : sqlContext.getTables()) {
            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName()))) {
                sqlParser.getSqlBuilderContext().getSqlTokens().add(
                        new TableToken(startPosition, sqlPropertyExpr.getOwner().getName(), SQLUtil.getExactlyValue(sqlPropertyExpr.getOwner().getName())));
            }
        }
    }
}
