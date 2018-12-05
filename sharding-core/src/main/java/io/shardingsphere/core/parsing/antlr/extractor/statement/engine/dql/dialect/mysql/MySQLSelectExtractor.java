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

package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dql.dialect.mysql;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.FromWhereExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.GroupByExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.IndexNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.LimitExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.OrderByExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.SelectClauseExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.TableNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.AbstractSQLSegmentsExtractor;
import io.shardingsphere.core.parsing.parser.constant.DerivedColumn;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.DistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.ItemsToken;
import io.shardingsphere.core.parsing.parser.token.OrderByToken;

import java.util.List;

/**
 * Select extractor for MySQL.
 *
 * @author duhongjun
 */
public final class MySQLSelectExtractor extends AbstractSQLSegmentsExtractor {
    
    public MySQLSelectExtractor() {
        addSQLSegmentExtractor(new TableNamesExtractor());
        addSQLSegmentExtractor(new IndexNamesExtractor());
        addSQLSegmentExtractor(new SelectClauseExtractor());
        addSQLSegmentExtractor(new FromWhereExtractor());
        addSQLSegmentExtractor(new GroupByExtractor());
        addSQLSegmentExtractor(new OrderByExtractor());
        addSQLSegmentExtractor(new LimitExtractor());
    }
    
    @Override
    public void postExtract(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        appendDerivedColumns((SelectStatement) sqlStatement, shardingTableMetaData);
        appendDerivedOrderBy((SelectStatement) sqlStatement);
    }
    
    private void appendDerivedColumns(final SelectStatement selectStatement, final ShardingTableMetaData shardingTableMetaData) {
        ItemsToken itemsToken = new ItemsToken(selectStatement.getSelectListLastPosition());
        appendAvgDerivedColumns(itemsToken, selectStatement);
        if (!selectStatement.getOrderByItems().isEmpty()) {
            appendDerivedOrderColumns(itemsToken, selectStatement.getOrderByItems(), selectStatement, shardingTableMetaData);
        }
        if (!selectStatement.getGroupByItems().isEmpty()) {
            appendDerivedGroupColumns(itemsToken, selectStatement.getGroupByItems(), selectStatement, shardingTableMetaData);
        }
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
    
    private void appendDerivedOrderColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final SelectStatement selectStatement, final ShardingTableMetaData shardingTableMetaData) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!containsItem(selectStatement, each, shardingTableMetaData)) {
                String alias = DerivedColumn.ORDER_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }
    
    private void appendDerivedGroupColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final SelectStatement selectStatement, final ShardingTableMetaData shardingTableMetaData) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!containsItem(selectStatement, each, shardingTableMetaData)) {
                String alias = DerivedColumn.GROUP_BY_ALIAS.getDerivedColumnAlias(derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }
    
    private boolean containsItem(final SelectStatement selectStatement, final OrderItem orderItem, final ShardingTableMetaData shardingTableMetaData) {
        return orderItem.isIndex() || containsItemInStarSelectItems(selectStatement, orderItem, shardingTableMetaData) || containsItemInSelectItems(selectStatement, orderItem);
    }
    
    private boolean containsItemInStarSelectItems(final SelectStatement selectStatement, final OrderItem orderItem, final ShardingTableMetaData shardingTableMetaData) {
        return selectStatement.hasUnqualifiedStarSelectItem()
                || containsItemWithOwnerInStarSelectItems(selectStatement, orderItem) || containsItemWithoutOwnerInStarSelectItems(selectStatement, orderItem, shardingTableMetaData);
    }
    
    private boolean containsItemWithOwnerInStarSelectItems(final SelectStatement selectStatement, final OrderItem orderItem) {
        return orderItem.getOwner().isPresent() && selectStatement.findStarSelectItem(orderItem.getOwner().get()).isPresent();
    }
    
    private boolean containsItemWithoutOwnerInStarSelectItems(final SelectStatement selectStatement, final OrderItem orderItem, final ShardingTableMetaData shardingTableMetaData) {
        if (!orderItem.getOwner().isPresent()) {
            for (StarSelectItem each : selectStatement.getQualifiedStarSelectItems()) {
                if (isSameSelectItem(selectStatement, each, orderItem, shardingTableMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isSameSelectItem(final SelectStatement selectStatement, final StarSelectItem starSelectItem, final OrderItem orderItem, final ShardingTableMetaData shardingTableMetaData) {
        Preconditions.checkState(starSelectItem.getOwner().isPresent());
        Preconditions.checkState(orderItem.getName().isPresent());
        Optional<Table> table = selectStatement.getTables().find(starSelectItem.getOwner().get());
        return table.isPresent() && shardingTableMetaData.containsColumn(table.get().getName(), orderItem.getName().get());
    }
    
    private boolean containsItemInSelectItems(final SelectStatement selectStatement, final OrderItem orderItem) {
        for (SelectItem each : selectStatement.getItems()) {
            if (containsItemInDistinctItems(orderItem, each) || isSameAlias(each, orderItem) || isSameQualifiedName(each, orderItem)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsItemInDistinctItems(final OrderItem orderItem, final SelectItem selectItem) {
        if (!(selectItem instanceof DistinctSelectItem)) {
            return false;
        }
        DistinctSelectItem distinctSelectItem = (DistinctSelectItem) selectItem;
        return distinctSelectItem.getDistinctColumnNames().contains(orderItem.getColumnLabel()) || orderItem.getColumnLabel().equals(distinctSelectItem.getColumnLabel());
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
