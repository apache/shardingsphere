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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.type.select;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.CommonSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.type.SQLStatementParser;
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
    
    private static final String DERIVED_COUNT_ALIAS = "AVG_DERIVED_COUNT_%s";
    
    private static final String DERIVED_SUM_ALIAS = "AVG_DERIVED_SUM_%s";
    
    private static final String ORDER_BY_DERIVED_ALIAS = "ORDER_BY_DERIVED_%s";
    
    private static final String GROUP_BY_DERIVED_ALIAS = "GROUP_BY_DERIVED_%s";
    
    private final SQLParser sqlParser;
    
    private final SelectSQLContext sqlContext;
    
    @Setter
    private int parametersIndex;
    
    public AbstractSelectParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        sqlContext = new SelectSQLContext();
    }
    
    @Override
    public final SelectSQLContext parse() {
        query();
        sqlContext.getOrderByContexts().addAll(parseOrderBy());
        customizedSelect();
        appendDerivedColumns();
        return sqlContext;
    }
    
    protected void customizedSelect() {
    }
    
    protected void query() {
        sqlParser.accept(DefaultKeyword.SELECT);
        parseDistinct();
        parseSelectList();
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
    }
    
    protected final void parseDistinct() {
        if (sqlParser.equalAny(DefaultKeyword.DISTINCT, DefaultKeyword.DISTINCTROW, DefaultKeyword.UNION)) {
            sqlContext.setDistinct(true);
            sqlParser.getLexer().nextToken();
            if (hasDistinctOn() && sqlParser.equalAny(DefaultKeyword.ON)) {
                sqlParser.getLexer().nextToken();
                sqlParser.skipParentheses();
            }
        } else if (sqlParser.equalAny(DefaultKeyword.ALL)) {
            sqlParser.getLexer().nextToken();
        }
    }
    
    protected boolean hasDistinctOn() {
        return false;
    }
    
    protected final void parseSelectList() {
        int index = 1;
        do {
            SelectItemContext selectItemContext = parseSelectItem(index);
            sqlContext.getItemContexts().add(selectItemContext);
            if (selectItemContext instanceof CommonSelectItemContext && ((CommonSelectItemContext) selectItemContext).isStar()) {
                sqlContext.setContainStar(true);
            }
            index++;
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
        sqlContext.setSelectListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
    }
    
    private SelectItemContext parseSelectItem(final int index) {
        sqlParser.skipIfEqual(DefaultKeyword.CONNECT_BY_ROOT);
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        if (sqlParser.equalAny(Symbol.STAR) || Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(literals))) {
            sqlParser.getLexer().nextToken();
            return new CommonSelectItemContext(Symbol.STAR.getLiterals(), sqlParser.parseAlias(), true);
        }
        if (sqlParser.skipIfEqual(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT)) {
            return new AggregationSelectItemContext(sqlParser.skipParentheses(), sqlParser.parseAlias(), index, AggregationType.valueOf(literals.toUpperCase()));
        }
        StringBuilder expression = new StringBuilder();
        // FIXME 无as的alias解析, 应该做成倒数第二个token不是运算符,倒数第一个token是Identifier或char,则为别名, 不过CommonSelectItemContext类型并不关注expression和alias
        // FIXME 解析xxx.*
        while (!sqlParser.equalAny(DefaultKeyword.AS) && !sqlParser.equalAny(Symbol.COMMA) && !sqlParser.equalAny(DefaultKeyword.FROM) && !sqlParser.equalAny(Assist.END)) {
            String value = sqlParser.getLexer().getCurrentToken().getLiterals();
            int position = sqlParser.getLexer().getCurrentToken().getEndPosition() - value.length();
            expression.append(value);
            sqlParser.getLexer().nextToken();
            if (sqlParser.equalAny(Symbol.DOT)) {
                sqlContext.getSqlTokens().add(new TableToken(position, value));
            }
        }
        return new CommonSelectItemContext(SQLUtil.getExactlyValue(expression.toString()), sqlParser.parseAlias(), false);
    }
    
    protected void queryRest() {
        if (sqlParser.equalAny(DefaultKeyword.UNION, DefaultKeyword.EXCEPT, DefaultKeyword.INTERSECT, DefaultKeyword.MINUS)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
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
     * @return 排序上下文
     */
    public final List<OrderByContext> parseOrderBy() {
        if (!sqlParser.skipIfEqual(DefaultKeyword.ORDER)) {
            return Collections.emptyList();
        }
        List<OrderByContext> result = new LinkedList<>();
        sqlParser.skipIfEqual(DefaultKeyword.SIBLINGS);
        sqlParser.accept(DefaultKeyword.BY);
        do {
            Optional<OrderByContext> orderByContext = parseSelectOrderByItem();
            if (orderByContext.isPresent()) {
                result.add(orderByContext.get());
            }
        }
        while (sqlParser.skipIfEqual(Symbol.COMMA));
        return result;
    }
    
    protected Optional<OrderByContext> parseSelectOrderByItem() {
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
        return Optional.of(result);
    }
    
    protected void parseGroupBy() {
        if (sqlParser.skipIfEqual(DefaultKeyword.GROUP)) {
            sqlParser.accept(DefaultKeyword.BY);
            while (true) {
                addGroupByItem(sqlParser.parseExpression(sqlContext));
                if (!sqlParser.equalAny(Symbol.COMMA)) {
                    break;
                }
                sqlParser.getLexer().nextToken();
            }
            while (sqlParser.equalAny(DefaultKeyword.WITH) || sqlParser.getLexer().getCurrentToken().getLiterals().equalsIgnoreCase("ROLLUP")) {
                sqlParser.getLexer().nextToken();
            }
            if (sqlParser.skipIfEqual(DefaultKeyword.HAVING)) {
                sqlParser.parseExpression(sqlContext);
            }
        } else if (sqlParser.skipIfEqual(DefaultKeyword.HAVING)) {
            sqlParser.parseExpression(sqlContext);
        }
    }
    
    protected final void addGroupByItem(final SQLExpr sqlExpr) {
        OrderType orderByType = OrderType.ASC;
        if (sqlParser.equalAny(DefaultKeyword.ASC)) {
            sqlParser.getLexer().nextToken();
        } else if (sqlParser.skipIfEqual(DefaultKeyword.DESC)) {
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
        return Optional.absent();
    }
    
    public final void parseFrom() {
        if (sqlParser.skipIfEqual(DefaultKeyword.FROM)) {
            parseTable();
        }
    }
    
    public void parseTable() {
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        parseTableFactor();
        parseJoinTable();
    }
    
    protected final void parseTableFactor() {
        int beginPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        sqlParser.getLexer().nextToken();
        // TODO 包含Schema解析
        if (sqlParser.skipIfEqual(Symbol.DOT)) {
            sqlParser.getLexer().nextToken();
            sqlParser.parseAlias();
            return;
        }
        // FIXME 根据shardingRule过滤table
        sqlContext.getSqlTokens().add(new TableToken(beginPosition, literals));
        sqlContext.getTables().add(new TableContext(literals, SQLUtil.getExactlyValue(literals), sqlParser.parseAlias()));
    }
    
    protected void parseJoinTable() {
        if (sqlParser.skipJoin()) {
            parseTable();
            if (sqlParser.skipIfEqual(DefaultKeyword.ON)) {
                do {
                    parseTableCondition(sqlParser.getLexer().getCurrentToken().getEndPosition());
                    sqlParser.accept(Symbol.EQ);
                    parseTableCondition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
                } while (sqlParser.skipIfEqual(DefaultKeyword.AND));
            } else if (sqlParser.skipIfEqual(DefaultKeyword.USING)) {
                sqlParser.skipParentheses();
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
                sqlContext.getSqlTokens().add(new TableToken(startPosition, sqlPropertyExpr.getOwner().getName()));
            }
        }
    }
    
    private void appendDerivedColumns() {
        ItemsToken itemsToken = new ItemsToken(sqlContext.getSelectListLastPosition());
        appendAvgDerivedColumns(itemsToken);
        appendOrderByDerivedColumns(itemsToken);
        appendGroupByDerivedColumns(itemsToken);
        if (!itemsToken.getItems().isEmpty()) {
            sqlContext.getSqlTokens().add(itemsToken);
        }
    }
    
    private void appendAvgDerivedColumns(final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (SelectItemContext each : sqlContext.getItemContexts()) {
            if (!(each instanceof AggregationSelectItemContext) || AggregationType.AVG != ((AggregationSelectItemContext) each).getAggregationType()) {
                continue;
            }
            AggregationSelectItemContext avgContext = (AggregationSelectItemContext) each;
            String countAlias = String.format(DERIVED_COUNT_ALIAS, derivedColumnOffset);
            AggregationSelectItemContext countContext = new AggregationSelectItemContext(avgContext.getInnerExpression(), Optional.of(countAlias), -1, AggregationType.COUNT);
            String sumAlias = String.format(DERIVED_SUM_ALIAS, derivedColumnOffset);
            AggregationSelectItemContext sumContext = new AggregationSelectItemContext(avgContext.getInnerExpression(), Optional.of(sumAlias), -1, AggregationType.SUM);
            avgContext.getDerivedAggregationSelectItemContexts().add(countContext);
            avgContext.getDerivedAggregationSelectItemContexts().add(sumContext);
            // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
            itemsToken.getItems().add(countContext.getExpression() + " AS " + countAlias + " ");
            itemsToken.getItems().add(sumContext.getExpression() + " AS " + sumAlias + " ");
            derivedColumnOffset++;
        }
    }
    
    private void appendOrderByDerivedColumns(final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (OrderByContext each : sqlContext.getOrderByContexts()) {
            if (!each.getIndex().isPresent() && !each.getAlias().isPresent() && !sqlContext.isContainStar()) {
                String orderByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName().get() : each.getName().get();
                String alias = String.format(ORDER_BY_DERIVED_ALIAS, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(orderByExpression + " AS " + alias + " ");
            }
        }
    }
    
    private void appendGroupByDerivedColumns(final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (GroupByContext each : sqlContext.getGroupByContexts()) {
            if (!each.getAlias().isPresent() && !sqlContext.isContainStar()) {
                String groupByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName() : each.getName();
                String alias = String.format(GROUP_BY_DERIVED_ALIAS, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(groupByExpression + " AS " + alias + " ");
            }
        }
    }
}
