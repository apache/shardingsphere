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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowTest {
    
    private DataSource logicDataSource;
    
    private OrderRepository orderRepository;
    
    private OrderItemRepository orderItemRepository;
    
    private AddressRepository addressRepository;
    
    @AfterEach
    void afterEach() throws SQLException {
        ResourceUtils.closeJdbcDataSource(logicDataSource);
    }
    
    @Test
    void assertShadowInLocalTransactions() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/features/shadow.yaml");
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
        orderRepository.createTableIfNotExistsShadow();
        orderRepository.truncateTableShadow();
    }
    
    private void processSuccess() throws SQLException {
        final Collection<Long> orderIds = insertData();
        assertThat(selectAll(), is(Arrays.asList(
                new Order(1L, 0, 2, 2L, "INSERT_TEST"),
                new Order(2L, 0, 4, 4L, "INSERT_TEST"),
                new Order(3L, 0, 6, 6L, "INSERT_TEST"),
                new Order(4L, 0, 8, 8L, "INSERT_TEST"),
                new Order(5L, 0, 10, 10L, "INSERT_TEST"),
                new Order(1L, 1, 1, 1L, "INSERT_TEST"),
                new Order(2L, 1, 3, 3L, "INSERT_TEST"),
                new Order(3L, 1, 5, 5L, "INSERT_TEST"),
                new Order(4L, 1, 7, 7L, "INSERT_TEST"),
                new Order(5L, 1, 9, 9L, "INSERT_TEST"))));
        assertThat(orderItemRepository.selectAll(), is(Arrays.asList(
                new OrderItem(1L, 1L, 1, "13800000001", "INSERT_TEST"),
                new OrderItem(2L, 1L, 2, "13800000001", "INSERT_TEST"),
                new OrderItem(3L, 2L, 3, "13800000001", "INSERT_TEST"),
                new OrderItem(4L, 2L, 4, "13800000001", "INSERT_TEST"),
                new OrderItem(5L, 3L, 5, "13800000001", "INSERT_TEST"),
                new OrderItem(6L, 3L, 6, "13800000001", "INSERT_TEST"),
                new OrderItem(7L, 4L, 7, "13800000001", "INSERT_TEST"),
                new OrderItem(8L, 4L, 8, "13800000001", "INSERT_TEST"),
                new OrderItem(9L, 5L, 9, "13800000001", "INSERT_TEST"),
                new OrderItem(10L, 5L, 10, "13800000001", "INSERT_TEST"))));
        assertThat(addressRepository.selectAll(),
                is(LongStream.range(1L, 11L).mapToObj(each -> new Address(each, "address_test_" + each)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertThat(selectAll(), is(Collections.singletonList(new Order(1L, 0, 2, 2L, "INSERT_TEST"))));
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
        long count = 1L;
        for (Long each : orderIds) {
            orderRepository.deleteShadow(each);
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }
    
    private Collection<Order> selectAll() throws SQLException {
        Collection<Order> result = orderRepository.selectAll();
        result.addAll(orderRepository.selectShadowOrder());
        return result;
    }
    
    private void cleanEnvironment() throws SQLException {
        orderRepository.dropTableShadow();
        orderRepository.dropTableInMySQL();
        orderItemRepository.dropTableInMySQL();
        addressRepository.dropTableInMySQL();
    }
}
