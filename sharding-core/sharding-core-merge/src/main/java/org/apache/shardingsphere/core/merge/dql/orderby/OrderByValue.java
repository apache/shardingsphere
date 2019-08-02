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

package org.apache.shardingsphere.core.merge.dql.orderby;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Order by value.
 * 
 * @author zhangliang
 * @author yangyi
 */
public final class OrderByValue implements Comparable<OrderByValue> {
    
    @Getter
    private final QueryResult queryResult;
    
    private final Collection<OrderByItem> orderByItems;
    
    private final List<Boolean> orderValuesCaseSensitive;
    
    private List<Comparable<?>> orderValues;
    
    public OrderByValue(final QueryResult queryResult, final Collection<OrderByItem> orderByItems) {
        this.queryResult = queryResult;
        this.orderByItems = orderByItems;
        this.orderValuesCaseSensitive = getOrderValuesCaseSensitive();
    }
    
    @SneakyThrows
    private List<Boolean> getOrderValuesCaseSensitive() {
        List<Boolean> result = new ArrayList<>(orderByItems.size());
        for (OrderByItem each : orderByItems) {
            result.add(queryResult.isCaseSensitive(each.getIndex()));
        }
        return result;
    }
    
    /**
     * iterate next data.
     *
     * @return has next data
     * @throws SQLException SQL Exception
     */
    public boolean next() throws SQLException {
        boolean result = queryResult.next();
        orderValues = result ? getOrderValues() : Collections.<Comparable<?>>emptyList();
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
