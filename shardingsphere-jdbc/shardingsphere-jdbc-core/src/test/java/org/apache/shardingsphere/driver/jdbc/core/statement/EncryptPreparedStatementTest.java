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

import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForEncryptTest;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptPreparedStatementTest extends AbstractShardingSphereDataSourceForEncryptTest {
    
    private static final String INSERT_SQL = "INSERT INTO t_query_encrypt(id, pwd) VALUES(?,?)";
    
    private static final String INSERT_GENERATED_KEY_SQL = "INSERT INTO t_query_encrypt(pwd) VALUES('b')";
    
    private static final String DELETE_SQL = "DELETE FROM t_query_encrypt WHERE pwd = ? AND id = ?";
    
    private static final String UPDATE_SQL = "UPDATE t_query_encrypt SET pwd =? WHERE pwd = ?";
    
    private static final String SELECT_SQL = "SELECT * FROM t_query_encrypt WHERE pwd = ?";
 
    private static final String SELECT_SQL_OR = "SELECT * FROM t_query_encrypt WHERE pwd = ? AND (id = ? OR id =?)";
    
    private static final String SELECT_ALL_SQL = "SELECT id, cipher_pwd, assist_pwd FROM t_query_encrypt";
    
    private static final String SELECT_SQL_WITH_IN_OPERATOR = "SELECT * FROM t_query_encrypt WHERE pwd IN (?)";
    
    private static final String SELECT_SQL_FOR_CONTAINS_COLUMN = "SELECT * FROM t_encrypt_contains_column WHERE plain_pwd = ?";
    
    @Test
    public void assertSQLShow() {
        assertTrue(getEncryptConnectionWithProps().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
    }
    
    @Test
    public void assertInsertWithExecute() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(INSERT_SQL)) {
            statement.setObject(1, 2);
            statement.setObject(2, 'b');
            statement.execute();
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertInsertWithBatchExecute() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(INSERT_SQL)) {
            statement.setObject(1, 3);
            statement.setObject(2, 'c');
            statement.addBatch();
            statement.setObject(1, 4);
            statement.setObject(2, 'd');
            statement.addBatch();
            statement.executeBatch();
        }
        assertResultSet(4, 3, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertInsertWithExecuteWithGeneratedKey() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(INSERT_GENERATED_KEY_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(6));
            assertFalse(resultSet.next());
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertDeleteWithExecute() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(DELETE_SQL)) {
            statement.setObject(1, 'a');
            statement.setObject(2, 1);
            statement.execute();
        }
        assertResultSet(1, 5, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(UPDATE_SQL)) {
            statement.setObject(1, 'f');
            statement.setObject(2, 'a');
            result = statement.executeUpdate();
        }
        assertThat(result, is(2));
        assertResultSet(2, 1, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertSelectWithExecuteQuery() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(SELECT_SQL)) {
            statement.setObject(1, 'a');
            ResultSet resultSet = statement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertThat(resultSet.getString(2), is("decryptValue"));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
            assertThat(resultSet.getString(2), is("decryptValue"));
        }
    }
    
    @Test
    public void assertSelectWithOr() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(SELECT_SQL_OR)) {
            statement.setObject(1, "plainValue");
            statement.setObject(2, 1);
            statement.setObject(3, 5);
            ResultSet resultSet = statement.executeQuery();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
        }
    }
    
    @Test
    public void assertSelectWithMetaData() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(SELECT_SQL)) {
            statement.setObject(1, 'a');
            ResultSetMetaData metaData = statement.executeQuery().getMetaData();
            assertThat(metaData.getColumnCount(), is(2));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("pwd"));
            }
        }
    }
    
    @Test
    public void assertSelectWithExecuteWithProperties() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(SELECT_ALL_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            boolean result = statement.execute();
            assertTrue(result);
            assertThat(statement.getResultSetType(), is(ResultSet.TYPE_FORWARD_ONLY));
            assertThat(statement.getResultSetConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
            assertThat(statement.getResultSetHoldability(), is(ResultSet.HOLD_CURSORS_OVER_COMMIT));
        }
    }
    
    private void assertResultSet(final int resultSetCount, final int id, final Object pwd, final Object assistPwd) throws SQLException {
        try (Connection conn = getDATABASE_TYPE_MAP().get(DatabaseTypes.getActualDatabaseType("H2")).get("encrypt").getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(SELECT_ALL_SQL);
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
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(null)) {
            statement.executeQuery();
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement("")) {
            statement.executeQuery();
        }
    }
    
    @Test
    public void assertSelectWithInOperator() throws SQLException {
        try (PreparedStatement statement = getEncryptConnection().prepareStatement(SELECT_SQL_WITH_IN_OPERATOR)) {
            statement.setObject(1, 'a');
            ResultSetMetaData metaData = statement.executeQuery().getMetaData();
            assertThat(metaData.getColumnCount(), is(2));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("pwd"));
            }
        }
    }
    
    @Test
    public void assertSelectWithPlainColumnForContainsColumn() throws SQLException {
        try (PreparedStatement statement = getEncryptConnectionWithProps().prepareStatement(SELECT_SQL_FOR_CONTAINS_COLUMN)) {
            statement.setObject(1, "plainValue");
            ResultSetMetaData metaData = statement.executeQuery().getMetaData();
            assertThat(metaData.getColumnCount(), is(3));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("plain_pwd")); 
                assertThat(metaData.getColumnLabel(3), is("plain_pwd2"));
            }
        }
    }
}
