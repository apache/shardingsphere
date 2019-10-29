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

package org.apache.shardingsphere.example.transaction.base.seata.raw.jdbc;

import org.apache.shardingsphere.example.core.api.entity.Order;
import org.apache.shardingsphere.example.core.api.entity.OrderItem;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.apache.shardingsphere.example.core.jdbc.repository.OrderItemRepositoryImpl;
import org.apache.shardingsphere.example.core.jdbc.repository.OrderRepositoryImpl;
import org.apache.shardingsphere.example.core.jdbc.service.OrderServiceImpl;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.LockSupport;

/*
    Please startup seata-server( before running this example.
    Download seata-server from here https://github.com/seata/seata/releases
 */
public class ExampleMain {
    
    private static String configFile = "/META-INF/sharding-databases-tables.yaml";
    
    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(getFile(configFile));
        ExampleService exampleService = getExampleService(dataSource);
        exampleService.initEnvironment();
        processSeataTransaction(dataSource, exampleService);
        exampleService.cleanEnvironment();
    }
    
    private static File getFile(final String fileName) {
        return new File(Thread.currentThread().getClass().getResource(fileName).getFile());
    }
    
    private static ExampleService getExampleService(final DataSource dataSource) {
        return new OrderServiceImpl(dataSource);
    }
    
    private static void processSeataTransaction(final DataSource dataSource, final ExampleService exampleService) throws SQLException {
        TransactionTypeHolder.set(TransactionType.BASE);
        System.out.println("------############## Start seata succeed transaction ##################------");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            insertSuccess(connection, exampleService);
            connection.commit();
        }
        LockSupport.parkUntil(System.currentTimeMillis() + 1000);
        truncateTable(dataSource);
        System.out.println("------############## End seata succeed transaction ######################------");
        System.out.println("------############## Start seata failure transaction ############------");
        TransactionTypeHolder.set(TransactionType.BASE);
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            insertSuccess(connection, exampleService);
            throw new SQLException("exception occur!");
        } catch (final SQLException ex) {
            connection.rollback();
        }
        exampleService.printData();
        System.out.println("------############# End seata failure transaction #############------");
        truncateTable(dataSource);
    }
    
    private static void insertSuccess(final Connection connection, final ExampleService exampleService) throws SQLException {
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setStatus("SEATA-INIT");
            insertOrder(connection, order);
            OrderItem item = new OrderItem();
            item.setUserId(i);
            item.setOrderId(order.getOrderId());
            item.setStatus("SEATA-INIT");
            insertOrderItem(connection, item);
        }
        exampleService.printData();
    }
    
    private static Long insertOrder(final Connection connection, final Order order) {
        String sql = "INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, order.getUserId());
            preparedStatement.setObject(2, order.getAddressId());
            preparedStatement.setObject(3, order.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getLong(1));
                }
            }
        } catch (final SQLException ignored) {
        }
        return order.getOrderId();
    }
    
    private static Long insertOrderItem(final Connection connection, final OrderItem orderItem) {
        String sql = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, orderItem.getOrderId());
            preparedStatement.setObject(2, orderItem.getUserId());
            preparedStatement.setString(3, orderItem.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderItem.setOrderItemId(resultSet.getLong(1));
                }
            }
        } catch (final SQLException ignored) {
        }
        return orderItem.getOrderItemId();
    }
    
    private static void truncateTable(final DataSource dataSource) throws SQLException {
        OrderRepositoryImpl orderRepository = new OrderRepositoryImpl(dataSource);
        OrderItemRepositoryImpl orderItemRepository = new OrderItemRepositoryImpl(dataSource);
        orderRepository.truncateTable();
        orderItemRepository.truncateTable();
    }
}
