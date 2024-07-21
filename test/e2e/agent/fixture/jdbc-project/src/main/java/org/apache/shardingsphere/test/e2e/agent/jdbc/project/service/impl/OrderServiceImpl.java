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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.enums.StatementType;
import org.apache.shardingsphere.test.e2e.agent.jdbc.project.service.OrderService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Order service impl.
 */
@RequiredArgsConstructor
@Slf4j
public final class OrderServiceImpl implements OrderService {
    
    private final Connection connection;
    
    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL, user_id BIGINT DEFAULT NULL, status VARCHAR(32) DEFAULT NULL, PRIMARY KEY (order_id))";
        execute(sql, true, false);
    }
    
    @Override
    public void dropTable() {
        String sql = "DROP TABLE IF EXISTS t_order";
        execute(sql, true, false);
    }
    
    @Override
    public void insert(final OrderEntity order, final StatementType statementType, final boolean isRollback) {
        if (StatementType.PREPARED == statementType) {
            String sql = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
            log.info("execute sql:{}", sql);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                connection.setAutoCommit(false);
                preparedStatement.setLong(1, order.getOrderId());
                preparedStatement.setLong(2, order.getUserId());
                preparedStatement.setString(3, order.getStatus());
                preparedStatement.executeUpdate();
                if (isRollback) {
                    connection.rollback();
                } else {
                    connection.commit();
                }
            } catch (final SQLException ex) {
                log.error(String.format("execute `%s` error", sql), ex);
            }
        } else if (StatementType.STATEMENT == statementType) {
            String sql = String.format("INSERT INTO t_order (order_id, user_id, status) VALUES (%d, %d, '%s')", order.getOrderId(), order.getUserId(), order.getStatus());
            execute(sql, false, isRollback);
        }
    }
    
    @Override
    public void delete(final Long orderId, final StatementType statementType) {
        if (StatementType.PREPARED == statementType) {
            String sql = "DELETE FROM t_order WHERE order_id=?";
            log.info("execute sql:{}", sql);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, orderId);
                preparedStatement.executeUpdate();
            } catch (final SQLException ex) {
                log.error(String.format("execute `%s` error", sql), ex);
            }
        } else if (StatementType.STATEMENT == statementType) {
            String sql = String.format("DELETE FROM t_order WHERE order_id = %d", orderId);
            execute(sql, true, false);
        }
    }
    
    @Override
    public void update(final OrderEntity order, final StatementType statementType) {
        if (StatementType.PREPARED == statementType) {
            String sql = "UPDATE t_order SET status = ? WHERE order_id = ?";
            log.info("execute sql:{}", sql);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                connection.setAutoCommit(false);
                preparedStatement.setString(1, order.getStatus());
                preparedStatement.setLong(2, order.getOrderId());
                preparedStatement.executeUpdate();
                connection.commit();
            } catch (final SQLException ex) {
                log.error(String.format("execute `%s` error", sql), ex);
            }
        } else if (StatementType.STATEMENT == statementType) {
            String sql = String.format("UPDATE t_order SET status = '%s' WHERE order_id = %d", order.getStatus(), order.getOrderId());
            execute(sql, false, false);
        }
    }
    
    @Override
    public Collection<OrderEntity> selectAll(final StatementType statementType) {
        String sql = "SELECT * FROM t_order";
        log.info("execute sql:{}", sql);
        if (StatementType.PREPARED == statementType) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                return getOrders(preparedStatement.executeQuery());
            } catch (final SQLException ex) {
                log.error(String.format("execute `%s` error", sql), ex);
            }
        } else if (StatementType.STATEMENT == statementType) {
            try (Statement statement = connection.createStatement()) {
                return getOrders(statement.executeQuery(sql));
            } catch (final SQLException ex) {
                log.error(String.format("execute `%s` error", sql), ex);
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<OrderEntity> getOrders(final ResultSet resultSet) throws SQLException {
        Collection<OrderEntity> result = new LinkedList<>();
        while (resultSet.next()) {
            OrderEntity orderEntity = new OrderEntity(resultSet.getLong(1), resultSet.getLong(2), resultSet.getString(3));
            result.add(orderEntity);
        }
        return result;
    }
    
    private void execute(final String sql, final boolean autoCommit, final boolean isRollback) {
        log.info("execute sql:{}", sql);
        try (Statement statement = connection.createStatement()) {
            if (autoCommit) {
                statement.execute(sql);
            } else {
                connection.setAutoCommit(false);
                statement.execute(sql);
                if (isRollback) {
                    connection.rollback();
                } else {
                    connection.commit();
                }
            }
        } catch (final SQLException ex) {
            log.error(String.format("execute `%s` error", sql), ex);
        }
    }
}
