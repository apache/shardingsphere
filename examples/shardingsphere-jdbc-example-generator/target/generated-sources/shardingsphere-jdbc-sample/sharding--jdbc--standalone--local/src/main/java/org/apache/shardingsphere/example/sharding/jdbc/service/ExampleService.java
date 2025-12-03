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

package org.apache.shardingsphere.example.sharding.jdbc.service;

import org.apache.shardingsphere.example.sharding.jdbc.entity.Address;
import org.apache.shardingsphere.example.sharding.jdbc.entity.Order;
import org.apache.shardingsphere.example.sharding.jdbc.entity.OrderItem;
import org.apache.shardingsphere.example.sharding.jdbc.repository.AddressRepository;
import org.apache.shardingsphere.example.sharding.jdbc.repository.OrderItemRepository;
import org.apache.shardingsphere.example.sharding.jdbc.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ExampleService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleService.class);
    
    private final OrderRepository orderRepository;
    
    private final OrderItemRepository orderItemRepository;
    
    private final AddressRepository addressRepository;

    /**
     * 构造函数， 创建三个仓库的实例对象。
     * @param dataSource
     */
    public ExampleService(final DataSource dataSource) {
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
        addressRepository = new AddressRepository(dataSource);
    }

    /**
     * 运行测试程序。
     * @throws SQLException
     */
    public void run() throws SQLException {
        try {
            //环境初始化。
            this.initEnvironment();
            //执行测试。
            this.processSuccess();
        } finally {
            //this.cleanEnvironment();
        }
    }

    /**
     * 环境初始化：创建表，清空表。
     * @throws SQLException
     */
    private void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        addressRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        addressRepository.truncateTable();
    }

    /**
     * 执行测试。
     * @throws SQLException
     */
    private void processSuccess() throws SQLException {
        LOGGER.info("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        LOGGER.info("-------------- Process Success Finish --------------");
    }

    /**
     * 插入数据。
     * @return
     * @throws SQLException
     */
    private List<Long> insertData() throws SQLException {
        LOGGER.info("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            //创建并初始化一个Order对象。
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            //插入创建好的order对象。
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

    /**
     * 删除数据。
     * @param orderIds
     * @throws SQLException
     */
    private void deleteData(final List<Long> orderIds) throws SQLException {
        LOGGER.info("---------------------------- Delete Data ----------------------------");
        long count = 1;
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }

    /**
     * 打印数据。
     * @throws SQLException
     */
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

    /**
     * 查询全部信息。
     * @return
     * @throws SQLException
     */
    private List<Order> selectAll() throws SQLException {
        List<Order> result = orderRepository.selectAll();
        return result;
    }

    /**
     * 清空环境。
     * @throws SQLException
     */
    private void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
