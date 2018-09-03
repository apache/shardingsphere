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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DataRepository {

    private final DataSource dataSource;

    public DataRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void demo() throws Exception {
        dropTable(dataSource);
        createTable(dataSource);
        insert(dataSource);
        updateFailure(dataSource);
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
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try {
            for (int i = 0; i < 100; i++) {
                String sql = String.format("INSERT INTO t_order VALUES (%s, %s, 'INIT');", 21474843647L + i, i);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
        }
    }

    private void updateFailure(final DataSource dataSource) throws Exception {
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
