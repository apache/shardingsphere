/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.sql.dql.select;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.context.table.Tables;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Select parser.
 * 
 * @author zhangliang 
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSelectParser implements SQLParser {
    
    private static final String DERIVED_COUNT_ALIAS = "AVG_DERIVED_COUNT_%s";
    
    private static final String DERIVED_SUM_ALIAS = "AVG_DERIVED_SUM_%s";
    
    private static final String ORDER_BY_DERIVED_ALIAS = "ORDER_BY_DERIVED_%s";
    
    private static final String GROUP_BY_DERIVED_ALIAS = "GROUP_BY_DERIVED_%s";
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AbstractSelectClauseParserFacade selectClauseParserFacade;
    
    private final List<SelectItem> items = new LinkedList<>();
    
    private final ShardingMetaData shardingMetaData;
    
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
    
    protected abstract void parseInternal(SelectStatement selectStatement);
    
    protected final void parseDistinct() {
        selectClauseParserFacade.getDistinctClauseParser().parse();
    }
    
    protected final void parseSelectList(final SelectStatement selectStatement, final List<SelectItem> items) {
        selectClauseParserFacade.getSelectListClauseParser().parse(selectStatement, items);
    }
    
    protected final void parseFrom(final SelectStatement selectStatement) {
        lexerEngine.unsupportedIfEqual(DefaultKeyword.INTO);
        if (lexerEngine.skipIfEqual(DefaultKeyword.FROM)) {
            parseTable(selectStatement);
        }
    }
    
    private void parseTable(final SelectStatement selectStatement) {
        if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
            selectStatement.setSubQueryStatement(parseInternal());
            if (lexerEngine.equalAny(DefaultKeyword.WHERE, Assist.END)) {
                return;
            }
        }
        selectClauseParserFacade.getTableReferencesClauseParser().parse(selectStatement, false);
    }
    
    protected final void parseWhere(final ShardingRule shardingRule, final SelectStatement selectStatement, final List<SelectItem> items) {
        selectClauseParserFacade.getWhereClauseParser().parse(shardingRule, selectStatement, items);
    }
    
    protected final void parseGroupBy(final SelectStatement selectStatement) {
        selectClauseParserFacade.getGroupByClauseParser().parse(selectStatement);
    }
    
    protected final void parseHaving() {
        selectClauseParserFacade.getHavingClauseParser().parse();
    }
    
    protected final void parseOrderBy(final SelectStatement selectStatement) {
        selectClauseParserFacade.getOrderByClauseParser().parse(selectStatement);
    }
    
    protected final void parseSelectRest() {
        selectClauseParserFacade.getSelectRestClauseParser().parse();
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
            // TODO replace avg to constant, avoid calculate useless avg
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
        if (-1 != orderItem.getIndex()) {
            return true;
        }
        if (!selectStatement.getStarSelectItems().isEmpty()) {
            return isContainsItemInStarSelectItem(selectStatement.getStarSelectItems(), orderItem, selectStatement.getTables());
        }
        for (SelectItem each : selectStatement.getItems()) {
            if (each.getAlias().isPresent() && orderItem.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(orderItem.getAlias().get())) {
                return true;
            }
            if (!each.getAlias().isPresent() && orderItem.getQualifiedName().isPresent() && each.getExpression().equalsIgnoreCase(orderItem.getQualifiedName().get())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isContainsItemInStarSelectItem(final List<StarSelectItem> starSelectItems, final OrderItem orderItem, final Tables tables) {
        for (StarSelectItem each : starSelectItems) {
            if (!each.getOwner().isPresent()) {
                return true;
            }
            Optional<Table> tableOptionalOfStarSelectItem = tables.find(each.getOwner().get());
            if (orderItem.getOwner().isPresent() && tables.find(orderItem.getOwner().get()).equals(tableOptionalOfStarSelectItem)) {
                return true;
            }
            List<String> columnNames = tableOptionalOfStarSelectItem.isPresent()
                    ? shardingMetaData.getTableMetaDataMap().get(tableOptionalOfStarSelectItem.get().getName()).getAllColumnNames() : new ArrayList<String>();
            if (columnNames.contains(orderItem.getName().get().toUpperCase()) || columnNames.contains(orderItem.getName().get().toLowerCase())) {
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
