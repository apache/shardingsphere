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

import org.apache.shardingsphere.example.common.entity.Sportsman;
import org.apache.shardingsphere.example.common.repository.SportsmanRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class SportsmanRepositoryImpl implements SportsmanRepository {

    private final DataSource dataSource;

    public SportsmanRepositoryImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS t_sportsman (id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(200), country_code VARCHAR(50), PRIMARY KEY (id))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public void dropTable() {
        String sql = "DROP TABLE t_sportsman";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public void truncateTable() {
        String sql = "TRUNCATE TABLE t_sportsman";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public Long insert(final Sportsman sportsman) {
        String sql = "INSERT INTO t_sportsman (name,country_code) VALUES ( ?, ?)";
        int result = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, sportsman.getName());
            preparedStatement.setString(2, sportsman.getCountryCode());
            result = preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    sportsman.setId(resultSet.getLong(1));
                }
            }
        } catch (final SQLException ignored) {
        }
        return (long) result;
    }

    @Override
    public void delete(final Long id) {
        String sql = "DELETE FROM t_sportsman WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }

    @Override
    public List<Sportsman> selectAll() {
        String sql = "SELECT s.id,s.name,s.country_code,c.name,c.language FROM t_sportsman s left join t_country c on s.country_code=c.code";
        return getSportsmen(sql);
    }

    private List<Sportsman> getSportsmen(final String sql) {
        List<Sportsman> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                SportsmanExtend sportsman = new SportsmanExtend();
                sportsman.setId(resultSet.getLong(1));
                sportsman.setName(resultSet.getString(2));
                sportsman.setCountryCode(resultSet.getString(3));
                sportsman.setCountryName(resultSet.getString(4));
                sportsman.setCountryLanguage(resultSet.getString(5));
                result.add(sportsman);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }

    private class SportsmanExtend extends Sportsman {

        private String countryName;

        private String countryLanguage;

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(final String countryName) {
            this.countryName = countryName;
        }

        public String getCountryLanguage() {
            return countryLanguage;
        }

        public void setCountryLanguage(final String countryLanguage) {
            this.countryLanguage = countryLanguage;
        }

        @Override
        public String toString() {
            return String.format("id: %s, name: %s, countryCode: %s, countryName: %s, countryLanguage: %s", getId(), getName(), getCountryCode(), countryName, countryLanguage);
        }
    }
}
