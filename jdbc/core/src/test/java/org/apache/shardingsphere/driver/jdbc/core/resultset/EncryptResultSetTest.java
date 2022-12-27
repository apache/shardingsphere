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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.util.StatementTestUtil;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
public final class EncryptResultSetTest {
    
    private static final String SELECT_SQL_TO_ASSERT = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt";
    
    private static final String CONFIG_FILE_WITH_QUERY_WITH_CIPHER = "config/config-encrypt-query-with-cipher.yaml";
    
    private static ShardingSphereDataSource queryWithPlainDataSource;
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException, IOException {
        DataSource actualDataSource = StatementTestUtil.createDataSourcesWithInitFile("encrypt_result_set_test", "sql/jdbc_encrypt_init.sql");
        queryWithPlainDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(actualDataSource, getFile());
    }
    
    private static File getFile() {
        return new File(Objects.requireNonNull(EncryptResultSetTest.class.getClassLoader().getResource(CONFIG_FILE_WITH_QUERY_WITH_CIPHER), String.format("File `%s` is not existed.",
                CONFIG_FILE_WITH_QUERY_WITH_CIPHER)).getFile());
    }
    
    @AfterClass
    public static void close() throws Exception {
        queryWithPlainDataSource.close();
    }
    
    @Before
    public void initTable() {
        try (Connection connection = queryWithPlainDataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(EncryptResultSetTest.class.getClassLoader().getResourceAsStream("sql/encrypt_data.sql"))));
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Test
    public void assertResultSetIsBeforeFirst() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            assertTrue(resultSet.isBeforeFirst());
            resultSet.next();
            assertThat(resultSet.getRow(), is(1));
            resultSet.afterLast();
            assertTrue(resultSet.isAfterLast());
            resultSet.beforeFirst();
            assertTrue(resultSet.isBeforeFirst());
        }
    }
    
    private Connection getEncryptConnection() {
        return queryWithPlainDataSource.getConnection();
    }
    
    @Test
    public void assertResultSetGetRow() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.next();
            assertThat(resultSet.getRow(), is(1));
        }
    }
    
    @Test
    public void assertResultSetAfterLast() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.afterLast();
            assertTrue(resultSet.isAfterLast());
        }
    }
    
    @Test
    public void assertResultSetBeforeFirst() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            assertTrue(resultSet.isBeforeFirst());
        }
    }
    
    @Test
    public void assertResultSetPrevious() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.next();
            assertThat(resultSet.getRow(), is(1));
            resultSet.previous();
            assertThat(resultSet.getRow(), is(0));
        }
    }
    
    @Test
    public void assertRelative() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.next();
            resultSet.relative(1);
            assertThat(resultSet.getRow(), is(2));
        }
    }
    
    @Test
    public void assertAbsolute() throws SQLException {
        try (
                Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT)) {
            resultSet.absolute(2);
            assertThat(resultSet.getRow(), is(2));
        }
    }
}
