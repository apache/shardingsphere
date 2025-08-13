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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskTest {
    
    private DataSource logicDataSource;
    
    private OrderRepository orderRepository;
    
    private OrderItemRepository orderItemRepository;
    
    private AddressRepository addressRepository;
    
    @AfterEach
    void afterEach() throws SQLException {
        ResourceUtils.closeJdbcDataSource(logicDataSource);
    }
    
    @Test
    void assertMaskInLocalTransactions() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/features/mask.yaml");
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
        final Collection<Long> orderIds = insertData();
        assertThat(orderRepository.selectAll(), is(IntStream.range(1, 11).mapToObj(each -> new Order(each, each % 2, each, each, "INSERT_TEST")).collect(Collectors.toList())));
        assertThat(orderItemRepository.selectAll(), is(IntStream.range(1, 11).mapToObj(each -> new OrderItem(each, each, each, "138****0001", "INSERT_TEST")).collect(Collectors.toList())));
        assertThat(addressRepository.selectAll(), is(LongStream.range(1L, 11L).mapToObj(each -> new Address(each, "address_test_" + each)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertTrue(orderRepository.selectAll().isEmpty());
        assertTrue(orderItemRepository.selectAll().isEmpty());
        assertTrue(addressRepository.selectAll().isEmpty());
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
    
    private void cleanEnvironment() throws SQLException {
        orderRepository.dropTableInMySQL();
        orderItemRepository.dropTableInMySQL();
        addressRepository.dropTableInMySQL();
    }
}
