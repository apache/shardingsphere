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

package org.apache.shardingsphere.test.natived.jdbc.features;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.natived.commons.entity.Address;
import org.apache.shardingsphere.test.natived.commons.entity.Order;
import org.apache.shardingsphere.test.natived.commons.entity.OrderItem;
import org.apache.shardingsphere.test.natived.commons.repository.AddressRepository;
import org.apache.shardingsphere.test.natived.commons.repository.OrderItemRepository;
import org.apache.shardingsphere.test.natived.commons.repository.OrderRepository;
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadWriteSplittingTest {
    
    private DataSource logicDataSource;
    
    private OrderRepository orderRepository;
    
    private OrderItemRepository orderItemRepository;
    
    private AddressRepository addressRepository;
    
    @AfterEach
    void afterEach() throws SQLException {
        ResourceUtils.closeJdbcDataSource(logicDataSource);
    }
    
    @Test
    void assertReadWriteSplittingInLocalTransactions() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/features/readwrite-splitting.yaml");
        logicDataSource = new HikariDataSource(config);
        orderRepository = new OrderRepository(logicDataSource);
        orderItemRepository = new OrderItemRepository(logicDataSource);
        addressRepository = new AddressRepository(logicDataSource);
        initEnvironment();
        processSuccess();
        cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExistsInMySQL();
        orderItemRepository.createTableIfNotExistsInMySQL();
        addressRepository.createTableIfNotExistsInMySQL();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        addressRepository.truncateTable();
    }
    
    private void processSuccess() throws SQLException {
        Collection<Long> orderIds = insertData();
        assertThrows(JdbcSQLSyntaxErrorException.class, this::printData,
                "This is intentional because the read operation is in the slave database and the corresponding table does not exist.");
        deleteData(orderIds);
        assertThrows(JdbcSQLSyntaxErrorException.class, this::printData,
                "This is intentional because the read operation is in the slave database and the corresponding table does not exist.");
    }
    
    private Collection<Long> insertData() throws SQLException {
        Collection<Long> result = new ArrayList<>(10);
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
            Address address = new Address((long) i, "address_test_" + i);
            addressRepository.insert(address);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final Collection<Long> orderIds) throws SQLException {
        long count = 1;
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }
    
    private void printData() throws SQLException {
        orderRepository.selectAll();
        orderItemRepository.selectAll();
        addressRepository.selectAll();
    }
    
    private void cleanEnvironment() throws SQLException {
        orderRepository.dropTableInMySQL();
        orderItemRepository.dropTableInMySQL();
        addressRepository.dropTableInMySQL();
    }
}
