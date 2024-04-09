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
import org.apache.shardingsphere.test.natived.jdbc.commons.entity.Address;
import org.apache.shardingsphere.test.natived.jdbc.commons.entity.Order;
import org.apache.shardingsphere.test.natived.jdbc.commons.entity.OrderItem;
import org.apache.shardingsphere.test.natived.jdbc.commons.repository.AddressRepository;
import org.apache.shardingsphere.test.natived.jdbc.commons.repository.OrderItemRepository;
import org.apache.shardingsphere.test.natived.jdbc.commons.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowTest {
    
    private OrderRepository orderRepository;
    
    private OrderItemRepository orderItemRepository;
    
    private AddressRepository addressRepository;
    
    @Test
    void assertShadowInLocalTransactions() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/features/shadow.yaml");
        DataSource dataSource = new HikariDataSource(config);
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
        addressRepository = new AddressRepository(dataSource);
        this.initEnvironment();
        this.processSuccess();
        this.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExistsInMySQL();
        orderItemRepository.createTableIfNotExistsInMySQL();
        addressRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        addressRepository.truncateTable();
        orderRepository.createTableIfNotExistsShadow();
        orderRepository.truncateTableShadow();
    }
    
    private void processSuccess() throws SQLException {
        final Collection<Long> orderIds = insertData();
        assertThat(this.selectAll(), equalTo(Arrays.asList(
                new Order(1, 0, 2, 2, "INSERT_TEST"),
                new Order(2, 0, 4, 4, "INSERT_TEST"),
                new Order(3, 0, 6, 6, "INSERT_TEST"),
                new Order(4, 0, 8, 8, "INSERT_TEST"),
                new Order(5, 0, 10, 10, "INSERT_TEST"),
                new Order(1, 1, 1, 1, "INSERT_TEST"),
                new Order(2, 1, 3, 3, "INSERT_TEST"),
                new Order(3, 1, 5, 5, "INSERT_TEST"),
                new Order(4, 1, 7, 7, "INSERT_TEST"),
                new Order(5, 1, 9, 9, "INSERT_TEST"))));
        assertThat(orderItemRepository.selectAll(), equalTo(Arrays.asList(
                new OrderItem(1, 1, 1, "13800000001", "INSERT_TEST"),
                new OrderItem(2, 1, 2, "13800000001", "INSERT_TEST"),
                new OrderItem(3, 2, 3, "13800000001", "INSERT_TEST"),
                new OrderItem(4, 2, 4, "13800000001", "INSERT_TEST"),
                new OrderItem(5, 3, 5, "13800000001", "INSERT_TEST"),
                new OrderItem(6, 3, 6, "13800000001", "INSERT_TEST"),
                new OrderItem(7, 4, 7, "13800000001", "INSERT_TEST"),
                new OrderItem(8, 4, 8, "13800000001", "INSERT_TEST"),
                new OrderItem(9, 5, 9, "13800000001", "INSERT_TEST"),
                new OrderItem(10, 5, 10, "13800000001", "INSERT_TEST"))));
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1, 11).mapToObj(i -> new Address(i, "address_test_" + i)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertThat(this.selectAll(), equalTo(Collections.singletonList(new Order(1, 0, 2, 2, "INSERT_TEST"))));
        assertThat(orderItemRepository.selectAll(), equalTo(new ArrayList<>()));
        assertThat(addressRepository.selectAll(), equalTo(new ArrayList<>()));
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
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
