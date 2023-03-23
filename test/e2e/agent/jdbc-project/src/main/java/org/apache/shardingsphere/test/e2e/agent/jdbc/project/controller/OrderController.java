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

import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.enums.StatementType;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.OrderService;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.vo.response.HttpResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * Order controller.
 */
@RestController
@RequestMapping("/order")
public class OrderController extends AbstractRestController {
    
    @Resource
    private OrderService orderService;
    
    /**
     * Create table.
     *
     * @return http result
     */
    @GetMapping("/createTable")
    public HttpResult<Void> createTable() {
        orderService.createTable();
        return success();
    }
    
    /**
     * Drop table.
     *
     * @return http result
     */
    @GetMapping("/dropTable")
    public HttpResult<Void> dropTable() {
        orderService.dropTable();
        return success();
    }
    
    /**
     * Insert order.
     *
     * @return http result
     */
    @GetMapping("/insert")
    public HttpResult<Void> insert() {
        long index = 0;
        while (index++ < 100) {
            OrderEntity order = new OrderEntity(index, index, "OK");
            orderService.insert(order, 0 == (index & 1) ? StatementType.STATEMENT : StatementType.PREPARED, 0 == index % 5);
        }
        index = 0;
        while (index++ < 10) {
            OrderEntity order = new OrderEntity(index, index, "Fail");
            orderService.insert(order, 0 == (index & 1) ? StatementType.STATEMENT : StatementType.PREPARED, false);
        }
        return success();
    }
    
    /**
     * Update.
     *
     * @return http result
     */
    @GetMapping("/update")
    public HttpResult<Void> update() {
        Collection<OrderEntity> orders = orderService.selectAll(StatementType.STATEMENT);
        int index = 0;
        for (OrderEntity each : orders) {
            each.setStatus("Fail");
            orderService.update(each, 0 == (index++ & 1) ? StatementType.STATEMENT : StatementType.PREPARED);
        }
        return success();
    }
    
    /**
     * Delete order.
     *
     * @return http result
     */
    @GetMapping("/delete")
    public HttpResult<Void> delete() {
        Collection<OrderEntity> orders = orderService.selectAll(StatementType.STATEMENT);
        int index = 0;
        for (OrderEntity each : orders) {
            orderService.delete(each.getOrderId(), 0 == (index++ & 1) ? StatementType.STATEMENT : StatementType.PREPARED);
        }
        return success();
    }
    
    /**
     * Select all order.
     *
     * @return http result
     */
    @GetMapping("selectAll")
    public HttpResult<Collection<OrderEntity>> selectAll() {
        return success(orderService.selectAll(StatementType.PREPARED));
    }
}
