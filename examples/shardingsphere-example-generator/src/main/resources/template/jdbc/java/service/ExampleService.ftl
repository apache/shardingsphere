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
<#assign package = feature?replace('-', '')?replace(',', '.') />

package org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.service;

import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.Address;
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.Order;
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.OrderItem;
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository.AddressRepository;
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository.OrderItemRepository;
import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
<#if framework?contains("spring")>

import org.springframework.stereotype.Service;
</#if>
<#if framework?contains("jdbc")>

import javax.sql.DataSource;
</#if>
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

<#if framework?contains("spring")>
@Service
</#if>
public final class ExampleService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleService.class);
    
    private final OrderRepository orderRepository;
    
    private final OrderItemRepository orderItemRepository;
    
    private final AddressRepository addressRepository;
<#if framework?contains("jdbc")>
    
    public ExampleService(final DataSource dataSource) {
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
        addressRepository = new AddressRepository(dataSource);
    }
<#else>
    
    public ExampleService(final OrderRepository orderRepository, final OrderItemRepository orderItemRepository, final AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
    }
</#if>
    
    public void run() throws SQLException {
        try {
            this.initEnvironment();
            this.processSuccess();
        } finally {
            this.cleanEnvironment();
        }
    }
    
    private void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        addressRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        addressRepository.truncateTable();
    <#if feature?contains("shadow")>
        orderRepository.createTableIfNotExistsShadow();
        orderRepository.truncateTableShadow();
    </#if>
    }
    
    private void processSuccess() throws SQLException {
        LOGGER.info("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        LOGGER.info("-------------- Process Success Finish --------------");
    }
    
    private List<Long> insertData() throws SQLException {
        LOGGER.info("---------------------------- Insert Data ----------------------------");
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
    
    private void deleteData(final List<Long> orderIds) throws SQLException {
        LOGGER.info("---------------------------- Delete Data ----------------------------");
        long count = 1;
        for (Long each : orderIds) {
        <#if feature?contains("shadow")>
            orderRepository.deleteShadow(each);
        </#if>        
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }
    
    private void printData() throws SQLException {
        LOGGER.info("---------------------------- Print Order Data -----------------------");
        for (Object each : this.selectAll()) {
            LOGGER.info(each.toString());
        }
        LOGGER.info("---------------------------- Print OrderItem Data -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            LOGGER.info(each.toString());
        } 
        LOGGER.info("---------------------------- Print Address Data -------------------");
        for (Object each : addressRepository.selectAll()) {
            LOGGER.info(each.toString());
        }
    }
    
    private List<Order> selectAll() throws SQLException {
        List<Order> result = orderRepository.selectAll();
    <#if feature?contains("shadow")>
        result.addAll(orderRepository.selectShadowOrder());
    </#if>
        return result;
    }
    
    private void cleanEnvironment() throws SQLException {
    <#if feature?contains("shadow")>
        orderRepository.dropTableShadow();
    </#if>
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
