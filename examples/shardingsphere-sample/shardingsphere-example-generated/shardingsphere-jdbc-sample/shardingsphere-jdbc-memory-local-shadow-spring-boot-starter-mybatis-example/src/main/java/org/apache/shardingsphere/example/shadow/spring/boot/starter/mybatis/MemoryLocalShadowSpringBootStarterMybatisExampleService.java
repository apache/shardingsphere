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

package org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis;

import org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis.entity.Address;
import org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis.entity.Order;
import org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis.entity.OrderItem;
import org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis.repository.AddressRepository;
import org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis.repository.OrderItemRepository;
import org.apache.shardingsphere.example.shadow.spring.boot.starter.mybatis.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public final class MemoryLocalShadowSpringBootStarterMybatisExampleService {
    
    private final OrderRepository orderRepository;
    
    private final OrderItemRepository orderItemRepository;
    
    private final AddressRepository addressRepository;
    
    public MemoryLocalShadowSpringBootStarterMybatisExampleService(final OrderRepository orderRepository, final OrderItemRepository orderItemRepository, final AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
    }
    
    /**
     * Execute test.
     */
    public void run() {
        try {
            this.initEnvironment();
            this.processSuccess();
        } finally {
            this.cleanEnvironment();
        }
    }
    
    /**
     * Initialize the database test environment.
     */
    private void initEnvironment() {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        addressRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        addressRepository.truncateTable();
        orderRepository.createTableIfNotExistsShadow();
        orderRepository.truncateTableShadow();
    }
    
    private void processSuccess() {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    private List<Long> insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setUserId(i);
            orderItem.setPhone("13800000001");
            orderItem.setStatus("INSERT_TEST");
            orderItemRepository.insert(orderItem);
            
            Address address = new Address();
            address.setAddressId((long) i);
            address.setAddressName("address_test_" + i);
            addressRepository.insert(address);
            
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        long count = 1;
        for (Long each : orderIds) {
            orderRepository.deleteShadow(each);
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }
    
    private void printData() {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : this.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            System.out.println(each);
        } 
        System.out.println("---------------------------- Print Address Data -------------------");
        for (Object each : addressRepository.selectAll()) {
            System.out.println(each);
        }
    }
    
    private List<Order> selectAll() {
        List<Order> result = orderRepository.selectAll();
        result.addAll(orderRepository.selectShadowOrder());
        return result;
    }
    
    /**
     * Restore the environment.
     */
    private void cleanEnvironment() {
        orderRepository.dropTableShadow();
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
