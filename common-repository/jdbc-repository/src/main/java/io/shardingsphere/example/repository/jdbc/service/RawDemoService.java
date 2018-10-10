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

package io.shardingsphere.example.repository.jdbc.service;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.jdbc.repository.RawOrderItemRepository;
import io.shardingsphere.example.repository.jdbc.repository.RawOrderRepository;

import java.util.ArrayList;
import java.util.List;

public class RawDemoService {
    
    private final RawOrderRepository rawOrderRepository;
    
    private final RawOrderItemRepository rawOrderItemRepository;
    
    public RawDemoService(final RawOrderRepository rawOrderRepository, final RawOrderItemRepository rawOrderItemRepository) {
        this.rawOrderRepository = rawOrderRepository;
        this.rawOrderItemRepository = rawOrderItemRepository;
    }
    
    public void demo() {
        rawOrderRepository.createTableIfNotExists();
        rawOrderItemRepository.createTableIfNotExists();
        rawOrderRepository.truncateTable();
        rawOrderItemRepository.truncateTable();
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            rawOrderRepository.insert(order);
            long orderId = order.getOrderId();
            orderIds.add(orderId);
            
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            rawOrderItemRepository.insert(item);
        }
        System.out.println("Order Data--------------");
        System.out.println(rawOrderRepository.selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(rawOrderItemRepository.selectAll());
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            rawOrderRepository.delete(each);
            rawOrderItemRepository.delete(each);
        }
        System.out.println("Order Data--------------");
        System.out.println(rawOrderRepository.selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(rawOrderItemRepository.selectAll());
        rawOrderItemRepository.dropTable();
        rawOrderRepository.dropTable();
    }
}
