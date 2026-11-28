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

package org.apache.shardingsphere.test.e2e.driver.statement;

import org.apache.shardingsphere.test.e2e.driver.AbstractShadowDriverTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShadowPreparedStatementTest extends AbstractShadowDriverTest {
    
    private static final String CLEAN_SQL = "DELETE FROM t_encrypt";
    
    private static final String RESULT_SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt";
    
    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (?, ?, ?)";
    
    private static final String UPDATE_SQL = "UPDATE t_encrypt SET cipher_pwd =? WHERE id = ?";
    
    private static final String DELETE_SQL = "DELETE FROM t_encrypt WHERE id = ?";
    
    private static final String SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = ?";
    
    private static final String MIXED_BATCH_INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)";
    
    private static final String MIXED_BATCH_INSERT_WITH_SKIP_REWRITE_SQL = "/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */ " + MIXED_BATCH_INSERT_SQL;
    
    private static final String TWO_ROWS_BATCH_INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (?, ?, ?), (?, ?, ?)";
    
    @AfterEach
    void clean() throws SQLException {
        try (Statement statement = getActualDataSources().get("shadow_jdbc_0").getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
        try (Statement statement = getActualDataSources().get("shadow_jdbc_1").getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
    }
    
    @Test
    void assertInsertNativeCase() throws SQLException {
        try (PreparedStatement preparedStatement = getShadowDataSource().getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setObject(1, 2);
            preparedStatement.setString(2, "cipher");
            preparedStatement.setString(3, "plain");
            preparedStatement.execute();
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
    void assertInsertShadowCase() throws SQLException {
        try (PreparedStatement preparedStatement = getShadowDataSource().getConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setObject(1, 1);
            preparedStatement.setString(2, "cipher");
            preparedStatement.setString(3, "plain");
            preparedStatement.execute();
        }
        assertResultSet(true, 1, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    void assertInsertMixedBatchCase() throws SQLException {
        try (PreparedStatement preparedStatement = getShadowDataSource().getConnection().prepareStatement(MIXED_BATCH_INSERT_SQL)) {
            setInsertParameters(preparedStatement, 1, 2, 3);
            assertThat(preparedStatement.executeUpdate(), is(3));
        }
        assertInsertedIds("shadow_jdbc_1", 1);
        assertInsertedIds("shadow_jdbc_0", 2, 3);
    }
    
    @Test
    void assertInsertMixedBatchUpdateCountCase() throws SQLException {
        int actual;
        try (PreparedStatement preparedStatement = getShadowDataSource().getConnection().prepareStatement(MIXED_BATCH_INSERT_SQL)) {
            setInsertParameters(preparedStatement, 1, 2, 3);
            assertFalse(preparedStatement.execute());
            actual = preparedStatement.getUpdateCount();
        }
        assertThat(actual, is(3));
        assertInsertedIds("shadow_jdbc_1", 1);
        assertInsertedIds("shadow_jdbc_0", 2, 3);
    }
    
    @Test
    void assertInsertMixedBatchWhenSQLRewriteSkippedCase() throws SQLException {
        try (PreparedStatement preparedStatement = getShadowDataSource().getConnection().prepareStatement(MIXED_BATCH_INSERT_WITH_SKIP_REWRITE_SQL)) {
            setInsertParameters(preparedStatement, 1, 2, 3);
            assertThat(preparedStatement.executeUpdate(), is(3));
        }
        assertInsertedIds("shadow_jdbc_1");
        assertInsertedIds("shadow_jdbc_0", 1, 2, 3);
    }
    
    @Test
    void assertInsertMixedBatchWithExecuteBatchCase() throws SQLException {
        int[] actual;
        try (PreparedStatement preparedStatement = getShadowDataSource().getConnection().prepareStatement(TWO_ROWS_BATCH_INSERT_SQL)) {
            setInsertParameters(preparedStatement, 1, 2);
            preparedStatement.addBatch();
            setInsertParameters(preparedStatement, 3, 4);
            preparedStatement.addBatch();
            actual = preparedStatement.executeBatch();
        }
        assertThat(actual.length, is(2));
        assertThat(actual[0], is(2));
        assertThat(actual[1], is(2));
        assertInsertedIds("shadow_jdbc_1", 1);
        assertInsertedIds("shadow_jdbc_0", 2, 3, 4);
    }
    
    private void setInsertParameters(final PreparedStatement preparedStatement, final int firstId, final int secondId) throws SQLException {
        preparedStatement.setObject(1, firstId);
        preparedStatement.setString(2, "cipher");
        preparedStatement.setString(3, "plain");
        preparedStatement.setObject(4, secondId);
        preparedStatement.setString(5, "cipher");
        preparedStatement.setString(6, "plain");
    }
    
    private void setInsertParameters(final PreparedStatement preparedStatement, final int firstId, final int secondId, final int thirdId) throws SQLException {
        setInsertParameters(preparedStatement, firstId, secondId);
        preparedStatement.setObject(7, thirdId);
        preparedStatement.setString(8, "cipher");
        preparedStatement.setString(9, "plain");
    }
    
    @Test
    void assertUpdateNativeCase() throws SQLException {
        int actual;
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement.setObject(1, 2);
            insertPreparedStatement.setString(2, "cipher");
            insertPreparedStatement.setString(3, "plain");
            insertPreparedStatement.execute();
            PreparedStatement updatePreparedStatement = connection.prepareStatement(UPDATE_SQL);
            updatePreparedStatement.setString(1, "cipher_pwd");
            updatePreparedStatement.setObject(2, 2);
            actual = updatePreparedStatement.executeUpdate();
        }
        assertThat(actual, is(1));
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 1, "cipher_pwd");
    }
    
    @Test
    void assertUpdateShadowCase() throws SQLException {
        int actual;
        try (Connection connection = getShadowDataSource().getConnection()) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement(INSERT_SQL);
            insertPreparedStatement.setObject(1, 1);
            insertPreparedStatement.setString(2, "cipher");
            insertPreparedStatement.setString(3, "plain");
            insertPreparedStatement.execute();
            PreparedStatement updatePreparedStatement = connection.prepareStatement(UPDATE_SQL);
            updatePreparedStatement.setString(1, "cipher_pwd");
            updatePreparedStatement.setObject(2, 1);
            actual = updatePreparedStatement.executeUpdate();
        }
        assertThat(actual, is(1));
        assertResultSet(true, 1, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @Test
    void assertDeleteNativeCase() throws SQLException {
        int actual;
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
            actual = deletePreparedStatement.executeUpdate();
        }
        assertThat(actual, is(1));
        assertResultSet(true, 1, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @Test
    void assertDeleteShadowCase() throws SQLException {
        int actual;
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
            actual = deletePreparedStatement.executeUpdate();
        }
        assertThat(actual, is(1));
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 1, "cipher");
    }
    
    @Test
    void assertSelectNativeCase() throws SQLException {
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
    void assertSelectShadowCase() throws SQLException {
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
}
