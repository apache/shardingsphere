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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Token;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.CommonSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OrderByToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Select语句解析器.
 * 
 * @author zhangliang 
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSelectParser implements SQLStatementParser {
    
    private static final String DERIVED_COUNT_ALIAS = "AVG_DERIVED_COUNT_%s";
    
    private static final String DERIVED_SUM_ALIAS = "AVG_DERIVED_SUM_%s";
    
    private static final String ORDER_BY_DERIVED_ALIAS = "ORDER_BY_DERIVED_%s";
    
    private static final String GROUP_BY_DERIVED_ALIAS = "GROUP_BY_DERIVED_%s";
    
    private final AbstractSQLParser sqlParser;
    
    private final SelectStatement selectStatement;
    
    @Setter
    private int parametersIndex;
    
    @Setter
    private boolean isInSubQuery;
    
    private boolean appendDerivedColumnsFlag;
    
    public AbstractSelectParser(final AbstractSQLParser sqlParser) {
        this.sqlParser = sqlParser;
        selectStatement = new SelectStatement();
    }
    
    @Override
    public final SelectStatement parse() {
        sqlParser.getLexer().nextToken();
        parseDistinct();
        parseBeforeSelectList();
        parseSelectList();
        parseFrom();
        parseWhere();
        customizedBetweenWhereAndGroupBy();
        parseGroupBy();
        customizedBetweenGroupByAndOrderBy();
        parseOrderBy();
        customizedSelect();
        processUnsupportedTokens();
        // TODO move to rewrite
        appendDerivedColumns();
        appendDerivedOrderBy();
        return selectStatement;
    }
    
    private void parseDistinct() {
        sqlParser.skipAll(DefaultKeyword.ALL);
        Collection<Keyword> distinctKeywords = Lists.newLinkedList(getCustomizedDistinctKeywords());
        distinctKeywords.add(DefaultKeyword.DISTINCT);
        if (getSqlParser().equalAny(distinctKeywords.toArray(new Keyword[distinctKeywords.size()]))) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
    }
    
    protected Collection<Keyword> getCustomizedDistinctKeywords() {
        return Collections.emptyList();
    }
    
    protected void parseBeforeSelectList() {
    }
    
    protected final void parseSelectList() {
        do {
            parseSelectItem();
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
        if (0 == selectStatement.getSelectListLastPosition()) {
            selectStatement.setSelectListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
        }
    }
    
    private void parseSelectItem() {
        if (isRowNumberSelectItem()) {
            selectStatement.getItems().add(parseRowNumberSelectItem());
            return;
        }
        sqlParser.skipIfEqual(DefaultKeyword.CONNECT_BY_ROOT);
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        if (isStarSelectItem(literals)) {
            selectStatement.getItems().add(parseStarSelectItem());
            return;
        }
        if (isAggregationSelectItem()) {
            selectStatement.getItems().add(parseAggregationSelectItem(literals));
            return;
        }
        StringBuilder expression = new StringBuilder();
        Token lastToken = null;
        while (!sqlParser.equalAny(DefaultKeyword.AS) && !sqlParser.equalAny(Symbol.COMMA) && !sqlParser.equalAny(DefaultKeyword.FROM) && !sqlParser.equalAny(Assist.END)) {
            String value = sqlParser.getLexer().getCurrentToken().getLiterals();
            int position = sqlParser.getLexer().getCurrentToken().getEndPosition() - value.length();
            expression.append(value);
            lastToken = sqlParser.getLexer().getCurrentToken();
            sqlParser.getLexer().nextToken();
            if (sqlParser.equalAny(Symbol.DOT)) {
                selectStatement.getSqlTokens().add(new TableToken(position, value));
            }
        }
        if (hasAlias(expression, lastToken)) {
            selectStatement.getItems().add(parseSelectItemWithAlias(expression, lastToken));
            return;
        }
        selectStatement.getItems().add(new CommonSelectItem(SQLUtil.getExactlyValue(expression.toString()), sqlParser.parseAlias()));
    }
    
    protected boolean isRowNumberSelectItem() {
        return false;
    }
    
    protected SelectItem parseRowNumberSelectItem() {
        throw new UnsupportedOperationException("Cannot support special select item.");
    }
    
    private boolean isStarSelectItem(final String literals) {
        return sqlParser.equalAny(Symbol.STAR) || Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(literals));
    }
    
    private SelectItem parseStarSelectItem() {
        sqlParser.getLexer().nextToken();
        selectStatement.setContainStar(true);
        return new CommonSelectItem(Symbol.STAR.getLiterals(), sqlParser.parseAlias());
    }
    
    private boolean isAggregationSelectItem() {
        return sqlParser.skipIfEqual(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT);
    }
    
    private SelectItem parseAggregationSelectItem(final String literals) {
        return new AggregationSelectItem(AggregationType.valueOf(literals.toUpperCase()), sqlParser.skipParentheses(), sqlParser.parseAlias());
    }
    
    private boolean hasAlias(final StringBuilder expression, final Token lastToken) {
        return null != lastToken && Literals.IDENTIFIER == lastToken.getType() && !isSQLPropertyExpression(expression, lastToken) && !expression.toString().equals(lastToken.getLiterals());
    }
    
    private boolean isSQLPropertyExpression(final StringBuilder expression, final Token lastToken) {
        return expression.toString().endsWith(Symbol.DOT.getLiterals() + lastToken.getLiterals());
    }
    
    private CommonSelectItem parseSelectItemWithAlias(final StringBuilder expression, final Token lastToken) {
        return new CommonSelectItem(SQLUtil.getExactlyValue(expression.substring(0, expression.lastIndexOf(lastToken.getLiterals()))), Optional.of(lastToken.getLiterals()));
    }
    
    protected final void parseWhere() {
        if (selectStatement.getTables().isEmpty()) {
            return;
        }
        sqlParser.parseWhere(selectStatement);
        parametersIndex = sqlParser.getParametersIndex();
    }
    
    protected void customizedBetweenWhereAndGroupBy() {
    }
    
    protected void customizedBetweenGroupByAndOrderBy() {
    }
    
    protected final void parseOrderBy() {
        if (!sqlParser.skipIfEqual(DefaultKeyword.ORDER)) {
            return;
        }
        List<OrderItem> result = new LinkedList<>();
        sqlParser.skipIfEqual(DefaultKeyword.SIBLINGS);
        sqlParser.accept(DefaultKeyword.BY);
        do {
            OrderItem orderItem = parseSelectOrderByItem();
            if (!isInSubQuery) {
                result.add(orderItem);
            }
        }
        while (sqlParser.skipIfEqual(Symbol.COMMA));
        selectStatement.getOrderByItems().addAll(result);
    }
    
    private OrderItem parseSelectOrderByItem() {
        SQLExpression sqlExpression = sqlParser.parseExpression(selectStatement);
        OrderType orderByType = OrderType.ASC;
        if (sqlParser.skipIfEqual(DefaultKeyword.ASC)) {
            orderByType = OrderType.ASC;
        } else if (sqlParser.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        OrderItem result;
        if (sqlExpression instanceof SQLNumberExpression) {
            result = new OrderItem(((SQLNumberExpression) sqlExpression).getNumber().intValue(), orderByType);
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            result = new OrderItem(
                    SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()), orderByType, getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName())));
        } else if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            result = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType, 
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName())));
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            result = new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, getAlias(sqlIgnoreExpression.getExpression()));
        } else {
            throw new SQLParsingException(sqlParser.getLexer());
        }
        skipAfterOrderByItem();
        return result;
    }
    
    protected void skipAfterOrderByItem() {
    }
    
    protected void parseGroupBy() {
        if (sqlParser.skipIfEqual(DefaultKeyword.GROUP)) {
            sqlParser.accept(DefaultKeyword.BY);
            while (true) {
                addGroupByItem(sqlParser.parseExpression(selectStatement));
                if (!sqlParser.equalAny(Symbol.COMMA)) {
                    break;
                }
                sqlParser.getLexer().nextToken();
            }
            while (sqlParser.equalAny(DefaultKeyword.WITH) || sqlParser.getLexer().getCurrentToken().getLiterals().equalsIgnoreCase("ROLLUP")) {
                sqlParser.getLexer().nextToken();
            }
            if (sqlParser.skipIfEqual(DefaultKeyword.HAVING)) {
                throw new UnsupportedOperationException("Cannot support Having");
            }
            selectStatement.setGroupByLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - getSqlParser().getLexer().getCurrentToken().getLiterals().length());
        } else if (sqlParser.skipIfEqual(DefaultKeyword.HAVING)) {
            throw new UnsupportedOperationException("Cannot support Having");
        }
    }
    
    protected final void addGroupByItem(final SQLExpression sqlExpression) {
        OrderType orderByType = OrderType.ASC;
        if (sqlParser.equalAny(DefaultKeyword.ASC)) {
            sqlParser.getLexer().nextToken();
        } else if (sqlParser.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        OrderItem orderItem;
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType,
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()))));
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            SQLIdentifierExpression sqlIdentifierExpression = (SQLIdentifierExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), orderByType, getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName())));
        } else {
            return;
        }
        if (!isInSubQuery) {
            selectStatement.getGroupByItems().add(orderItem);
        }
    }
    
    private Optional<String> getAlias(final String name) {
        if (selectStatement.isContainStar()) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItem each : selectStatement.getItems()) {
            if (rawName.equalsIgnoreCase(SQLUtil.getExactlyValue(each.getExpression()))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
            }
        }
        return Optional.absent();
    }
    
    private void parseFrom() {
        if (getSqlParser().equalAny(DefaultKeyword.INTO)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.INTO);
        }
        if (sqlParser.skipIfEqual(DefaultKeyword.FROM)) {
            parseTable();
        }
    }
    
    private void parseTable() {
        if (sqlParser.skipIfEqual(Symbol.LEFT_PAREN)) {
            if (!selectStatement.getTables().isEmpty()) {
                throw new UnsupportedOperationException("Cannot support subquery for nested tables.");
            }
            isInSubQuery = true;
            selectStatement.setContainStar(false);
            sqlParser.skipUselessParentheses();
            parse();
            sqlParser.skipUselessParentheses();
            if (getSqlParser().equalAny(DefaultKeyword.WHERE, Assist.END)) {
                return;
            }
        }
        isInSubQuery = false;
        customizedParseTableFactor();
        parseJoinTable();
    }
    
    protected void customizedParseTableFactor() {
        parseTableFactor();
    }
    
    protected final void parseTableFactor() {
        final int beginPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
        sqlParser.skipAll(DefaultKeyword.AS);
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        sqlParser.getLexer().nextToken();
        // TODO 包含Schema解析
        if (sqlParser.skipIfEqual(Symbol.DOT)) {
            sqlParser.getLexer().nextToken();
            sqlParser.parseAlias();
            return;
        }
        // FIXME 根据shardingRule过滤table
        selectStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
        selectStatement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), sqlParser.parseAlias()));
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
        SQLExpression sqlExpression = sqlParser.parseExpression();
        if (!(sqlExpression instanceof SQLPropertyExpression)) {
            return;
        }
        SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
        if (selectStatement.getTables().getTableNames().contains(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()))) {
            selectStatement.getSqlTokens().add(new TableToken(startPosition, sqlPropertyExpression.getOwner().getName()));
        }
    }
    
    protected abstract void customizedSelect();
    
    private void processUnsupportedTokens() {
        if (sqlParser.equalAny(DefaultKeyword.UNION, DefaultKeyword.EXCEPT, DefaultKeyword.INTERSECT, DefaultKeyword.MINUS)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
    }
    
    private void appendDerivedColumns() {
        if (appendDerivedColumnsFlag) {
            return;
        }
        appendDerivedColumnsFlag = true;
        ItemsToken itemsToken = new ItemsToken(selectStatement.getSelectListLastPosition());
        appendAvgDerivedColumns(itemsToken);
        appendDerivedOrderColumns(itemsToken, selectStatement.getOrderByItems(), ORDER_BY_DERIVED_ALIAS);
        appendDerivedOrderColumns(itemsToken, selectStatement.getGroupByItems(), GROUP_BY_DERIVED_ALIAS);
        if (!itemsToken.getItems().isEmpty()) {
            selectStatement.getSqlTokens().add(itemsToken);
        }
    }
    
    private void appendAvgDerivedColumns(final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (SelectItem each : selectStatement.getItems()) {
            if (!(each instanceof AggregationSelectItem) || AggregationType.AVG != ((AggregationSelectItem) each).getType()) {
                continue;
            }
            AggregationSelectItem avgItem = (AggregationSelectItem) each;
            String countAlias = String.format(DERIVED_COUNT_ALIAS, derivedColumnOffset);
            AggregationSelectItem countItem = new AggregationSelectItem(AggregationType.COUNT, avgItem.getInnerExpression(), Optional.of(countAlias));
            String sumAlias = String.format(DERIVED_SUM_ALIAS, derivedColumnOffset);
            AggregationSelectItem sumItem = new AggregationSelectItem(AggregationType.SUM, avgItem.getInnerExpression(), Optional.of(sumAlias));
            avgItem.getDerivedAggregationSelectItems().add(countItem);
            avgItem.getDerivedAggregationSelectItems().add(sumItem);
            // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
            itemsToken.getItems().add(countItem.getExpression() + " AS " + countAlias + " ");
            itemsToken.getItems().add(sumItem.getExpression() + " AS " + sumAlias + " ");
            derivedColumnOffset++;
        }
    }
    
    private void appendDerivedOrderColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final String aliasPattern) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!isContainsItem(each)) {
                String alias = String.format(aliasPattern, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }
    
    private boolean isContainsItem(final OrderItem orderItem) {
        if (selectStatement.isContainStar()) {
            return true;
        }
        for (SelectItem each : selectStatement.getItems()) {
            if (-1 != orderItem.getIndex()) {
                return true;
            }
            if (each.getAlias().isPresent() && orderItem.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(orderItem.getAlias().get())) {
                return true;
            }
            if (!each.getAlias().isPresent() && orderItem.getQualifiedName().isPresent() && each.getExpression().equalsIgnoreCase(orderItem.getQualifiedName().get())) {
                return true;
            }
        }
        return false;
    }
    
    private void appendDerivedOrderBy() {
        if (!getSelectStatement().getGroupByItems().isEmpty() && getSelectStatement().getOrderByItems().isEmpty()) {
            getSelectStatement().getOrderByItems().addAll(getSelectStatement().getGroupByItems());
            getSelectStatement().getSqlTokens().add(new OrderByToken(getSelectStatement().getGroupByLastPosition()));
        }
    }
}
