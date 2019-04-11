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

package org.apache.shardingsphere.example.common.jdbc.repository;

import org.apache.shardingsphere.example.common.entity.Country;
import org.apache.shardingsphere.example.common.repository.CountryRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public final class CountryRepositoryImpl implements CountryRepository {

    private final DataSource dataSource;

    public CountryRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS t_country (code VARCHAR(50), name VARCHAR(50), language VARCHAR(50), PRIMARY KEY (code))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public void dropTable() {
        String sql = "DROP TABLE t_country";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public void truncateTable() {
        String sql = "TRUNCATE TABLE t_country";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public Long insert(final Country country) {
        String sql = "INSERT INTO t_country (code, name, language) VALUES (?, ?, ?)";
        int result = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, country.getCode());
            preparedStatement.setString(2, country.getName());
            preparedStatement.setString(3, country.getLanguage());
            result = preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
        return (long) result;
    }

    @Override
    public void delete(final String code) {
        String sql = "DELETE FROM t_country WHERE code =?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, code);
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public List<Country> selectAll() {
        String sql = "SELECT * FROM t_country";
        return getCountries(sql);
    }

    private List<Country> getCountries(final String sql) {
        List<Country> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Country country = new Country();
                country.setCode(resultSet.getString(1));
                country.setName(resultSet.getString(2));
                country.setLanguage(resultSet.getString(3));
                result.add(country);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
}
