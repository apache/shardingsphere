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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShadowStatementTest {
    
    private static final String CLEAN_SQL = "DELETE FROM t_encrypt";
    
    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (2, 'cipher', 'plain')";
    
    private static final String INSERT_SHADOW_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (1, 'cipher', 'plain')";
    
    private static final String UPDATE_SQL = "UPDATE t_encrypt SET cipher_pwd ='cipher_pwd' WHERE id = 2";
    
    private static final String UPDATE_SHADOW_SQL = "UPDATE t_encrypt SET cipher_pwd ='cipher_pwd' WHERE id = 1";
    
    private static final String DELETE_SQL = "DELETE FROM t_encrypt WHERE id = 2";
    
    private static final String DELETE_SHADOW_SQL = "DELETE FROM t_encrypt WHERE id = 1";
    
    private static final String SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = 2";
    
    private static final String SELECT_SHADOW_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = 1";
    
    private static final String RESULT_SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt";
    
    private static ShardingSphereDataSource dataSource;
    
    private static DataSource actualDataSource0;
    
    private static DataSource actualDataSource1;
    
    private static final String CONFIG_FILE = "config/config-shadow.yaml";
    
    @BeforeClass
    public static void initShadowDataSource() throws SQLException, IOException {
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        actualDataSource0 = StatementTestUtil.createDataSourcesWithInitFile("shadow_jdbc_statement_0", "sql/jdbc_shadow_init.sql");
        actualDataSource1 = StatementTestUtil.createDataSourcesWithInitFile("shadow_jdbc_statement_1", "sql/jdbc_shadow_init.sql");
        dataSources.put("shadow_jdbc_0", actualDataSource0);
        dataSources.put("shadow_jdbc_1", actualDataSource1);
        dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSources, getFile());
    }
    
    private static File getFile() {
        return new File(Objects.requireNonNull(ShadowStatementTest.class.getClassLoader().getResource(CONFIG_FILE), String.format("File `%s` is not existed.", CONFIG_FILE)).getFile());
    }
    
    @AfterClass
    public static void close() throws Exception {
        dataSource.close();
    }
    
    @Test
    public void assertInsertNativeCase() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
        }
        assertResultSet(true, 0, "cipher");
        assertResultSet(false, 1, "cipher");
    }
    
    private void assertResultSet(final boolean isShadow, final int resultSetCount, final Object cipherPwd) throws SQLException {
        DataSource dataSource = isShadow ? actualDataSource1 : actualDataSource0;
        try (Statement statement = dataSource.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(RESULT_SELECT_SQL);
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is(cipherPwd));
                count += 1;
            }
            assertThat(count, is(resultSetCount));
        }
    }
    
    @Test
    public void assertInsertShadowCase() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SHADOW_SQL);
        }
        assertResultSet(true, 1, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertUpdateNativeCase() throws SQLException {
        int result;
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            result = statement.executeUpdate(UPDATE_SQL);
        }
        assertThat(result, is(1));
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 1, "cipher_pwd");
        
    }
    
    @Test
    public void assertUpdateShadowCase() throws SQLException {
        int result;
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SHADOW_SQL);
            result = statement.executeUpdate(UPDATE_SHADOW_SQL);
        }
        assertThat(result, is(1));
        assertResultSet(true, 1, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @Test
    public void assertDeleteNativeCase() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            statement.execute(INSERT_SHADOW_SQL);
            statement.execute(DELETE_SQL);
        }
        assertResultSet(true, 1, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertDeleteShadowCase() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            statement.execute(INSERT_SHADOW_SQL);
            statement.execute(DELETE_SHADOW_SQL);
        }
        assertResultSet(true, 0, "cipher");
        assertResultSet(false, 1, "cipher");
    }
    
    @Test
    public void assertSelectNativeCase() throws SQLException {
        ResultSet resultSet;
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            resultSet = statement.executeQuery(SELECT_SQL);
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is("cipher"));
                count += 1;
            }
            assertThat(count, is(1));
            statement.execute(DELETE_SQL);
        }
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertSelectShadowCase() throws SQLException {
        ResultSet resultSet;
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(INSERT_SHADOW_SQL);
            resultSet = statement.executeQuery(SELECT_SHADOW_SQL);
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is("cipher"));
                count += 1;
            }
            assertThat(count, is(1));
            statement.execute(DELETE_SHADOW_SQL);
        }
        assertResultSet(true, 0, "cipher");
    }
    
    @After
    public void clean() throws SQLException {
        try (Statement statement = actualDataSource0.getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
        try (Statement statement = actualDataSource1.getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
    }
}
