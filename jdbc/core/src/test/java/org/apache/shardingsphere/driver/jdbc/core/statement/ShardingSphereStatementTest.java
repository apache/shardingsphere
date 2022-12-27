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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.util.StatementTestUtil;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereStatementTest {
    
    private static ShardingSphereDataSource dataSource;
    
    private static final String CONFIG_FILE = "config/config-sharding.yaml";
    
    @BeforeClass
    public static void initShardingSphereDataSource() throws SQLException, IOException {
        dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSourceMap(), getFile());
    }
    
    private static Map<String, DataSource> getDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("jdbc_0", StatementTestUtil.createDataSourcesWithInitFile("jdbc_statement_0", "sql/jdbc_init.sql"));
        result.put("jdbc_1", StatementTestUtil.createDataSourcesWithInitFile("jdbc_statement_1", "sql/jdbc_init.sql"));
        result.put("single_jdbc", StatementTestUtil.createDataSourcesWithInitFile("single_jdbc_statement", "sql/single_jdbc_init.sql"));
        return result;
    }
    
    private static File getFile() {
        return new File(Objects.requireNonNull(ShardingSphereStatementTest.class.getClassLoader().getResource(CONFIG_FILE), String.format("File `%s` is not existed.", CONFIG_FILE)).getFile());
    }
    
    @Before
    public void initTable() {
        try {
            Connection connection = dataSource.getConnection();
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(ShardingSphereStatementTest.class.getClassLoader().getResourceAsStream("sql/jdbc_data.sql"))));
            connection.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @AfterClass
    public static void clear() throws Exception {
        dataSource.close();
    }
    
    @Test
    public void assertGetGeneratedKeys() throws SQLException {
        String sql = "INSERT INTO t_order_item(order_id, user_id, status) VALUES (%d, %d, '%s')";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(String.format(sql, 1, 1, "init")));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), Statement.NO_GENERATED_KEYS));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), Statement.RETURN_GENERATED_KEYS));
            ResultSet generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(3L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new int[]{1}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(4L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new String[]{"user_id"}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(5L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new int[]{2}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(6L));
            assertFalse(statement.execute(String.format(sql, 1, 1, "init"), new String[]{"status"}));
            generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getLong(1), is(7L));
        }
    }
    
    @Test
    public void assertAddGetGeneratedKeysForNoGeneratedValues() throws SQLException {
        String sql = "INSERT INTO t_product (product_name) VALUES ('%s')";
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(String.format(sql, "cup"), Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeysResultSet = statement.getGeneratedKeys();
            assertTrue(generatedKeysResultSet.next());
            assertThat(generatedKeysResultSet.getInt(1), is(1));
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.executeQuery(null);
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.executeQuery("");
        }
    }
    
    @Test
    public void assertExecuteGetResultSet() throws SQLException {
        String sql = "UPDATE t_order_item SET status = '%s' WHERE user_id = %d AND order_id = %d";
        try (Statement statement = dataSource.getConnection().createStatement()) {
            assertFalse(statement.execute(String.format(sql, "OK", 1, 1)));
            assertNull(statement.getResultSet());
        }
    }
    
    @Test
    public void assertExecuteUpdateGetResultSet() throws SQLException {
        String sql = "UPDATE t_order_item SET status = '%s' WHERE user_id = %d AND order_id = %d";
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.executeUpdate(String.format(sql, "OK", 1, 1));
            assertNull(statement.getResultSet());
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertColumnNotFoundException() throws SQLException {
        String sql = "UPDATE t_order_item SET error_column = '%s'";
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.executeUpdate(String.format(sql, "OK"));
        }
    }
    
    @Test
    public void assertShowDatabases() throws SQLException {
        String sql = "SHOW DATABASES";
        try (Statement statement = dataSource.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            assertTrue(resultSet.next());
            assertThat(resultSet.getString(1), is(DefaultDatabase.LOGIC_NAME));
        }
    }
}
