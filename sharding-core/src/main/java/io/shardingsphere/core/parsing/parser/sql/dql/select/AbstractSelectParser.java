/*
 * Copyright 2016-2018 shardingsphere.io.
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
import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingsphere.core.parsing.parser.constant.DerivedColumn;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AbstractSelectClauseParserFacade selectClauseParserFacade;
    
    private final List<SelectItem> items = new LinkedList<>();
    
    private final ShardingTableMetaData shardingTableMetaData;
    
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
        appendDerivedOrderColumns(itemsToken, selectStatement.getOrderByItems(), selectStatement);
        appendDerivedGroupColumns(itemsToken, selectStatement.getGroupByItems(), selectStatement);
        if (!itemsToken.getItems().isEmpty()) {
            selectStatement.addSQLToken(itemsToken);
        }
    }
    
    private void appendAvgDerivedColumns(final ItemsToken itemsToken, final SelectStatement selectStatement) {
        int derivedColumnOffset = 0;
        for (SelectItem each : selectStatement.getItems()) {
            if (!(each instanceof AggregationSelectItem) || AggregationType.AVG != ((AggregationSelectItem) each).getType()) {
                continue;
            }
            AggregationSelectItem avgItem = (AggregationSelectItem) each;
            String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
            AggregationSelectItem countItem = new AggregationSelectItem(AggregationType.COUNT, avgItem.getInnerExpression(), Optional.of(countAlias));
            String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
            AggregationSelectItem sumItem = new AggregationSelectItem(AggregationType.SUM, avgItem.getInnerExpression(), Optional.of(sumAlias));
            avgItem.getDerivedAggregationSelectItems().add(countItem);
            avgItem.getDerivedAggregationSelectItems().add(sumItem);
            // TODO replace avg to constant, avoid calculate useless avg
            itemsToken.getItems().add(countItem.getExpression() + " AS " + countAlias + " ");
            itemsToken.getItems().add(sumItem.getExpression() + " AS " + sumAlias + " ");
            derivedColumnOffset++;
        }
    }
    
    private void appendDerivedOrderColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final SelectStatement selectStatement) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!containsItem(selectStatement, each)) {
                String alias = DerivedColumn.ORDER_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }
    
    private void appendDerivedGroupColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final SelectStatement selectStatement) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!containsItem(selectStatement, each)) {
                String alias = DerivedColumn.GROUP_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }
    
    private boolean containsItem(final SelectStatement selectStatement, final OrderItem orderItem) {
        return orderItem.isIndex() || containsItemInStarSelectItems(selectStatement, orderItem) || containsItemInSelectItems(selectStatement, orderItem);
    }
    
    private boolean containsItemInStarSelectItems(final SelectStatement selectStatement, final OrderItem orderItem) {
        return selectStatement.hasUnqualifiedStarSelectItem() 
                || containsItemWithOwnerInStarSelectItems(selectStatement, orderItem) || containsItemWithoutOwnerInStarSelectItems(selectStatement, orderItem);
    }
    
    private boolean containsItemWithOwnerInStarSelectItems(final SelectStatement selectStatement, final OrderItem orderItem) {
        return orderItem.getOwner().isPresent() && selectStatement.findStarSelectItem(orderItem.getOwner().get()).isPresent();
    }
    
    private boolean containsItemWithoutOwnerInStarSelectItems(final SelectStatement selectStatement, final OrderItem orderItem) {
        if (!orderItem.getOwner().isPresent()) {
            for (StarSelectItem each : selectStatement.getQualifiedStarSelectItems()) {
                if (isSameSelectItem(selectStatement, each, orderItem)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isSameSelectItem(final SelectStatement selectStatement, final StarSelectItem starSelectItem, final OrderItem orderItem) {
        Preconditions.checkState(starSelectItem.getOwner().isPresent());
        Preconditions.checkState(orderItem.getName().isPresent());
        Optional<Table> table = selectStatement.getTables().find(starSelectItem.getOwner().get());
        return table.isPresent() && shardingTableMetaData.containsColumn(table.get().getName(), orderItem.getName().get());
    }
    
    private boolean containsItemInSelectItems(final SelectStatement selectStatement, final OrderItem orderItem) {
        for (SelectItem each : selectStatement.getItems()) {
            if (isSameAlias(each, orderItem) || isSameQualifiedName(each, orderItem)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameAlias(final SelectItem selectItem, final OrderItem orderItem) {
        return selectItem.getAlias().isPresent() && orderItem.getAlias().isPresent() && selectItem.getAlias().get().equalsIgnoreCase(orderItem.getAlias().get());
    }
    
    private boolean isSameQualifiedName(final SelectItem selectItem, final OrderItem orderItem) {
        return !selectItem.getAlias().isPresent() && orderItem.getQualifiedName().isPresent() && selectItem.getExpression().equalsIgnoreCase(orderItem.getQualifiedName().get());
    }
    
    private void appendDerivedOrderBy(final SelectStatement selectStatement) {
        if (!selectStatement.getGroupByItems().isEmpty() && selectStatement.getOrderByItems().isEmpty()) {
            selectStatement.getOrderByItems().addAll(selectStatement.getGroupByItems());
            selectStatement.addSQLToken(new OrderByToken(selectStatement.getGroupByLastPosition()));
        }
    }
}
