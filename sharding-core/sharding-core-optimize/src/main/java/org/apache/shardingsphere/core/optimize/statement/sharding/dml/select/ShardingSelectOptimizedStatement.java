/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.ShardingWhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Select optimized statement for sharding.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class ShardingSelectOptimizedStatement extends ShardingWhereOptimizedStatement {
    
    private final SelectStatement selectStatement;
    
    private final Collection<SelectItem> items;
    
    private Pagination pagination;
    
    public ShardingSelectOptimizedStatement(final SQLStatement sqlStatement, final List<ShardingCondition> shardingConditions, final AndCondition encryptConditions) {
        super(sqlStatement, new ShardingConditions(shardingConditions), encryptConditions);
        this.selectStatement = (SelectStatement) sqlStatement;
        items = selectStatement.getItems();
    }
    
    /**
     * Get aggregation select items.
     *
     * @return aggregation select items
     */
    public List<AggregationSelectItem> getAggregationSelectItems() {
        List<AggregationSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem) {
                AggregationSelectItem aggregationSelectItem = (AggregationSelectItem) each;
                result.add(aggregationSelectItem);
                result.addAll(aggregationSelectItem.getDerivedAggregationSelectItems());
            }
        }
        return result;
    }
    
    /**
     * Get distinct select items.
     *
     * @return distinct select items
     */
    public Optional<DistinctSelectItem> getDistinctSelectItem() {
        for (SelectItem each : items) {
            if (each instanceof DistinctSelectItem) {
                return Optional.of((DistinctSelectItem) each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Set index for select items.
     *
     * @param columnLabelIndexMap map for column label and index
     */
    public void setIndexForItems(final Map<String, Integer> columnLabelIndexMap) {
        setIndexForAggregationItem(columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, selectStatement.getOrderByItems());
        setIndexForOrderItem(columnLabelIndexMap, selectStatement.getGroupByItems());
    }
    
    private void setIndexForAggregationItem(final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationSelectItem each : getAggregationSelectItems()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), "Can't find index: %s, please add alias for aggregate selections", each);
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), "Can't find index: %s", derived);
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }
    
    private void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final List<OrderByItemSegment> orderItems) {
        for (OrderByItemSegment each : orderItems) {
            if (each instanceof IndexOrderByItemSegment) {
                continue;
            }
            Optional<String> alias = getAlias(((TextOrderByItemSegment) each).getText());
            String columnLabel = alias.isPresent() ? alias.get() : getOrderItemText((TextOrderByItemSegment) each);
            Preconditions.checkState(columnLabelIndexMap.containsKey(columnLabel), "Can't find index: %s", each);
            if (columnLabelIndexMap.containsKey(columnLabel)) {
                each.setIndex(columnLabelIndexMap.get(columnLabel));
            }
        }
    }
    
    private Optional<String> getAlias(final String name) {
        if (selectStatement.isContainStar()) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItem each : items) {
            if (SQLUtil.getExactlyExpression(rawName).equalsIgnoreCase(SQLUtil.getExactlyExpression(SQLUtil.getExactlyValue(each.getExpression())))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
            }
        }
        return Optional.absent();
    }
    
    private String getOrderItemText(final TextOrderByItemSegment orderByItemSegment) {
        return orderByItemSegment instanceof ColumnOrderByItemSegment
                ? ((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getName() : ((ExpressionOrderByItemSegment) orderByItemSegment).getExpression();
    }
}
