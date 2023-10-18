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

package org.apache.shardingsphere.infra.nativetest.jdbc.features;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.nativetest.FileTestUtils;
import org.apache.shardingsphere.infra.nativetest.jdbc.features.entity.Address;
import org.apache.shardingsphere.infra.nativetest.jdbc.features.entity.Order;
import org.apache.shardingsphere.infra.nativetest.jdbc.features.entity.OrderItem;
import org.apache.shardingsphere.infra.nativetest.jdbc.features.repository.AddressRepository;
import org.apache.shardingsphere.infra.nativetest.jdbc.features.repository.OrderItemRepository;
import org.apache.shardingsphere.infra.nativetest.jdbc.features.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingTest {
    
    private OrderRepository orderRepository;
    
    private OrderItemRepository orderItemRepository;
    
    private AddressRepository addressRepository;
    
    @Test
    void testShardingInLocalTransactions() throws SQLException, IOException {
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("yaml/sharding.yaml"));
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
        addressRepository = new AddressRepository(dataSource);
        this.initEnvironment();
        this.processSuccess();
    }
    
    private void initEnvironment() throws SQLException {
        orderRepository.createTableIfNotExists();
        orderItemRepository.createTableIfNotExists();
        addressRepository.createTableIfNotExists();
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
        addressRepository.truncateTable();
    }
    
    private void processSuccess() throws SQLException {
        final List<Long> orderIds = insertData();
        
        List<Order> orders = orderRepository.selectAll();
        assertThat(orders.stream().map(Order::getOrderType).collect(Collectors.toList()),
                equalTo(Arrays.asList(1, 1, 1, 1, 1, 0, 0, 0, 0, 0)));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 2, 4, 6, 8, 10))));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(1L, 3L, 5L, 7L, 9L, 2L, 4L, 6L, 8L, 10L))));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        
        List<OrderItem> orderItems = orderItemRepository.selectAll();
        assertThat(orderItems.stream().map(OrderItem::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 2, 4, 6, 8, 10))));
        assertThat(orderItems.stream().map(OrderItem::getPhone).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "13800000001").collect(Collectors.toList())));
        assertThat(orderItems.stream().map(OrderItem::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1, 11).mapToObj(i -> new Address(i, "address_test_" + i)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertThat(orderRepository.selectAll(), equalTo(new ArrayList<>()));
        assertThat(orderItemRepository.selectAll(), equalTo(new ArrayList<>()));
        assertThat(addressRepository.selectAll(), equalTo(new ArrayList<>()));
    }
    
    private List<Long> insertData() throws SQLException {
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
            
            Address address = new Address((long) i, "address_test_" + i);
            addressRepository.insert(address);
            
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private void deleteData(final List<Long> orderIds) throws SQLException {
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
