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

package org.apache.shardingsphere.test.natived.jdbc.commons.repository;

import org.apache.shardingsphere.test.natived.jdbc.commons.entity.Address;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public final class AddressRepository {
    
    private final DataSource dataSource;
    
    public AddressRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * create table t_address if not exists.
     *
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * create table t_address in MS SQL Server.
     * This also ignored the default schema of the `dbo`.
     *
     * @throws SQLException SQL exception
     */
    public void createTableInSQLServer() throws SQLException {
        String sql = "CREATE TABLE [t_address] (\n"
                + "    address_id bigint NOT NULL,\n"
                + "    address_name varchar(100) NOT NULL,\n"
                + "    PRIMARY KEY (address_id)\n"
                + ");";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * drop table t_address.
     *
     * @throws SQLException SQL exception
     */
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * truncate table t_address.
     *
     * @throws SQLException SQL exception
     */
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * insert something to table t_address.
     *
     * @param address address
     * @return addressId of the insert statement
     * @throws SQLException SQL exception
     */
    public Long insert(final Address address) throws SQLException {
        String sql = "INSERT INTO t_address (address_id, address_name) VALUES (?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, address.getAddressId());
            preparedStatement.setString(2, address.getAddressName());
            preparedStatement.executeUpdate();
        }
        return address.getAddressId();
    }
    
    /**
     * delete by id.
     *
     * @param id id
     * @throws SQLException SQL exception
     */
    public void delete(final Long id) throws SQLException {
        String sql = "DELETE FROM t_address WHERE address_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * select all.
     *
     * @return list of address
     * @throws SQLException SQL exception
     */
    public List<Address> selectAll() throws SQLException {
        String sql = "SELECT * FROM t_address";
        List<Address> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Address address = new Address();
                address.setAddressId(resultSet.getLong(1));
                address.setAddressName(resultSet.getString(2));
                result.add(address);
            }
        }
        return result;
    }
    
    /**
     * Assert rollback with transactions.
     * This is currently just a simple test against a non-existent table and does not involve the competition scenario of distributed transactions.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void assertRollbackWithTransactions() throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            connection.createStatement().executeUpdate("INSERT INTO t_address (address_id, address_name) VALUES (2024, 'address_test_2024')");
            connection.createStatement().executeUpdate("INSERT INTO t_table_does_not_exist (test_id_does_not_exist) VALUES (2024)");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
        try (
                Connection conn = dataSource.getConnection();
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM t_address WHERE address_id = 2024")) {
            assertThat(resultSet.next(), is(false));
        }
    }
}
