/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.repository.jdbc.service;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.api.repository.OrderItemRepository;
import io.shardingsphere.example.repository.api.repository.OrderRepository;
import io.shardingsphere.example.repository.api.service.CommonServiceImpl;
import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemTransactionRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderTransactionRepositoryImpl;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class RawPojoTransactionService extends CommonServiceImpl implements TransactionService {
    
    private final JDBCOrderTransactionRepositoryImpl orderRepository;
    
    private final JDBCOrderItemTransactionRepositoryImpl orderItemRepository;
    
    private Connection connection;
    
    private DataSource dataSource;
    
    public RawPojoTransactionService(final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
        this.orderRepository = new JDBCOrderTransactionRepositoryImpl(connection);
        this.orderItemRepository = new JDBCOrderItemTransactionRepositoryImpl(connection);
    }
    
    @Override
    public void processFailureWithLocal() {
        TransactionTypeHolder.set(TransactionType.LOCAL);
        doInTransactionWithFailure();
    }
    
    @Override
    public void processFailureWithXA() {
        TransactionTypeHolder.set(TransactionType.XA);
        doInTransactionWithFailure();
    }
    
    @Override
    public void processFailureWithBase() {
        TransactionTypeHolder.set(TransactionType.BASE);
        doInTransactionWithFailure();
    }
    
    @Override
    public void processSuccessWithLocal() {
        TransactionTypeHolder.set(TransactionType.LOCAL);
        doInTransactionWithSuccess();
    }
    
    @Override
    public void processSuccessWithXA() {
        TransactionTypeHolder.set(TransactionType.XA);
        doInTransactionWithSuccess();
    }
    
    @Override
    public void processSuccessWithBase() {
        TransactionTypeHolder.set(TransactionType.BASE);
        doInTransactionWithSuccess();
    }
    
    @Override
    public void printTransactionType() {
        System.out.println(String.format("-------------- Process With Transaction %s ---------------", TransactionTypeHolder.get()));
    }
    
    @Override
    protected OrderRepository getOrderRepository() {
        return orderRepository;
    }
    
    @Override
    protected OrderItemRepository getOrderItemRepository() {
        return orderItemRepository;
    }
    
    @Override
    protected Order newOrder() {
        return new Order();
    }
    
    @Override
    protected OrderItem newOrderItem() {
        return new OrderItem();
    }
    
    private void doInTransactionWithFailure() {
        try {
            createNewConnection();
            beginTransaction();
            super.processFailure();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            rollbackTransaction();
            super.printData();
            closeConnection();
        }
    }
    
    private void doInTransactionWithSuccess() {
        try {
            createNewConnection();
            beginTransaction();
            super.processSuccess();
            commitTransaction();
            closeConnection();
        } catch (final SQLException ignore) {
        }
    }
    
    private void createNewConnection() throws SQLException {
        printTransactionType();
        connection = dataSource.getConnection();
        orderRepository.setConnection(connection);
        orderItemRepository.setConnection(connection);
    }
    
    private void closeConnection() {
        try {
            connection.close();
        } catch (final SQLException ignore ) {
        }
    }
    
    private void beginTransaction() {
        try {
            if (null != this.connection && !this.connection.isClosed()) {
                this.connection.setAutoCommit(false);
            }
        } catch (SQLException ignored) {
        }
    }
    
    private void commitTransaction() {
        try {
            if (null != this.connection && !this.connection.isClosed()) {
                this.connection.commit();
            }
        } catch (SQLException ignored) {
        }
    }
    
    private void rollbackTransaction() {
        try {
            if (null != this.connection && !this.connection.isClosed()) {
                this.connection.rollback();
            }
        } catch (SQLException ignored) {
        }
    }
}
