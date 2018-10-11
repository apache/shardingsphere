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

package io.shardingsphere.example.transaction.fixture.repository;

import io.shardingsphere.example.transaction.fixture.entity.Order;
import io.shardingsphere.example.transaction.fixture.entity.OrderItem;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Component
public class TransactionalDao {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;
    
    public void createTable() {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
    }
    
    public void truncateTable() {
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
    }
    
    public List<Long> insertData() {
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            long orderId = order.getOrderId();
            orderIds.add(orderId);
            
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setUserId(51);
            orderItemRepository.insert(item);
        }
        System.out.println(orderItemRepository.selectAll());
        return orderIds;
    }
    
    public List<Long> insertFailed() {
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("2.Insert failed--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            long orderId = order.getOrderId();
            orderIds.add(orderId);
            
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setUserId(51);
            orderItemRepository.insert(item);
            
        }
        System.out.println(orderItemRepository.selectAll());
        return orderIds;
    }
    
    public void deleteData(final List<Long> orderIds) {
        System.out.println("3.Delete--------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
        System.out.println(orderItemRepository.selectAll());
    }
    
    public void dropTable() {
        orderItemRepository.dropTable();
        orderRepository.dropTable();
    }
}
