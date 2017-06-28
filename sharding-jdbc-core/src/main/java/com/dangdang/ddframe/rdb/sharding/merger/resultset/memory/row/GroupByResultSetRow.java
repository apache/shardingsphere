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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row;

import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 具有分组功能的数据行对象.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public final class GroupByResultSetRow extends AbstractResultSetRow {
    
    private final ResultSet resultSet;
    
    private final List<OrderItem> groupByList;
    
    private final Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap;
    
    public GroupByResultSetRow(final ResultSet resultSet, final List<OrderItem> groupByList, final List<AggregationSelectItem> aggregationColumns) throws SQLException {
        super(resultSet);
        this.resultSet = resultSet;
        this.groupByList = groupByList;
        aggregationUnitMap = Maps.toMap(aggregationColumns, new Function<AggregationSelectItem, AggregationUnit>() {
            
            @Override
            public AggregationUnit apply(final AggregationSelectItem input) {
                return AggregationUnitFactory.create(input.getType());
            }
        });
    }
    
    /**
     * 处理聚合函数结果集.
     * 
     * @throws SQLException SQL异常
     */
    public void aggregate() throws SQLException {
        for (Map.Entry<AggregationSelectItem, AggregationUnit> each : aggregationUnitMap.entrySet()) {
            each.getValue().merge(getAggregationValues(
                    each.getKey().getDerivedAggregationSelectItems().isEmpty() ? Collections.singletonList(each.getKey()) : each.getKey().getDerivedAggregationSelectItems()));
        }
    }
    
    private List<Comparable<?>> getAggregationValues(final List<AggregationSelectItem> aggregationSelectItems) throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(aggregationSelectItems.size());
        for (AggregationSelectItem each : aggregationSelectItems) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(null == value || value instanceof Comparable, "Aggregation value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    /**
     * 生成结果.
     */
    public void generateResult() {
        for (AggregationSelectItem each : aggregationUnitMap.keySet()) {
            setCell(each.getIndex(), aggregationUnitMap.get(each).getResult());
        }
    }
    
    /**
     * 获取分组值.
     * 
     * @return 分组值集合
     * @throws SQLException SQL异常
     */
    public List<Object> getGroupValues() throws SQLException {
        List<Object> result = new ArrayList<>(groupByList.size());
        for (OrderItem each : groupByList) {
            result.add(resultSet.getObject(each.getIndex()));
        }
        return result;
    }
}
