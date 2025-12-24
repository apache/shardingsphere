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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
     * Process success in ClickHouse.
     * ClickHouse JDBC Driver does not support the use of transactions.
     * Databases like ClickHouse do not support returning auto generated keys after executing SQL,
     * see <a href="https://github.com/ClickHouse/ClickHouse/issues/56228">ClickHouse/ClickHouse#56228</a> .
     * TODO The current ShardingSphere parsing of ClickHouse's `INNER JOIN` syntax has shortcomings,
     *  and it returns incorrect query results for SQL statements such as `SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id`.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void processSuccessInClickHouse() throws SQLException {
        Collection<Long> orderIds = insertDataWithoutGeneratedKeys();
        assertQueryLoose();
        deleteDataInClickHouse(orderIds);
        assertTrue(orderRepository.selectAll().isEmpty());
        assertTrue(orderItemRepository.selectAll().isEmpty());
        assertTrue(addressRepository.selectAll().isEmpty());
    }
    
    /**
     * Process success in Hive.
     * Hive has not fully supported BEGIN, COMMIT, and ROLLBACK. Refer to <a href="https://cwiki.apache.org/confluence/display/Hive/Hive+Transactions">Hive Transactions</a>.
     * So ShardingSphere should not use {@link OrderItemRepository#assertRollbackWithTransactions()}
     * TODO The current ShardingSphere parsing of HiveServer2's `INNER JOIN` syntax has shortcomings,
     *  and it returns incorrect query results for SQL statements such as `SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id`.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void processSuccessInHive() throws SQLException {
        Collection<Long> orderIds = insertData();
        assertQueryLoose();
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
        Collection<Long> orderIds = insertData();
        assertQuery();
        deleteData(orderIds);
        assertTrue(orderRepository.selectAll().isEmpty());
        assertTrue(orderItemRepository.selectAll().isEmpty());
        assertTrue(addressRepository.selectAll().isEmpty());
    }
    
    private void assertQuery() throws SQLException {
        assertQueryInTOrder();
        assertQueryInTOrderItem(orderItemRepository.selectAll());
        assertQueryInTAddress();
    }
    
    private void assertQueryLoose() throws SQLException {
        assertQueryInTOrder();
        assertQueryInTOrderItem(orderItemRepository.selectAllLoose());
        assertQueryInTAddress();
    }
    
    private void assertQueryInTOrder() throws SQLException {
        List<Order> orders = orderRepository.selectAll();
        assertThat(orders.stream().map(Order::getOrderId).collect(Collectors.toList()), not(empty()));
        assertThat(orders.stream().map(Order::getOrderType).collect(Collectors.toList()),
                containsInAnyOrder(0, 1, 0, 1, 0, 1, 0, 1, 0, 1));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toList()),
                containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toList()),
                containsInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                is(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
    }
    
    private void assertQueryInTOrderItem(final List<OrderItem> orderItems) {
        assertThat(orderItems.stream().map(OrderItem::getOrderItemId).collect(Collectors.toList()), not(empty()));
        assertThat(orderItems.stream().map(OrderItem::getOrderId).collect(Collectors.toList()), not(empty()));
        assertThat(orderItems.stream().map(OrderItem::getUserId).collect(Collectors.toList()),
                containsInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertThat(orderItems.stream().map(OrderItem::getPhone).collect(Collectors.toList()),
                is(IntStream.range(1, 11).mapToObj(i -> "13800000001").collect(Collectors.toList())));
        assertThat(orderItems.stream().map(OrderItem::getStatus).collect(Collectors.toList()),
                is(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
    }
    
    private void assertQueryInTAddress() throws SQLException {
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
     * Insert data.
     *
     * @return orderId of the insert statement
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public Collection<Long> insertData() throws SQLException {
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
    
    /**
     * Insert data without generated keys.
     *
     * @return orderId of the insert statement
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public Collection<Long> insertDataWithoutGeneratedKeys() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insertWithoutGeneratedKeys(order);
            Address address = new Address((long) i, "address_test_" + i);
            addressRepository.insert(address);
        }
        List<Long> result = orderRepository.selectAll().stream().map(Order::getOrderId).collect(Collectors.toList());
        for (int i = 1; i <= 10; i++) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(result.get(i - 1));
            orderItem.setUserId(i);
            orderItem.setPhone("13800000001");
            orderItem.setStatus("INSERT_TEST");
            orderItemRepository.insertWithoutGeneratedKeys(orderItem);
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
     * Clean environment without verify. See <a href="https://github.com/FirebirdSQL/firebird/issues/4203">FirebirdSQL/firebird#4203</a>.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors
     */
    public void cleanEnvironmentWithoutVerify() throws SQLException {
        orderRepository.dropTableWithoutVerify();
        orderItemRepository.dropTableWithoutVerify();
        addressRepository.dropTableWithoutVerify();
    }
}
