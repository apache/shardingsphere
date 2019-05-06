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

package io.shardingsphere.core.merger.dql.orderby;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order by value.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OrderByValue implements Comparable<OrderByValue> {
    
    @Getter
    private final QueryResult queryResult;
    
    private final List<OrderItem> orderByItems;
    
    private List<Comparable<?>> orderValues;
    
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
        for (OrderItem each : orderByItems) {
            Object value = queryResult.getValue(each.getIndex(), Object.class);
            Preconditions.checkState(null == value || value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    @Override
    public int compareTo(final OrderByValue o) {
        for (int i = 0; i < orderByItems.size(); i++) {
            OrderItem thisOrderBy = orderByItems.get(i);
            int result = CompareUtil.compareTo(orderValues.get(i), o.orderValues.get(i), thisOrderBy.getOrderDirection(), thisOrderBy.getNullOrderDirection());
            if (0 != result) {
                return result;
            }
        }
        return 0;
    }
}
