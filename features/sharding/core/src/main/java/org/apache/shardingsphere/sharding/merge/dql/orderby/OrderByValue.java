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

package org.apache.shardingsphere.sharding.merge.dql.orderby;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.exception.data.NotImplementComparableValueException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Order by value.
 */
public final class OrderByValue implements Comparable<OrderByValue> {
    
    @Getter
    private final QueryResult queryResult;
    
    private final Collection<OrderByItem> orderByItems;
    
    private final List<Boolean> orderValuesCaseSensitive;
    
    private final SelectStatementContext selectStatementContext;
    
    private List<Comparable<?>> orderValues;
    
    public OrderByValue(final QueryResult queryResult, final Collection<OrderByItem> orderByItems,
                        final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        this.queryResult = queryResult;
        this.orderByItems = orderByItems;
        this.selectStatementContext = selectStatementContext;
        orderValuesCaseSensitive = getOrderValuesCaseSensitive(schema);
    }
    
    private List<Boolean> getOrderValuesCaseSensitive(final ShardingSphereSchema schema) throws SQLException {
        List<Boolean> result = new ArrayList<>(orderByItems.size());
        for (OrderByItem eachOrderByItem : orderByItems) {
            result.add(getOrderValuesCaseSensitiveFromTables(schema, eachOrderByItem));
        }
        return result;
    }
    
    private boolean getOrderValuesCaseSensitiveFromTables(final ShardingSphereSchema schema, final OrderByItem eachOrderByItem) throws SQLException {
        for (SimpleTableSegment each : selectStatementContext.getTablesContext().getSimpleTables()) {
            String tableName = each.getTableName().getIdentifier().getValue();
            ShardingSphereTable table = schema.getTable(tableName);
            OrderByItemSegment orderByItemSegment = eachOrderByItem.getSegment();
            if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
                String columnName = ((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getIdentifier().getValue();
                if (table.containsColumn(columnName)) {
                    return table.getColumn(columnName).isCaseSensitive();
                }
            } else if (orderByItemSegment instanceof IndexOrderByItemSegment) {
                int columnIndex = ((IndexOrderByItemSegment) orderByItemSegment).getColumnIndex();
                String columnName = queryResult.getMetaData().getColumnName(columnIndex);
                if (table.containsColumn(columnName)) {
                    return table.getColumn(columnName).isCaseSensitive();
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Iterate next data.
     *
     * @return has next data
     * @throws SQLException SQL exception
     */
    public boolean next() throws SQLException {
        boolean result = queryResult.next();
        orderValues = result ? getOrderValues() : Collections.emptyList();
        return result;
    }
    
    private List<Comparable<?>> getOrderValues() throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(orderByItems.size());
        for (OrderByItem each : orderByItems) {
            Object value = queryResult.getValue(each.getIndex(), Object.class);
            ShardingSpherePreconditions.checkState(null == value || value instanceof Comparable, () -> new NotImplementComparableValueException("Order by", value));
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    @Override
    public int compareTo(final OrderByValue orderByValue) {
        int i = 0;
        for (OrderByItem each : orderByItems) {
            int result = CompareUtils.compareTo(orderValues.get(i), orderByValue.orderValues.get(i), each.getSegment().getOrderDirection(),
                    each.getSegment().getNullsOrderType(selectStatementContext.getSqlStatement().getDatabaseType()), orderValuesCaseSensitive.get(i));
            if (0 != result) {
                return result;
            }
            i++;
        }
        return 0;
    }
}
