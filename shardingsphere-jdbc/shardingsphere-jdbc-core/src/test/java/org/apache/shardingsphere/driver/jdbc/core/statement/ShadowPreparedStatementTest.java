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

import org.apache.shardingsphere.driver.jdbc.base.AbstractShardingSphereDataSourceForShadowTest;
import org.junit.After;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShadowPreparedStatementTest extends AbstractShardingSphereDataSourceForShadowTest {
    
    private static final String CLEAN_SQL = "DELETE FROM t_encrypt";
    
    private static final String RESULT_SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt";
    
    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (?, ?, ?)";
    
    private static final String UPDATE_SQL = "UPDATE t_encrypt SET cipher_pwd =? WHERE id = ?";
    
    private static final String DELETE_SQL = "DELETE FROM t_encrypt WHERE id = ?";
    
    private static final String SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = ?";
    
    @Test
    public void assertInsertNativeCase() throws SQLException {
        try (PreparedStatement statement = getShadowDataSource().getConnection().prepareStatement(INSERT_SQL)) {
            statement.setObject(1, 2);
            statement.setString(2, "cipher");
            statement.setString(3, "plain");
            statement.execute();
        }
        assertResultSet(true, 0, "cipher");
        assertResultSet(false, 1, "cipher");
    }
    
    private void assertResultSet(final boolean isShadow, final int resultSetCount, final Object cipherPwd) throws SQLException {
        DataSource dataSource = isShadow ? getActualDataSources().get("shadow_jdbc_1") : getActualDataSources().get("shadow_jdbc_0");
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
        try (PreparedStatement statement = getShadowDataSource().getConnection().prepareStatement(INSERT_SQL)) {
            statement.setObject(1, 1);
            statement.setString(2, "cipher");
            statement.setString(3, "plain");
            statement.execute();
        }
        assertResultSet(true, 1, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertUpdateNativeCase() throws SQLException {
        int result;
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement.setObject(1, 2);
            insertPreparedStatement.setString(2, "cipher");
            insertPreparedStatement.setString(3, "plain");
            insertPreparedStatement.execute();
            PreparedStatement updatePreparedStatement = connection.prepareStatement(UPDATE_SQL);
            updatePreparedStatement.setString(1, "cipher_pwd");
            updatePreparedStatement.setObject(2, 2);
            result = updatePreparedStatement.executeUpdate();
        }
        assertThat(result, is(1));
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 1, "cipher_pwd");
    }
    
    @Test
    public void assertUpdateShadowCase() throws SQLException {
        int result;
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement.setObject(1, 1);
            insertPreparedStatement.setString(2, "cipher");
            insertPreparedStatement.setString(3, "plain");
            insertPreparedStatement.execute();
            PreparedStatement updatePreparedStatement = connection.prepareStatement(UPDATE_SQL);
            updatePreparedStatement.setString(1, "cipher_pwd");
            updatePreparedStatement.setObject(2, 1);
            result = updatePreparedStatement.executeUpdate();
        }
        assertThat(result, is(1));
        assertResultSet(true, 1, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @Test
    public void assertDeleteNativeCase() throws SQLException {
        int result;
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement1 = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement1.setObject(1, 2);
            insertPreparedStatement1.setString(2, "cipher");
            insertPreparedStatement1.setString(3, "plain");
            insertPreparedStatement1.execute();
            PreparedStatement insertPreparedStatement2 = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement2.setObject(1, 1);
            insertPreparedStatement2.setString(2, "cipher_pwd");
            insertPreparedStatement2.setString(3, "plain");
            insertPreparedStatement2.execute();
            PreparedStatement deletePreparedStatement = connection.prepareStatement(DELETE_SQL);
            deletePreparedStatement.setObject(1, 2);
            result = deletePreparedStatement.executeUpdate();
        }
        assertThat(result, is(1));
        assertResultSet(true, 1, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @Test
    public void assertDeleteShadowCase() throws SQLException {
        int result;
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement1 = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement1.setObject(1, 1);
            insertPreparedStatement1.setString(2, "cipher_pwd");
            insertPreparedStatement1.setString(3, "plain");
            insertPreparedStatement1.execute();
            PreparedStatement insertPreparedStatement2 = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement2.setObject(1, 2);
            insertPreparedStatement2.setString(2, "cipher");
            insertPreparedStatement2.setString(3, "plain");
            insertPreparedStatement2.execute();
            PreparedStatement deletePreparedStatement = connection.prepareStatement(DELETE_SQL);
            deletePreparedStatement.setObject(1, 1);
            result = deletePreparedStatement.executeUpdate();
        }
        assertThat(result, is(1));
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 1, "cipher");
    }
    
    @Test
    public void assertSelectNativeCase() throws SQLException {
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement.setObject(1, 2);
            insertPreparedStatement.setString(2, "cipher");
            insertPreparedStatement.setString(3, "plain");
            insertPreparedStatement.execute();
            PreparedStatement selectPreparedStatement = connection.prepareStatement(SELECT_SQL);
            selectPreparedStatement.setObject(1, 2);
            ResultSet resultSet = selectPreparedStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is("cipher"));
                count += 1;
            }
            assertThat(count, is(1));
            PreparedStatement deletePreparedStatement = connection.prepareStatement(DELETE_SQL);
            deletePreparedStatement.setObject(1, 2);
            deletePreparedStatement.executeUpdate();
        }
        assertResultSet(true, 0, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertSelectShadowCase() throws SQLException {
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement.setObject(1, 1);
            insertPreparedStatement.setString(2, "cipher_pwd");
            insertPreparedStatement.setString(3, "plain");
            insertPreparedStatement.execute();
            PreparedStatement selectPreparedStatement = connection.prepareStatement(SELECT_SQL);
            selectPreparedStatement.setObject(1, 1);
            ResultSet resultSet = selectPreparedStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is("cipher_pwd"));
                count += 1;
            }
            assertThat(count, is(1));
            PreparedStatement deletePreparedStatement = connection.prepareStatement(DELETE_SQL);
            deletePreparedStatement.setObject(1, 1);
            deletePreparedStatement.executeUpdate();
        }
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @After
    public void clean() throws SQLException {
        try (Statement statement = getActualDataSources().get("shadow_jdbc_0").getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
        try (Statement statement = getActualDataSources().get("shadow_jdbc_1").getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
    }
}
