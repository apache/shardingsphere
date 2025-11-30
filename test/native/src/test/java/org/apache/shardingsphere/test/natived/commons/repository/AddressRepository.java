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

package org.apache.shardingsphere.test.natived.commons.repository;

import org.apache.shardingsphere.test.natived.commons.entity.Address;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public final class AddressRepository {
    
    private final DataSource dataSource;
    
    public AddressRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Create table t_address if not exists in MySQL.
     *
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInMySQL() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * Create table t_address in MS SQL Server.
     * This also ignored the default schema of the `dbo`.
     *
     * @throws SQLException SQL exception
     */
    public void createTableInSQLServer() throws SQLException {
        String sql = "CREATE TABLE [t_address] (address_id bigint NOT NULL,address_name varchar(100) NOT NULL,PRIMARY KEY (address_id))";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * Create table t_address in Firebird.
     * Cannot use `create table if not exists` for Docker Image `firebirdsql/firebird`,
     * see <a href="https://github.com/FirebirdSQL/firebird/issues/8062">FirebirdSQL/firebird#8062</a>.
     *
     * @throws SQLException SQL exception
     */
    public void createTableInFirebird() throws SQLException {
        String sql = "CREATE TABLE t_address (address_id BIGINT NOT NULL PRIMARY KEY, address_name VARCHAR(100) NOT NULL)";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * Create ACID table in HiveServer2.
     *
     * @throws SQLException SQL exception
     */
    public void createAcidTableInHiveServer2() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL,address_name VARCHAR(100) NOT NULL,"
                + "PRIMARY KEY (address_id) disable novalidate) CLUSTERED BY (address_id) INTO 2 BUCKETS STORED AS ORC TBLPROPERTIES ('transactional' = 'true')";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * Drop table t_address in MySQL.
     *
     * @throws SQLException SQL exception
     */
    public void dropTableInMySQL() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * Drop table in Firebird.
     * Docker Image `firebirdsql/firebird` does not work with `DROP TABLE IF EXISTS`.
     * See <a href="https://github.com/FirebirdSQL/firebird/issues/4203">FirebirdSQL/firebird#4203</a> .
     *
     * @throws SQLException SQL exception
     */
    public void dropTableInFirebird() throws SQLException {
        String sql = "DROP TABLE t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
    
    /**
     * Truncate table t_address.
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
     * Insert something to table t_address.
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
     * Delete by id.
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
     * Delete by id in ClickHouse.
     *
     * @param id id
     * @throws SQLException SQL exception
     */
    public void deleteInClickHouse(final Long id) throws SQLException {
        String sql = "ALTER TABLE t_address delete WHERE address_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Select all.
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
}
