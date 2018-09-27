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

package io.shardingsphere.example.transaction.fixture;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.shardingjdbc.transaction.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataRepository {

    private final DataSource dataSource;
    
    private final TransactionType transactionType;

    public DataRepository(final DataSource dataSource, final TransactionType transactionType) {
        this.dataSource = dataSource;
        this.transactionType = transactionType;
    }

    public void demo() throws Exception {
        dropTable(dataSource);
        createTable(dataSource);
        insert(dataSource);
        queryWithEqual();
        updateSuccess(dataSource);
        queryWithEqual();
        updateFailure(dataSource);
        queryWithEqual();
    }

    private void createTable(final DataSource dataSource) throws SQLException {
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT NOT NULL AUTO_INCREMENT, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (order_item_id))");
    }

    private void dropTable(final DataSource dataSource) throws SQLException {
        executeUpdate(dataSource, "DROP TABLE IF EXISTS t_order_item");
        executeUpdate(dataSource, "DROP TABLE IF EXISTS t_order");
    }

    private void executeUpdate(final DataSource dataSource, final String sql) throws SQLException {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    private void insert(final DataSource dataSource) throws Exception {
        TransactionTypeHolder.set(transactionType);
        System.out.println("1.Insert--------------");
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try {
            for (int i = 0; i < 10; i++) {
                String orderSql = String.format("INSERT INTO t_order VALUES (%s, %s, 'INIT');", 21474843647L + i, i);
                String itemSql = String.format("INSERT INTO t_order_item VALUES (%s, %s);", 21474843647L + i, i);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(orderSql);
                    statement.executeUpdate(itemSql);
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
        } finally {
            connection.close();
        }
    }
    
    private void queryWithEqual() throws SQLException {
        TransactionTypeHolder.set(transactionType);
        String sql = "SELECT i.*, o.status FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.print("order_item_id:" + resultSet.getLong(1) + ", ");
                    System.out.print("order_id:" + resultSet.getLong(2) + ", ");
                    System.out.print("user_id:" + resultSet.getInt(3) + ", ");
                    System.out.print("status:" + resultSet.getString(4));
                    System.out.println();
                }
            }
        }
    }

    private void updateSuccess(final DataSource dataSource) throws Exception {
        TransactionTypeHolder.set(transactionType);
        System.out.println("2.Update Success--------------");
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        String sql = "UPDATE t_order SET status='UPDATE_1' WHERE user_id=? AND order_id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, 0);
            preparedStatement.setObject(2, 21474843647L);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
        }
    }
    
    private void updateFailure(final DataSource dataSource) throws Exception {
        TransactionTypeHolder.set(transactionType);
        System.out.println("3.Update Failed--------------");
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        String sql1 = "UPDATE t_order SET status='UPDATE_1' WHERE user_id=0 AND order_id=?";
        String sql2 = "UPDATE t_order SET not_existed_column=1 WHERE user_id=1 AND order_id=?";
        String sql3 = "UPDATE t_order SET status='UPDATE_2' WHERE user_id=0 AND order_id=?";
        try (PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
             PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
             PreparedStatement preparedStatement3 = connection.prepareStatement(sql3)) {
            preparedStatement1.setObject(1, 21474843647L);
            preparedStatement2.setObject(1, 21474843647L);
            preparedStatement3.setObject(1, 21474843647L);
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
        }
    }
}
