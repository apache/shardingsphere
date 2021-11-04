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

package org.apache.shardingsphere.example.${feature}.${framework?replace('-', '.')};

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.example.${feature}.${framework?replace('-', '.')}.entity.Order;
import org.apache.shardingsphere.example.${feature}.${framework?replace('-', '.')}.entity.OrderItem;
<#if framework?contains("spring")>
import org.springframework.stereotype.Service;
</#if>

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

<#assign frameworkName="">
<#list framework?split("-") as framework1>
    <#assign frameworkName=frameworkName + framework1?cap_first>
</#list>
<#if framework?contains("spring")>
@Service
</#if>
@AllArgsConstructor
public final class ${mode?cap_first}${transaction?cap_first}${feature?cap_first}${frameworkName}ExampleService {
    
    private final DataSource dataSource;

    /**
     * Execute test.
     *
     * @throws SQLException
     */
    public void run() throws SQLException {
        try {
            this.initEnvironment();
            this.processSuccess();
        } finally {
            this.cleanEnvironment();
        }
    }
    
    /**
     * Initialize the database test environment.
     * @throws SQLException
     */
    private void initEnvironment() throws SQLException {
        String createOrderTableSql = "CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))";
        String createOrderItemTableSql = "CREATE TABLE IF NOT EXISTS t_order_item "
                + "(order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_item_id))";
        String createAddressTableSql = "CREATE TABLE IF NOT EXISTS t_address "
                + "(address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))";
        String truncateOrderTable = "TRUNCATE TABLE t_order";
        String truncateOrderItemTable = "TRUNCATE TABLE t_order_item";
        String truncateAddressTableSql = "TRUNCATE TABLE t_address";
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createOrderTableSql);
            statement.executeUpdate(createOrderItemTableSql);
            statement.executeUpdate(createAddressTableSql);
            statement.executeUpdate(createAddressTableSql);
            statement.executeUpdate(truncateOrderTable);
            statement.executeUpdate(truncateOrderItemTable);
            statement.executeUpdate(truncateAddressTableSql);
        }
    }
    
    private void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData(); 
        deleteData(orderIds);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }
    
    private void processFailure() throws SQLException {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = insertOrder(i);
            insertOrderItem(i, order);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    private Order insertOrder(final int i) throws SQLException {
        Order order = new Order();
        order.setUserId(i);
        order.setAddressId(i);
        order.setStatus("INSERT_TEST");
        String sql = "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setLong(2, order.getAddressId());
            preparedStatement.setString(3, order.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getLong(1));
                }
            }
        }
        return order;
    }

    private void insertOrderItem(final int i, final Order order) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setUserId(i);
        orderItem.setStatus("INSERT_TEST");
        String sql = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderItem.getOrderId());
            preparedStatement.setInt(2, orderItem.getUserId());
            preparedStatement.setString(3, orderItem.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderItem.setOrderItemId(resultSet.getLong(1));
                }
            }
        }
    }

    private void deleteData(final List<Long> orderIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            String deleteOrderSql = "DELETE FROM t_order WHERE order_id=?";
            String deleteOrderItemSql = "DELETE FROM t_order_item WHERE order_id=?";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement orderPreparedStatement = connection.prepareStatement(deleteOrderSql);
                 PreparedStatement orderItemPreparedStatement = connection.prepareStatement(deleteOrderItemSql)) {
                orderPreparedStatement.setLong(1, each);
                orderItemPreparedStatement.setLong(1, each);
                orderPreparedStatement.executeUpdate();
                orderItemPreparedStatement.executeUpdate();
            }
        }
    }
    
    private void printData() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : this.getOrders()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print OrderItem Data -------------------");
        for (Object each : this.getOrderItems()) {
            System.out.println(each);
        }
    }

    protected List<OrderItem> getOrderItems() throws SQLException {
        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        List<OrderItem> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(resultSet.getLong(1));
                orderItem.setOrderId(resultSet.getLong(2));
                orderItem.setUserId(resultSet.getInt(3));
                orderItem.setStatus(resultSet.getString(4));
                result.add(orderItem);
            }
        }
        return result;
    }
    
    protected List<Order> getOrders() throws SQLException {
        String sql = "SELECT * FROM t_order";
        List<Order> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong(1));
                order.setUserId(resultSet.getInt(2));
                order.setAddressId(resultSet.getLong(3));
                order.setStatus(resultSet.getString(4));
                result.add(order);
            }
        }
        return result;
    }
    
    /**
     * Restore the environment.
     * @throws SQLException
     */
    private void cleanEnvironment() throws SQLException {
        String dropOrderSql = "DROP TABLE t_order";
        String dropOrderItemSql = "DROP TABLE t_order_item";
        String dropAddressSql = "DROP TABLE t_address";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropOrderSql);
            statement.executeUpdate(dropOrderItemSql);
            statement.executeUpdate(dropAddressSql);
        }
    }
}
