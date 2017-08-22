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

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.CommonSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.StarSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.AliasSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.ExpressionSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.TableSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.WhereSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OrderByToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collection;
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
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AliasSQLParser aliasSQLParser;
    
    private final ExpressionSQLParser expressionSQLParser;
    
    private final TableSQLParser tableSQLParser;
    
    private final WhereSQLParser whereSQLParser;
    
    private final List<SelectItem> items = new LinkedList<>();
    
    @Setter
    private int parametersIndex;
    
    public AbstractSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine, final WhereSQLParser whereSQLParser) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        aliasSQLParser = new AliasSQLParser(lexerEngine);
        expressionSQLParser = new ExpressionSQLParser(lexerEngine);
        this.whereSQLParser = whereSQLParser;
        tableSQLParser = new TableSQLParser(lexerEngine);
    }
    
    @Override
    public final SelectStatement parse() {
        SelectStatement result = parseInternal();
        if (result.containsSubQuery()) {
            result = result.mergeSubQueryStatement();
        }
        // TODO move to rewrite
        appendDerivedColumns(result);
        appendDerivedOrderBy(result);
        return result;
    }
    
    private SelectStatement parseInternal() {
        SelectStatement result = new SelectStatement();
        lexerEngine.nextToken();
        parseInternal(result);
        return result;
    }
    
    protected abstract void parseInternal(final SelectStatement selectStatement);
    
    protected final void parseDistinct() {
        lexerEngine.skipAll(DefaultKeyword.ALL);
        Collection<Keyword> distinctKeywords = new LinkedList<>();
        distinctKeywords.add(DefaultKeyword.DISTINCT);
        distinctKeywords.addAll(Arrays.asList(getSynonymousKeywordsForDistinct()));
        if (lexerEngine.equalAny(distinctKeywords.toArray(new Keyword[distinctKeywords.size()]))) {
            throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
        }
    }
    
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[0];
    }
    
    protected final void parseSelectList(final SelectStatement selectStatement) {
        do {
            selectStatement.getItems().add(parseSelectItem(selectStatement));
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.setSelectListLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
        items.addAll(selectStatement.getItems());
    }
    
    private SelectItem parseSelectItem(final SelectStatement selectStatement) {
        lexerEngine.skipIfEqual(getSkippedKeywordsBeforeSelectItem());
        SelectItem result;
        if (isRowNumberSelectItem()) {
            result = parseRowNumberSelectItem(selectStatement);
        } else if (isStarSelectItem()) {
            selectStatement.setContainStar(true);
            result = parseStarSelectItem();
        } else if (isAggregationSelectItem()) {
            result = parseAggregationSelectItem(selectStatement);
            parseRestSelectItem(selectStatement);
        } else {
            result = new CommonSelectItem(SQLUtil.getExactlyValue(parseCommonSelectItem(selectStatement) + parseRestSelectItem(selectStatement)), aliasSQLParser.parse());
        }
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBeforeSelectItem() {
        return new Keyword[0];
    }
    
    protected boolean isRowNumberSelectItem() {
        return false;
    }
    
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        throw new UnsupportedOperationException("Cannot support special select item.");
    }
    
    private boolean isStarSelectItem() {
        return Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals()));
    }
    
    private SelectItem parseStarSelectItem() {
        lexerEngine.nextToken();
        aliasSQLParser.parse();
        return new StarSelectItem(Optional.<String>absent());
    }
    
    private boolean isAggregationSelectItem() {
        return lexerEngine.equalAny(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT);
    }
    
    private SelectItem parseAggregationSelectItem(final SelectStatement selectStatement) {
        AggregationType aggregationType = AggregationType.valueOf(lexerEngine.getCurrentToken().getLiterals().toUpperCase());
        lexerEngine.nextToken();
        return new AggregationSelectItem(aggregationType, lexerEngine.skipParentheses(selectStatement), aliasSQLParser.parse());
    }
    
    private String parseCommonSelectItem(final SelectStatement selectStatement) {
        String literals = lexerEngine.getCurrentToken().getLiterals();
        int position = lexerEngine.getCurrentToken().getEndPosition() - literals.length();
        StringBuilder result = new StringBuilder();
        result.append(literals);
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            result.append(lexerEngine.skipParentheses(selectStatement));
        } else if (lexerEngine.equalAny(Symbol.DOT)) {
            String tableName = SQLUtil.getExactlyValue(literals);
            if (shardingRule.tryFindTableRule(tableName).isPresent() || shardingRule.findBindingTableRule(tableName).isPresent()) {
                selectStatement.getSqlTokens().add(new TableToken(position, literals));
            }
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
        }
        return result.toString();
    }
    
    private String parseRestSelectItem(final SelectStatement selectStatement) {
        StringBuilder result = new StringBuilder();
        while (lexerEngine.equalAny(Symbol.getOperators())) {
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            result.append(parseCommonSelectItem(selectStatement));
        }
        return result.toString();
    }
    
    protected final void parseFrom(final SelectStatement selectStatement) {
        if (lexerEngine.equalAny(DefaultKeyword.INTO)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.INTO);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.FROM)) {
            parseTable(selectStatement);
        }
    }
    
    private void parseTable(final SelectStatement selectStatement) {
        if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
            lexerEngine.skipUselessParentheses();
            selectStatement.setSubQueryStatement(parseInternal());
            lexerEngine.skipUselessParentheses();
            if (lexerEngine.equalAny(DefaultKeyword.WHERE, Assist.END)) {
                return;
            }
        }
        parseTableFactor(selectStatement);
        parseJoinTable(selectStatement);
    }
    
    protected void parseTableFactor(final SelectStatement selectStatement) {
        parseTableFactorInternal(selectStatement);
    }
    
    protected final void parseTableFactorInternal(final SelectStatement selectStatement) {
        lexerEngine.skipAll(DefaultKeyword.AS);
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.DOT)) {
            throw new UnsupportedOperationException("Cannot support SQL for `schema.table`");
        }
        String tableName = SQLUtil.getExactlyValue(literals);
        Optional<String> alias = aliasSQLParser.parse();
        if (shardingRule.tryFindTableRule(tableName).isPresent() || shardingRule.findBindingTableRule(tableName).isPresent()) {
            selectStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
            selectStatement.getTables().add(new Table(tableName, alias));
        }
    }
    
    protected void parseJoinTable(final SelectStatement selectStatement) {
        if (tableSQLParser.skipJoin()) {
            parseTable(selectStatement);
            if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
                do {
                    expressionSQLParser.parse(selectStatement);
                    lexerEngine.accept(Symbol.EQ);
                    expressionSQLParser.parse(selectStatement);
                } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
            } else if (lexerEngine.skipIfEqual(DefaultKeyword.USING)) {
                lexerEngine.skipParentheses(selectStatement);
            }
            parseJoinTable(selectStatement);
        }
    }
    
    protected final void parseWhere(final SelectStatement selectStatement) {
        whereSQLParser.parseWhere(shardingRule, selectStatement, items);
        parametersIndex = selectStatement.getParametersIndex();
    }
    
    protected final void parseGroupBy(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.GROUP)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.BY);
        while (true) {
            addGroupByItem(expressionSQLParser.parse(selectStatement), selectStatement);
            if (!lexerEngine.equalAny(Symbol.COMMA)) {
                break;
            }
            lexerEngine.nextToken();
        }
        lexerEngine.skipAll(getSkippedKeywordAfterGroupBy());
        selectStatement.setGroupByLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
    }
    
    private void addGroupByItem(final SQLExpression sqlExpression, final SelectStatement selectStatement) {
        if (lexerEngine.equalAny(getUnsupportedKeywordBeforeGroupByItem())) {
            throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
        }
        OrderType orderByType = OrderType.ASC;
        if (lexerEngine.equalAny(DefaultKeyword.ASC)) {
            lexerEngine.nextToken();
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        OrderItem orderItem;
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType, getNullOrderType(), 
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName())), selectStatement));
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            SQLIdentifierExpression sqlIdentifierExpression = (SQLIdentifierExpression) sqlExpression;
            orderItem = new OrderItem(
                    SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), orderByType, getNullOrderType(), getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), selectStatement));
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            orderItem = new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, getNullOrderType(), getAlias(sqlIgnoreExpression.getExpression(), selectStatement));
        } else {
            return;
        }
        selectStatement.getGroupByItems().add(orderItem);
    }
    
    protected Keyword[] getUnsupportedKeywordBeforeGroupByItem() {
        return new Keyword[0];
    }
    
    protected Keyword[] getSkippedKeywordAfterGroupBy() {
        return new Keyword[0];
    }
    
    protected final void parseHaving() {
        if (lexerEngine.equalAny(DefaultKeyword.HAVING)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.HAVING);
        }
    }
    
    protected final void parseOrderBy(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.ORDER)) {
            return;
        }
        List<OrderItem> result = new LinkedList<>();
        lexerEngine.skipIfEqual(DefaultKeyword.SIBLINGS);
        lexerEngine.accept(DefaultKeyword.BY);
        do {
            result.add(parseSelectOrderByItem(selectStatement));
        }
        while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.getOrderByItems().addAll(result);
    }
    
    private OrderItem parseSelectOrderByItem(final SelectStatement selectStatement) {
        SQLExpression sqlExpression = expressionSQLParser.parse(selectStatement);
        OrderType orderByType = OrderType.ASC;
        if (lexerEngine.skipIfEqual(DefaultKeyword.ASC)) {
            orderByType = OrderType.ASC;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return new OrderItem(((SQLNumberExpression) sqlExpression).getNumber().intValue(), orderByType, getNullOrderType());
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return new OrderItem(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()), 
                    orderByType, getNullOrderType(), getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()), selectStatement));
        }
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            return new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType, getNullOrderType(), 
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), selectStatement));
        }
        if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            return new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, getNullOrderType(), getAlias(sqlIgnoreExpression.getExpression(), selectStatement));
        }
        throw new SQLParsingException(lexerEngine);
    }
    
    protected abstract OrderType getNullOrderType();
    
    private Optional<String> getAlias(final String name, final SelectStatement selectStatement) {
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
    
    protected final void parseRest() {
        Collection<Keyword> unsupportedRestKeywords = new LinkedList<>();
        unsupportedRestKeywords.addAll(Arrays.asList(DefaultKeyword.UNION, DefaultKeyword.INTERSECT, DefaultKeyword.EXCEPT, DefaultKeyword.MINUS));
        unsupportedRestKeywords.addAll(Arrays.asList(getUnsupportedKeywordsRest()));
        if (lexerEngine.equalAny(unsupportedRestKeywords.toArray(new Keyword[unsupportedRestKeywords.size()]))) {
            throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
        }
    }
    
    protected Keyword[] getUnsupportedKeywordsRest() {
        return new Keyword[0];
    }
    
    private void appendDerivedColumns(final SelectStatement selectStatement) {
        ItemsToken itemsToken = new ItemsToken(selectStatement.getSelectListLastPosition());
        appendAvgDerivedColumns(itemsToken, selectStatement);
        appendDerivedOrderColumns(itemsToken, selectStatement.getOrderByItems(), ORDER_BY_DERIVED_ALIAS, selectStatement);
        appendDerivedOrderColumns(itemsToken, selectStatement.getGroupByItems(), GROUP_BY_DERIVED_ALIAS, selectStatement);
        if (!itemsToken.getItems().isEmpty()) {
            selectStatement.getSqlTokens().add(itemsToken);
        }
    }
    
    private void appendAvgDerivedColumns(final ItemsToken itemsToken, final SelectStatement selectStatement) {
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
    
    private void appendDerivedOrderColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final String aliasPattern, final SelectStatement selectStatement) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!isContainsItem(each, selectStatement)) {
                String alias = String.format(aliasPattern, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }
    
    private boolean isContainsItem(final OrderItem orderItem, final SelectStatement selectStatement) {
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
    
    private void appendDerivedOrderBy(final SelectStatement selectStatement) {
        if (!selectStatement.getGroupByItems().isEmpty() && selectStatement.getOrderByItems().isEmpty()) {
            selectStatement.getOrderByItems().addAll(selectStatement.getGroupByItems());
            selectStatement.getSqlTokens().add(new OrderByToken(selectStatement.getGroupByLastPosition()));
        }
    }
}
