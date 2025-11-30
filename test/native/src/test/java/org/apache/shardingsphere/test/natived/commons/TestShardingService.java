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

package org.apache.shardingsphere.test.natived.commons;

import lombok.Getter;
import org.apache.shardingsphere.test.natived.commons.entity.Address;
import org.apache.shardingsphere.test.natived.commons.entity.Order;
import org.apache.shardingsphere.test.natived.commons.entity.OrderItem;
import org.apache.shardingsphere.test.natived.commons.repository.AddressRepository;
import org.apache.shardingsphere.test.natived.commons.repository.OrderItemRepository;
import org.apache.shardingsphere.test.natived.commons.repository.OrderRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void processSuccess() throws SQLException {
        processSuccessWithoutTransactions();
        orderItemRepository.assertRollbackWithTransactions();
    }
    
    /**
     * Process success in ClickHouse. ClickHouse JDBC Driver does not support the use of transactions.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void processSuccessInClickHouse() throws SQLException {
        Collection<Long> orderIds = insertData(Statement.NO_GENERATED_KEYS);
        assertQueryInClickHouse();
        deleteDataInClickHouse(orderIds);
        assertTrue(orderRepository.selectAll().isEmpty());
        assertTrue(orderItemRepository.selectAll().isEmpty());
        assertTrue(addressRepository.selectAll().isEmpty());
    }
    
    private void assertQueryInClickHouse() throws SQLException {
        Collection<Order> orders = orderRepository.selectAll();
        assertThat(orders.stream().map(Order::getOrderId).collect(Collectors.toList()), not(empty()));
        assertThat(orders.stream().map(Order::getOrderType).collect(Collectors.toList()),
                containsInAnyOrder(0, 1, 0, 1, 0, 1, 0, 1, 0, 1));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toList()),
                containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toList()),
                containsInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                is(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        Collection<OrderItem> orderItems = orderItemRepository.selectAll();
        assertThat(orderItems.stream().map(OrderItem::getOrderItemId).collect(Collectors.toList()), not(empty()));
        assertThat(orderItems.stream().map(OrderItem::getOrderId).collect(Collectors.toList()), not(empty()));
        assertThat(orderItems.stream().map(OrderItem::getUserId).collect(Collectors.toList()),
                containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertThat(orderItems.stream().map(OrderItem::getPhone).collect(Collectors.toList()),
                is(IntStream.range(1, 11).mapToObj(i -> "13800000001").collect(Collectors.toList())));
        assertThat(orderItems.stream().map(OrderItem::getStatus).collect(Collectors.toList()),
                is(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        assertThat(new HashSet<>(addressRepository.selectAll()),
                is(LongStream.range(1L, 11L).mapToObj(each -> new Address(each, "address_test_" + each)).collect(Collectors.toSet())));
    }
    
    private void deleteDataInClickHouse(final Collection<Long> orderIds) throws SQLException {
        long count = 1L;
        for (Long each : orderIds) {
            orderRepository.deleteInClickHouse(each);
            orderItemRepository.deleteInClickHouse(each);
            addressRepository.deleteInClickHouse(count++);
        }
    }
    
    /**
     * Process success in Hive.
     * Hive has not fully supported BEGIN, COMMIT, and ROLLBACK. Refer to <a href="https://cwiki.apache.org/confluence/display/Hive/Hive+Transactions">Hive Transactions</a>.
     * So ShardingSphere should not use {@link OrderItemRepository#assertRollbackWithTransactions()}
     * TODO It looks like HiveServer2 insert statements are inserted out of order. Waiting for further investigation.
     *  The result of the insert is not currently asserted.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void processSuccessInHive() throws SQLException {
        Collection<Long> orderIds = insertData(Statement.RETURN_GENERATED_KEYS);
        deleteData(orderIds);
        assertTrue(orderRepository.selectAll().isEmpty());
        assertTrue(orderItemRepository.selectAll().isEmpty());
        assertTrue(addressRepository.selectAll().isEmpty());
    }
    
    /**
     * Process success in Presto Iceberg Connector or Doris FE.
     * There are bugs with Presto's transaction support, see <a href="https://github.com/prestodb/presto/issues/25204">prestodb/presto#25204</a> .
     * Can't execute {@code orderItemRepository.assertRollbackWithTransactions();} here.
     * There is a bug with Doris FE's support for transaction rollback.
     * Statements that have been successfully executed in a single transaction unit will not be rolled back.
     * Refer to <a href="https://doris.apache.org/docs/3.0/data-operate/transaction#failed-statements-within-a-transaction">Failed Statements Within a Transaction</a> .
     *
     * @throws SQLException SQL exception
     */
    public void processSuccessWithoutTransactions() throws SQLException {
        Collection<Long> orderIds = insertData(Statement.RETURN_GENERATED_KEYS);
        assertQueryInClickHouse();
        deleteData(orderIds);
        assertTrue(orderRepository.selectAll().isEmpty());
        assertTrue(orderItemRepository.selectAll().isEmpty());
        assertTrue(addressRepository.selectAll().isEmpty());
    }
    
    /**
     * Insert data.
     *
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys should be returned; one of {@code Statement.RETURN_GENERATED_KEYS} or {@code Statement.NO_GENERATED_KEYS}
     * @return orderId of the insert statement
     * @throws SQLException An exception that provides information on a database access error or other errors
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
     * @param orderIds orderId of the insert statement
     * @throws SQLException An exception that provides information on a database access error or other errors
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
     * Clean environment.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTableInMySQL();
        orderItemRepository.dropTableInMySQL();
        addressRepository.dropTableInMySQL();
    }
    
    /**
     * Clean environment in Firebird.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void cleanEnvironmentInFirebird() throws SQLException {
        orderRepository.dropTableInFirebird();
        orderItemRepository.dropTableInFirebird();
        addressRepository.dropTableInFirebird();
    }
}
