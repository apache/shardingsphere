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

package org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')};

import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity.Order;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity.OrderItem;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.repository.OrderItemRepository;
import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.repository.OrderRepository;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

<#assign frameworkName="">
<#list framework?split("-") as framework1>
    <#assign frameworkName=frameworkName + framework1?cap_first>
</#list>
<#assign featureName="">
<#list feature?split("-") as feature1>
    <#assign featureName=featureName + feature1?cap_first>
</#list>
<#if framework?contains("spring")>
@Service
</#if>
public final class ${mode?cap_first}${transaction?cap_first}${featureName}${frameworkName}ExampleService {
    
    private final OrderRepository orderRepository;
    
    private final OrderItemRepository orderItemRepository;
<#if framework?contains("jdbc")>
    public ${mode?cap_first}${transaction?cap_first}${featureName}${frameworkName}ExampleService(final DataSource dataSource) {
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
    }
<#else>
    public ${mode?cap_first}${transaction?cap_first}${featureName}${frameworkName}ExampleService(final OrderRepository orderRepository,
                                                                                                 final OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }
</#if>
    
    /**
     * Execute test.
     *
     * @throws SQLException
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
     * @throws SQLException
     */
    private void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
    <#if feature=="shadow">
        orderRepository.createTableIfNotExistsShadow();
        orderRepository.truncateTableShadow();
    </#if>
        }
    }
    
    private void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            repository.insertOrder(order);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setUserId(i);
            orderItem.setPhone("13800000001");
            orderItem.setStatus("INSERT_TEST");
            repository.insertOrderItem(orderItem);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
    }
    
    private void printData() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : this.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            System.out.println(each);
        }
    }
    
    private List<Order> selectAll() {
        List<Order> result = orderRepository.selectAll();
    <#if feature=="shadow">
        result.addAll(orderRepository.selectShadowOrder());
    </#if>
        return result;
    }
    
    /**
     * Restore the environment.
     * @throws SQLException
     */
    private void cleanEnvironment() throws SQLException {
    <#if feature=="shadow">
        orderRepository.dropTableShadow();
    </#if>
        orderRepository.dropTable();
        orderItemRepository.dropTable();
    }
}
