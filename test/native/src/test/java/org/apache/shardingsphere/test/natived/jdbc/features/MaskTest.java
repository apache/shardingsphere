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
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskTest {
    
    private OrderRepository orderRepository;
    
    private OrderItemRepository orderItemRepository;
    
    private AddressRepository addressRepository;
    
    @Test
    void assertMaskInLocalTransactions() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/features/mask.yaml");
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
    }
    
    private void processSuccess() throws SQLException {
        final Collection<Long> orderIds = insertData();
        assertThat(orderRepository.selectAll(),
                equalTo(IntStream.range(1, 11).mapToObj(i -> new Order(i, i % 2, i, i, "INSERT_TEST")).collect(Collectors.toList())));
        assertThat(orderItemRepository.selectAll(),
                equalTo(IntStream.range(1, 11).mapToObj(i -> new OrderItem(i, i, i, "138****0001", "INSERT_TEST")).collect(Collectors.toList())));
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1, 11).mapToObj(i -> new Address(i, "address_test_" + i)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertThat(orderRepository.selectAll(), equalTo(new ArrayList<>()));
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
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }
    
    private void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
