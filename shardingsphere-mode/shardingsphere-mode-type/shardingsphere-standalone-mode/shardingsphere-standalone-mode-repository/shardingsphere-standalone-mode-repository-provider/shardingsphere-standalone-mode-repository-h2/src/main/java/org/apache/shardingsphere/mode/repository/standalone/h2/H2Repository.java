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

package org.apache.shardingsphere.mode.repository.standalone.h2;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * H2 repository.
 */
@Slf4j
public final class H2Repository implements StandalonePersistRepository {
    
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:~/h2_repository";
    
    private static final String DEFAULT_USER = "sa";
    
    private static final String DEFAULT_PASSWORD = "";
    
    private static final String SEPARATOR = "/";
    
    private String jdbcUrl;
    
    private String user;
    
    private String password;
    
    private Connection connection;
    
    @Override
    public void setProps(final Properties props) {
        H2RepositoryProperties localRepositoryProperties = new H2RepositoryProperties(props);
        jdbcUrl = Optional.ofNullable(
                Strings.emptyToNull(localRepositoryProperties.getValue(H2RepositoryPropertyKey.JDBC_URL))).orElse(DEFAULT_JDBC_URL);
        user = Optional.ofNullable(
                Strings.emptyToNull(localRepositoryProperties.getValue(H2RepositoryPropertyKey.USER))).orElse(DEFAULT_USER);
        password = Optional.ofNullable(
                Strings.emptyToNull(localRepositoryProperties.getValue(H2RepositoryPropertyKey.PASSWORD))).orElse(DEFAULT_PASSWORD);
        init();
    }
    
    @SneakyThrows
    private void init() {
        connection = DriverManager.getConnection(jdbcUrl, user, password);
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS REPOSITORY");
            statement.execute("CREATE TABLE REPOSITORY(id varchar(36) PRIMARY KEY, key TEXT, value TEXT, parent TEXT)");
        }
    }
    
    @Override
    public String get(final String key) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT value FROM REPOSITORY WHERE key = '" + key + "'");
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.ofNullable(
                        Strings.emptyToNull(resultSet.getString("value"))).map(each -> each.replace("\"", "'")).orElse("");
            }
        } catch (final SQLException ex) {
            log.error("Get h2 data by key: {} failed", key, ex);
        }
        return "";
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT key FROM REPOSITORY WHERE parent = '" + key + "'");
             ResultSet resultSet = statement.executeQuery()) {
            List<String> resultChildrenList = new ArrayList<>(10);
            while (resultSet.next()) {
                String childrenKey = resultSet.getString("key");
                if (Strings.isNullOrEmpty(childrenKey)) {
                    continue;
                }
                int lastIndexOf = childrenKey.lastIndexOf(SEPARATOR);
                resultChildrenList.add(childrenKey.substring(lastIndexOf + 1));
            }
            return resultChildrenList;
        } catch (final SQLException ex) {
            log.error("Get children h2 data by key: {} failed", key, ex);
        }
        return Collections.emptyList();
    }
    
    @Override
    public void persist(final String key, final String value) {
        // Single quotation marks are the keywords executed by H2. Replace with double quotation marks.
        String insensitiveValue = value.replace("'", "\"");
        String[] paths = Lists.newArrayList(key.split(SEPARATOR))
                .stream().filter(each -> StringUtils.isNotBlank(each)).toArray(String[]::new);
        String tempPrefix = "";
        String parent = SEPARATOR;
        try {
            // Create key level directory recursively.
            for (int i = 0; i < paths.length - 1; i++) {
                String tempKey = tempPrefix + SEPARATOR + paths[i];
                String tempKeyVal = get(tempKey);
                if (StringUtils.isBlank(tempKeyVal)) {
                    if (i != 0) {
                        parent = tempPrefix;
                    }
                    insert(tempKey, "", parent);
                }
                tempPrefix = tempKey;
                parent = tempKey;
            }
            String keyValue = get(key);
            if (StringUtils.isBlank(keyValue)) {
                insert(key, insensitiveValue, parent);
            } else {
                update(key, insensitiveValue);
            }
        } catch (final SQLException ex) {
            log.error("Persist h2 data to key: {} failed", key, ex);
        }
    }
    
    private void insert(final String key, final String value, final String parent) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO REPOSITORY VALUES('" + UUID.randomUUID() + "','" + key + "','" + value + "','" + parent + "')")) {
            statement.executeUpdate();
        }
    }
    
    private void update(final String key, final String value) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("UPDATE REPOSITORY SET value = '" + value + "' WHERE key = '" + key + "'")) {
            statement.executeUpdate();
        }
    }
    
    @Override
    public void delete(final String key) {
        try (PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM REPOSITORY WHERE key = '" + key + "'")) {
            statement.executeUpdate();
        } catch (final SQLException ex) {
            log.error("Delete h2 data by key: {} failed", key, ex);
        }
    }
    
    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final SQLException ex) {
            log.error("Failed to release h2 database resources.", ex);
        }
    }
    
    @Override
    public String getType() {
        return "H2";
    }
}
