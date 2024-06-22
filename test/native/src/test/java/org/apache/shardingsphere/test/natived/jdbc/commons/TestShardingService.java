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

package org.apache.shardingsphere.test.natived.jdbc.commons;

import lombok.Getter;
import org.apache.shardingsphere.test.natived.jdbc.commons.entity.Address;
import org.apache.shardingsphere.test.natived.jdbc.commons.entity.Order;
import org.apache.shardingsphere.test.natived.jdbc.commons.entity.OrderItem;
import org.apache.shardingsphere.test.natived.jdbc.commons.repository.AddressRepository;
import org.apache.shardingsphere.test.natived.jdbc.commons.repository.OrderItemRepository;
import org.apache.shardingsphere.test.natived.jdbc.commons.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Getter
public final class TestShardingService {
    
    private final OrderRepository orderRepository;
    
    private final OrderItemRepository orderItemRepository;
    
    private final AddressRepository addressRepository;
    
    public TestShardingService(final DataSource dataSource) {
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
        addressRepository = new AddressRepository(dataSource);
    }
    
    /**
     * Process success.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void processSuccess() throws SQLException {
        final Collection<Long> orderIds = insertData(Statement.RETURN_GENERATED_KEYS);
        Collection<Order> orders = orderRepository.selectAll();
        assertThat(orders.stream().map(Order::getOrderType).collect(Collectors.toList()),
                equalTo(Arrays.asList(0, 0, 0, 0, 0, 1, 1, 1, 1, 1)));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(2, 4, 6, 8, 10, 1, 3, 5, 7, 9))));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(2L, 4L, 6L, 8L, 10L, 1L, 3L, 5L, 7L, 9L))));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        Collection<OrderItem> orderItems = orderItemRepository.selectAll();
        assertThat(orderItems.stream().map(OrderItem::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(2, 4, 6, 8, 10, 1, 3, 5, 7, 9))));
        assertThat(orderItems.stream().map(OrderItem::getPhone).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "13800000001").collect(Collectors.toList())));
        assertThat(orderItems.stream().map(OrderItem::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1L, 11L).mapToObj(each -> new Address(each, "address_test_" + each)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertThat(orderRepository.selectAll(), equalTo(Collections.emptyList()));
        assertThat(orderItemRepository.selectAll(), equalTo(Collections.emptyList()));
        assertThat(addressRepository.selectAll(), equalTo(Collections.emptyList()));
        orderItemRepository.assertRollbackWithTransactions();
    }
    
    /**
     * Process success in ClickHouse.
     * ClickHouse has not fully supported transactions. Refer to <a href="https://github.com/ClickHouse/clickhouse-docs/issues/2300">ClickHouse/clickhouse-docs#2300</a>.
     * So ShardingSphere should not use {@link OrderItemRepository#assertRollbackWithTransactions()} in the method here.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void processSuccessInClickHouse() throws SQLException {
        final Collection<Long> orderIds = insertData(Statement.NO_GENERATED_KEYS);
        Collection<Order> orders = orderRepository.selectAll();
        assertThat(orders.stream().map(Order::getOrderType).collect(Collectors.toList()),
                equalTo(Arrays.asList(0, 0, 0, 0, 0, 1, 1, 1, 1, 1)));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(2, 4, 6, 8, 10, 1, 3, 5, 7, 9))));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(2L, 4L, 6L, 8L, 10L, 1L, 3L, 5L, 7L, 9L))));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        Collection<OrderItem> orderItems = orderItemRepository.selectAll();
        assertThat(orderItems.stream().map(OrderItem::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(2, 4, 6, 8, 10, 1, 3, 5, 7, 9))));
        assertThat(orderItems.stream().map(OrderItem::getPhone).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "13800000001").collect(Collectors.toList())));
        assertThat(orderItems.stream().map(OrderItem::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1L, 11L).mapToObj(each -> new Address(each, "address_test_" + each)).collect(Collectors.toList())));
        deleteDataInClickHouse(orderIds);
        assertThat(orderRepository.selectAll(), equalTo(Collections.emptyList()));
        assertThat(orderItemRepository.selectAll(), equalTo(Collections.emptyList()));
        assertThat(addressRepository.selectAll(), equalTo(Collections.emptyList()));
    }
    
    /**
     * Insert data.
     *
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys
     *                          should be returned; one of
     *                          {@code Statement.RETURN_GENERATED_KEYS} or
     *                          {@code Statement.NO_GENERATED_KEYS}
     * @return orderId of the insert statement.
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public Collection<Long> insertData(final int autoGeneratedKeys) throws SQLException {
        Collection<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order, autoGeneratedKeys);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setUserId(i);
            orderItem.setPhone("13800000001");
            orderItem.setStatus("INSERT_TEST");
            orderItemRepository.insert(orderItem, autoGeneratedKeys);
            Address address = new Address((long) i, "address_test_" + i);
            addressRepository.insert(address);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    /**
     * Delete data.
     *
     * @param orderIds orderId of the insert statement.
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void deleteData(final Collection<Long> orderIds) throws SQLException {
        long count = 1L;
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }
    
    /**
     * Delete data in ClickHouse.
     *
     * @param orderIds orderId of the insert statement.
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void deleteDataInClickHouse(final Collection<Long> orderIds) throws SQLException {
        long count = 1L;
        for (Long each : orderIds) {
            orderRepository.deleteInClickHouse(each);
            orderItemRepository.deleteInClickHouse(each);
            addressRepository.deleteInClickHouse(count++);
        }
    }
    
    /**
     * Clean environment.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
