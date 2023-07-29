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

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.test.e2e.driver.AbstractEncryptDriverTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptPreparedStatementTest extends AbstractEncryptDriverTest {
    
    private static final String INSERT_SQL = "INSERT INTO t_query_encrypt(id, pwd) VALUES(?,?)";
    
    private static final String INSERT_GENERATED_KEY_SQL = "INSERT INTO t_query_encrypt(pwd) VALUES('b')";
    
    private static final String DELETE_SQL = "DELETE FROM t_query_encrypt WHERE pwd = ? AND id = ?";
    
    private static final String UPDATE_SQL = "UPDATE t_query_encrypt SET pwd =? WHERE pwd = ?";
    
    private static final String SELECT_SQL = "SELECT * FROM t_query_encrypt WHERE pwd = ?";
    
    private static final String SELECT_SQL_OR = "SELECT * FROM t_query_encrypt WHERE pwd = ? AND (id = ? OR id =?)";
    
    private static final String SELECT_ALL_LOGICAL_SQL = "SELECT id, pwd FROM t_query_encrypt";
    
    private static final String SELECT_ALL_ACTUAL_SQL = "SELECT id, cipher_pwd, assist_pwd FROM t_query_encrypt";
    
    private static final String SELECT_SQL_WITH_IN_OPERATOR = "SELECT * FROM t_query_encrypt WHERE pwd IN (?)";
    
    @Test
    void assertSQLShow() {
        assertTrue(getEncryptConnectionWithProps().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
    }
    
    @Test
    void assertInsertWithExecute() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setObject(1, 2);
            preparedStatement.setObject(2, 'b');
            preparedStatement.execute();
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    void assertInsertWithBatchExecute() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(INSERT_SQL)) {
            preparedStatement.setObject(1, 3);
            preparedStatement.setObject(2, 'c');
            preparedStatement.addBatch();
            preparedStatement.setObject(1, 4);
            preparedStatement.setObject(2, 'd');
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
        }
        assertResultSet(4, 3, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    void assertInsertWithExecuteWithGeneratedKey() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(INSERT_GENERATED_KEY_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(6));
            assertFalse(resultSet.next());
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    void assertInsertWithBatchExecuteWithGeneratedKeys() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(INSERT_GENERATED_KEY_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, 'b');
            preparedStatement.addBatch();
            preparedStatement.setObject(1, 'c');
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    void assertDeleteWithExecute() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(DELETE_SQL)) {
            preparedStatement.setObject(1, 'a');
            preparedStatement.setObject(2, 1);
            preparedStatement.execute();
        }
        assertResultSet(1, 5, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(UPDATE_SQL)) {
            preparedStatement.setObject(1, 'f');
            preparedStatement.setObject(2, 'a');
            result = preparedStatement.executeUpdate();
        }
        assertThat(result, is(2));
        assertResultSet(2, 1, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    void assertSelectWithExecuteQuery() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(SELECT_SQL)) {
            preparedStatement.setObject(1, 'a');
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertThat(resultSet.getString(2), is("decryptValue"));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
            assertThat(resultSet.getString(2), is("decryptValue"));
        }
    }
    
    @Test
    void assertSelectWithOr() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(SELECT_SQL_OR)) {
            preparedStatement.setObject(1, "plainValue");
            preparedStatement.setObject(2, 1);
            preparedStatement.setObject(3, 5);
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
        }
    }
    
    @Test
    void assertSelectWithMetaData() throws SQLException {
        try (PreparedStatement prepareStatement = getEncryptConnection().prepareStatement(SELECT_SQL)) {
            prepareStatement.setObject(1, 'a');
            ResultSetMetaData metaData = prepareStatement.executeQuery().getMetaData();
            assertThat(metaData.getColumnCount(), is(2));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("pwd"));
            }
        }
    }
    
    @Test
    void assertSelectWithExecuteWithProperties() throws SQLException {
        try (
                PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(
                        SELECT_ALL_LOGICAL_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            boolean result = preparedStatement.execute();
            assertTrue(result);
            assertThat(preparedStatement.getResultSetType(), is(ResultSet.TYPE_FORWARD_ONLY));
            assertThat(preparedStatement.getResultSetConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
            assertThat(preparedStatement.getResultSetHoldability(), is(ResultSet.HOLD_CURSORS_OVER_COMMIT));
        }
    }
    
    private void assertResultSet(final int resultSetCount, final int id, final Object pwd, final Object assistPwd) throws SQLException {
        try (
                Connection connection = getActualDataSources().get("encrypt").getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_ALL_ACTUAL_SQL);
            int count = 1;
            while (resultSet.next()) {
                if (id == count) {
                    assertThat(pwd, is(resultSet.getObject("cipher_pwd")));
                    assertThat(assistPwd, is(resultSet.getObject("assist_pwd")));
                }
                count += 1;
            }
            assertThat(count - 1, is(resultSetCount));
        }
    }
    
    @Test
    void assertQueryWithNull() {
        assertThrows(SQLException.class, () -> getEncryptConnection().prepareStatement(null));
    }
    
    @Test
    void assertQueryWithEmptyString() {
        assertThrows(SQLException.class, () -> getEncryptConnection().prepareStatement(""));
    }
    
    @Test
    void assertSelectWithInOperator() throws SQLException {
        try (PreparedStatement preparedStatement = getEncryptConnection().prepareStatement(SELECT_SQL_WITH_IN_OPERATOR)) {
            preparedStatement.setObject(1, 'a');
            ResultSetMetaData metaData = preparedStatement.executeQuery().getMetaData();
            assertThat(metaData.getColumnCount(), is(2));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("pwd"));
            }
        }
    }
}
