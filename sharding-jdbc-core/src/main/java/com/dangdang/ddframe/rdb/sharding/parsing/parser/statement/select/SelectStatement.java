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

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.AbstractSQLStatement;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Select SQL语句对象.
 *
 * @author zhangliang
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class SelectStatement extends AbstractSQLStatement {
    
    private boolean distinct;
    
    private boolean containStar;
    
    private int selectListLastPosition;
    
    private int groupByLastPosition;
    
    private final List<SelectItem> items = new LinkedList<>();
    
    private final List<OrderItem> groupByItems = new LinkedList<>();
    
    private final List<OrderItem> orderByItems = new LinkedList<>();
    
    private Limit limit;
    
    public SelectStatement() {
        super(SQLType.SELECT);
    }
    
    /**
     * 获取聚合选择项集合.
     *
     * @return 聚合选择项
     */
    public List<AggregationSelectItem> getAggregationSelectItems() {
        List<AggregationSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem) {
                AggregationSelectItem aggregationSelectItem = (AggregationSelectItem) each;
                result.add(aggregationSelectItem);
                for (AggregationSelectItem derivedEach: aggregationSelectItem.getDerivedAggregationSelectItems()) {
                    result.add(derivedEach);
                }
            }
        }
        return result;
    }
    
    /**
     * 判断是否分组和排序项一致.
     *
     * @return 是否分组和排序项一致
     */
    public boolean isSameGroupByAndOrderByItems() {
        return !getGroupByItems().isEmpty() && getGroupByItems().equals(getOrderByItems());
    }
    
    /**
     * 为选择项设置索引.
     * 
     * @param columnLabelIndexMap 列标签索引字典
     */
    public void setIndexForItems(final Map<String, Integer> columnLabelIndexMap) {
        setIndexForAggregationItem(columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, orderByItems);
        setIndexForOrderItem(columnLabelIndexMap, groupByItems);
    }
    
    private void setIndexForAggregationItem(final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationSelectItem each : getAggregationSelectItems()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s, please add alias for aggregate selections", each));
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), String.format("Can't find index: %s", derived));
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }
    
    private void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final List<OrderItem> orderItems) {
        for (OrderItem each : orderItems) {
            if (-1 != each.getIndex()) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s", each));
            if (columnLabelIndexMap.containsKey(each.getColumnLabel())) {
                each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            }
        }
    }
}
