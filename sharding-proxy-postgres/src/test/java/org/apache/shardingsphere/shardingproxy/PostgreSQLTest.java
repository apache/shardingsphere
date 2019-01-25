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

package org.apache.shardingsphere.shardingproxy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLTest {
    
    @Test
    public void assertPgSQL1() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/db_0");
        config.setUsername("postgres");
        config.setPassword("postgres");
        DataSource dataSource = new HikariDataSource(config);
        Connection connection = dataSource.getConnection();
        for (int i = 0; i < 10; i++) {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from t_order where order_id = ?");
            preparedStatement.setInt(1, 4);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                System.out.println(resultSet.getInt(1) + ", " + resultSet.getInt(2) + ", " + resultSet.getString(3));
            }
            resultSet.close();
            preparedStatement.close();
        }
    }
    
    @Test
    public void assertPgSQL2() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/db_0?prepareThreshold=1");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setMaximumPoolSize(100);
        DataSource dataSource = new HikariDataSource(config);
        Connection connection1 = dataSource.getConnection();
        for (int i = 0; i < 10; i++) {
            PreparedStatement preparedStatement1 = connection1.prepareStatement("select * from t_order where order_id = ?");
            preparedStatement1.setInt(1, 4);
            preparedStatement1.execute();
            ResultSet resultSet1 = preparedStatement1.getResultSet();
            while (resultSet1.next()) {
                System.out.println("pstmt1 " + resultSet1.getInt(1) + ", " + resultSet1.getInt(2) + ", " + resultSet1.getString(3));
            }
            resultSet1.close();
            preparedStatement1.close();
        }
        connection1.close();
        System.out.println();
        Connection connection2 = dataSource.getConnection();
        for (int i = 0; i < 10; i++) {
            PreparedStatement preparedStatement2 = connection2.prepareStatement("select * from t_order where order_id = ?");
            preparedStatement2.setInt(1, 4);
            preparedStatement2.execute();
            ResultSet resultSet2 = preparedStatement2.getResultSet();
            while (resultSet2.next()) {
                System.out.println("pstmt2 " + resultSet2.getInt(1) + ", " + resultSet2.getInt(2) + ", " + resultSet2.getString(3));
            }
            resultSet2.close();
            preparedStatement2.close();
        }
        connection2.close();
    }
    
    @Test
    public void assertPgSQL3() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/db_0");
        config.setUsername("postgres");
        config.setPassword("postgres");
        DataSource dataSource = new HikariDataSource(config);
        Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("select * from t_order where order_id = ?");
            preparedStatement.setInt(1, 4);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
//            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                System.out.println(resultSet.getInt(1) + ", " + resultSet.getInt(2) + ", " + resultSet.getString(3));
            }
            resultSet.close();
            preparedStatement.close();
    }
}
