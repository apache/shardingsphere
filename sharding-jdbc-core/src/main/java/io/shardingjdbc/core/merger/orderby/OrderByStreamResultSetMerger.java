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

package io.shardingjdbc.core.merger.orderby;

import io.shardingjdbc.core.merger.ResultSetMergerInput;
import io.shardingjdbc.core.merger.common.AbstractStreamResultSetMerger;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import lombok.AccessLevel;
import lombok.Getter;

import java.sql.SQLException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Stream merger for order by.
 *
 * @author zhangliang
 */
public class OrderByStreamResultSetMerger extends AbstractStreamResultSetMerger {
    
    private final List<OrderItem> orderByItems;
    
    @Getter(AccessLevel.PROTECTED)
    private final Queue<OrderByValue> orderByValuesQueue;
    
    @Getter(AccessLevel.PROTECTED)
    private boolean isFirstNext;
    
    public OrderByStreamResultSetMerger(final List<ResultSetMergerInput> resultSetMergerInputs, final List<OrderItem> orderByItems) throws SQLException {
        this.orderByItems = orderByItems;
        this.orderByValuesQueue = new PriorityQueue<>(resultSetMergerInputs.size());
        orderResultSetsToQueue(resultSetMergerInputs);
        isFirstNext = true;
    }
    
    private void orderResultSetsToQueue(final List<ResultSetMergerInput> resultSetMergerInputs) throws SQLException {
        for (ResultSetMergerInput each : resultSetMergerInputs) {
            OrderByValue orderByValue = new OrderByValue(each, orderByItems);
            if (orderByValue.next()) {
                orderByValuesQueue.offer(orderByValue);
            }
        }
        setCurrentResultSetMergerInput(orderByValuesQueue.isEmpty() ? resultSetMergerInputs.get(0) : orderByValuesQueue.peek().getResultSetMergerInput());
    }
    
    @Override
    public boolean next() throws SQLException {
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        if (isFirstNext) {
            isFirstNext = false;
            return true;
        }
        OrderByValue firstOrderByValue = orderByValuesQueue.poll();
        if (firstOrderByValue.next()) {
            orderByValuesQueue.offer(firstOrderByValue);
        }
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        setCurrentResultSetMergerInput(orderByValuesQueue.peek().getResultSetMergerInput());
        return true;
    }
}
