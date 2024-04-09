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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project.controller;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.enums.StatementType;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.OrderService;

import java.util.Collection;

/**
 * Order controller.
 */
@RequiredArgsConstructor
public final class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Create table.
     */
    public void createTable() {
        orderService.createTable();
    }
    
    /**
     * Drop table.
     */
    public void dropTable() {
        orderService.dropTable();
    }
    
    /**
     * Insert order.
     */
    public void insert() {
        long index = 0;
        while (index++ <= 100) {
            OrderEntity order = new OrderEntity(index, index, "OK");
            orderService.insert(order, 0 == (index & 1) ? StatementType.STATEMENT : StatementType.PREPARED, 0 == index % 5);
        }
    }
    
    /**
     * Create error request.
     */
    public void createErrorRequest() {
        long index = 0;
        while (index++ <= 10) {
            OrderEntity order = new OrderEntity(index, index, "Fail");
            orderService.insert(order, 0 == (index & 1) ? StatementType.STATEMENT : StatementType.PREPARED, false);
        }
    }
    
    /**
     * Update.
     */
    public void update() {
        Collection<OrderEntity> orders = orderService.selectAll(StatementType.STATEMENT);
        int index = 0;
        for (OrderEntity each : orders) {
            each.setStatus("Fail");
            orderService.update(each, 0 == (index++ & 1) ? StatementType.STATEMENT : StatementType.PREPARED);
        }
    }
    
    /**
     * Delete order.
     */
    public void delete() {
        Collection<OrderEntity> orders = orderService.selectAll(StatementType.STATEMENT);
        int index = 0;
        for (OrderEntity each : orders) {
            orderService.delete(each.getOrderId(), 0 == (index++ & 1) ? StatementType.STATEMENT : StatementType.PREPARED);
        }
    }
    
    /**
     * Select all order.
     */
    public void selectAll() {
        orderService.selectAll(StatementType.PREPARED);
    }
}
