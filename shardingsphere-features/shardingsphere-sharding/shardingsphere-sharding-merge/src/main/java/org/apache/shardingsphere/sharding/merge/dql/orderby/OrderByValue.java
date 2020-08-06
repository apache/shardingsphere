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

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Order by value.
 */
public final class OrderByValue implements Comparable<OrderByValue> {
    
    @Getter
    private final QueryResult queryResult;
    
    private final Collection<OrderByItem> orderByItems;
    
    private final List<Boolean> orderValuesCaseSensitive;
    
    private List<Comparable<?>> orderValues;
    
    public OrderByValue(final QueryResult queryResult, final Collection<OrderByItem> orderByItems, 
                        final SelectStatementContext selectStatementContext, final SchemaMetaData schemaMetaData) throws SQLException {
        this.queryResult = queryResult;
        this.orderByItems = orderByItems;
        orderValuesCaseSensitive = getOrderValuesCaseSensitive(selectStatementContext, schemaMetaData);
    }
    
    private List<Boolean> getOrderValuesCaseSensitive(final SelectStatementContext selectStatementContext, final SchemaMetaData schemaMetaData) throws SQLException {
        List<Boolean> result = new ArrayList<>(orderByItems.size());
        for (OrderByItem eachOrderByItem : orderByItems) {
            result.add(getOrderValuesCaseSensitiveFromTables(selectStatementContext, schemaMetaData, eachOrderByItem));
        }
        return result;
    }
    
    private boolean getOrderValuesCaseSensitiveFromTables(final SelectStatementContext selectStatementContext, 
                                                          final SchemaMetaData schemaMetaData, final OrderByItem eachOrderByItem) throws SQLException {
        for (SimpleTableSegment eachSimpleTableSegment : selectStatementContext.getAllTables()) {
            String tableName = eachSimpleTableSegment.getTableName().getIdentifier().getValue();
            TableMetaData tableMetaData = schemaMetaData.get(tableName);
            Map<String, ColumnMetaData> columns = tableMetaData.getColumns();
            OrderByItemSegment orderByItemSegment = eachOrderByItem.getSegment();
            if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
                String columnName = ((ColumnOrderByItemSegment) orderByItemSegment).getColumn().getIdentifier().getValue();
                if (columns.containsKey(columnName)) {
                    return columns.get(columnName).isCaseSensitive();
                }
            } else if (orderByItemSegment instanceof IndexOrderByItemSegment) {
                int columnIndex = ((IndexOrderByItemSegment) orderByItemSegment).getColumnIndex();
                String columnName = queryResult.getColumnName(columnIndex);
                if (columns.containsKey(columnName)) {
                    return columns.get(columnName).isCaseSensitive();
                }
            } else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * iterate next data.
     *
     * @return has next data
     * @throws SQLException SQL Exception
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
            Preconditions.checkState(null == value || value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    @Override
    public int compareTo(final OrderByValue o) {
        int i = 0;
        for (OrderByItem each : orderByItems) {
            int result = CompareUtil.compareTo(orderValues.get(i), o.orderValues.get(i), each.getSegment().getOrderDirection(),
                each.getSegment().getNullOrderDirection(), orderValuesCaseSensitive.get(i));
            if (0 != result) {
                return result;
            }
            i++;
        }
        return 0;
    }
}
