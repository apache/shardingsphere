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

package io.shardingsphere.example.repository.mybatis.service;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.mybatis.repository.MybatisOrderItemRepository;
import io.shardingsphere.example.repository.mybatis.repository.MybatisOrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemoService {
    
    @Resource
    private MybatisOrderRepository orderRepository;
    
    @Resource
    private MybatisOrderItemRepository orderItemRepository;
    
    public void demo() {
        initTables();
        List<Long> orderIds = insertData();
        printData();
        deleteData(orderIds);
        printData();
        cleanTables();
    }
    
    private void initTables() {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
    }
    
    private List<Long> insertData() {
        System.out.println("1.Insert--------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds) {
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
    }
    
    private void printData() {
        System.out.println("Order Data--------------");
        System.out.println(orderRepository.selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(orderItemRepository.selectAll());
    }
    
    private void cleanTables() {
        orderItemRepository.dropTable();
        orderRepository.dropTable();
    }
}
