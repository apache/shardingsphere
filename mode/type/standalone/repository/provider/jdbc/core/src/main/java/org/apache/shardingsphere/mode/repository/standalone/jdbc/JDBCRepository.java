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

package org.apache.shardingsphere.mode.repository.standalone.jdbc;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.props.JDBCRepositoryProperties;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.props.JDBCRepositoryPropertyKey;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.provider.JDBCRepositoryProvider;
import org.apache.shardingsphere.mode.repository.standalone.jdbc.provider.JDBCRepositoryProviderFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * JDBC repository.
 */
@Slf4j
public final class JDBCRepository implements StandalonePersistRepository {
    
    private static final String SEPARATOR = "/";
    
    private static final String H2_FILE_MODE_KEY = "file:";
    
    private JDBCRepositoryProvider provider;
    
    private HikariDataSource hikariDataSource;
    
    @SneakyThrows(SQLException.class)
    @Override
    public void init(final Properties props) {
        JDBCRepositoryProperties jdbcRepositoryProps = new JDBCRepositoryProperties(props);
        hikariDataSource = new HikariDataSource();
        String jdbcUrl = jdbcRepositoryProps.getValue(JDBCRepositoryPropertyKey.JDBC_URL);
        hikariDataSource.setJdbcUrl(jdbcUrl);
        hikariDataSource.setUsername(jdbcRepositoryProps.getValue(JDBCRepositoryPropertyKey.USERNAME));
        hikariDataSource.setPassword(jdbcRepositoryProps.getValue(JDBCRepositoryPropertyKey.PASSWORD));
        try (
                Connection connection = hikariDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            provider = JDBCRepositoryProviderFactory.getInstance(jdbcRepositoryProps.getValue(JDBCRepositoryPropertyKey.PROVIDER));
            if (!jdbcUrl.contains(H2_FILE_MODE_KEY)) {
                statement.execute(provider.dropTableSQL());
            }
            statement.execute(provider.createTableSQL());
        }
    }
    
    @Override
    public String get(final String key) {
        // TODO
        return null;
    }
    
    @Override
    public String getDirectly(final String key) {
        try (
                Connection connection = hikariDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(provider.selectByKeySQL())) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("value");
                }
            }
        } catch (final SQLException ex) {
            log.error("Get {} data by key: {} failed", getType(), key, ex);
        }
        return "";
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try (
                Connection connection = hikariDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(provider.selectByParentKeySQL())) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> resultChildren = new LinkedList<>();
                while (resultSet.next()) {
                    String childrenKey = resultSet.getString("key");
                    if (Strings.isNullOrEmpty(childrenKey)) {
                        continue;
                    }
                    int lastIndexOf = childrenKey.lastIndexOf(SEPARATOR);
                    resultChildren.add(childrenKey.substring(lastIndexOf + 1));
                }
                return new ArrayList<>(resultChildren);
            }
        } catch (final SQLException ex) {
            log.error("Get children {} data by key: {} failed", getType(), key, ex);
        }
        return Collections.emptyList();
    }
    
    @Override
    public boolean isExisted(final String key) {
        return !Strings.isNullOrEmpty(getDirectly(key));
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            if (isExisted(key)) {
                update(key, value);
                return;
            }
            String tempPrefix = "";
            String parent = SEPARATOR;
            String[] paths = Arrays.stream(key.split(SEPARATOR)).filter(each -> !Strings.isNullOrEmpty(each)).toArray(String[]::new);
            // Create key level directory recursively.
            for (int i = 0; i < paths.length - 1; i++) {
                String tempKey = tempPrefix + SEPARATOR + paths[i];
                String tempKeyVal = getDirectly(tempKey);
                if (Strings.isNullOrEmpty(tempKeyVal)) {
                    if (i != 0) {
                        parent = tempPrefix;
                    }
                    insert(tempKey, "", parent);
                }
                tempPrefix = tempKey;
                parent = tempKey;
            }
            insert(key, value, parent);
        } catch (final SQLException ex) {
            log.error("Persist {} data to key: {} failed", getType(), key, ex);
        }
    }
    
    private void insert(final String key, final String value, final String parent) throws SQLException {
        try (
                Connection connection = hikariDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(provider.insertSQL())) {
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, key);
            preparedStatement.setString(3, value);
            preparedStatement.setString(4, parent);
            preparedStatement.executeUpdate();
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        try (
                Connection connection = hikariDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(provider.updateSQL())) {
            preparedStatement.setString(1, value);
            preparedStatement.setString(2, key);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            log.error("Update {} data to key: {} failed", getType(), key, ex);
        }
    }
    
    @Override
    public void delete(final String key) {
        try (
                Connection connection = hikariDataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(provider.deleteSQL())) {
            preparedStatement.setString(1, key);
            preparedStatement.executeUpdate();
        } catch (final SQLException ex) {
            log.error(String.format("Delete %s data by key: {} failed", getType()), key, ex);
        }
    }
    
    @Override
    public void close() {
        hikariDataSource.close();
    }
    
    @Override
    public String getType() {
        return "JDBC";
    }
}
