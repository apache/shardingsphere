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

package io.shardingsphere.example.repository.api.trace;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.entity.OrderItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Memory repository history.
 *
 * @author zhaojun
 */
public class MemoryLogService {
    
    private final Map<DatabaseAccess, List<Order>> orderMap = new HashMap<>();
    
    private final Map<DatabaseAccess, List<OrderItem>> orderItemMap = new HashMap<>();
    
    public void putOrderData(final DatabaseAccess operation, final Order order) {
        if (!orderMap.containsKey(operation)) {
            orderMap.put(operation, new LinkedList<Order>());
        }
        orderMap.get(operation).add(order);
    }
    
    public List<Order> getOrderData(final DatabaseAccess operation) {
        return orderMap.containsKey(operation) ? orderMap.get(operation) : Collections.<Order>emptyList();
    }
    
    public void putItemData(final DatabaseAccess operation, final OrderItem orderItem) {
        if (!orderItemMap.containsKey(operation)) {
            orderItemMap.put(operation, new LinkedList<OrderItem>());
        }
        orderItemMap.get(operation).add(orderItem);
    }
    
    public List<OrderItem> getOrderItemData(final DatabaseAccess operation) {
        return orderItemMap.containsKey(operation) ? orderItemMap.get(operation) : Collections.<OrderItem>emptyList();
    }
}
