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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptStatementTest extends AbstractEncryptDriverTest {
    
    private static final String INSERT_SQL = "INSERT INTO t_encrypt(id, pwd) VALUES (2,'b')";
    
    private static final String INSERT_GENERATED_KEY_SQL = "INSERT INTO t_encrypt(pwd) VALUES ('b')";
    
    private static final String DELETE_SQL = "DELETE FROM t_encrypt WHERE pwd = 'a' AND id = 1";
    
    private static final String UPDATE_SQL = "UPDATE t_encrypt SET pwd ='f' WHERE pwd = 'a'";
    
    private static final String SELECT_SQL = "SELECT id, pwd FROM t_encrypt WHERE pwd = 'a'";
    
    private static final String SELECT_SQL_WITH_STAR = "SELECT * FROM t_encrypt WHERE pwd = 'a'";
    
    private static final String SELECT_SQL_WITH_CIPHER = "SELECT id, pwd FROM t_encrypt WHERE pwd = 'plainValue'";
    
    private static final String SELECT_SQL_TO_ASSERT = "SELECT id, cipher_pwd FROM t_encrypt";
    
    private static final String SHOW_COLUMNS_SQL = "SHOW columns FROM t_encrypt";
    
    @Test
    void assertSQLShow() {
        assertTrue(getEncryptConnectionWithProps().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
    }
    
    @Test
    void assertInsertWithExecute() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            statement.execute(INSERT_SQL);
        }
        assertResultSet(3, 2, "encryptValue");
    }
    
    @Test
    void assertInsertWithExecuteWithGeneratedKey() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            statement.execute(INSERT_GENERATED_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(6));
            assertFalse(resultSet.next());
        }
        assertResultSet(3, 6, "encryptValue");
    }
    
    @Test
    void assertDeleteWithExecute() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            statement.execute(DELETE_SQL);
        }
        assertResultSet(1, 5, "encryptValue");
    }
    
    @Test
    void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (Statement statement = getEncryptConnection().createStatement()) {
            result = statement.executeUpdate(UPDATE_SQL);
        }
        assertThat(result, is(2));
        assertResultSet(2, 1, "encryptValue");
    }
    
    @Test
    void assertSelectWithExecuteQuery() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL);
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertThat(resultSet.getString(2), is("decryptValue"));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
            assertThat(resultSet.getString(2), is("decryptValue"));
        }
    }
    
    @Test
    void assertSelectWithExecuteWithProperties() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            int[] columnIndexes = {1, 2};
            boolean result = statement.execute(SELECT_SQL, columnIndexes);
            assertTrue(result);
            assertThat(statement.getResultSetType(), is(ResultSet.TYPE_FORWARD_ONLY));
            assertThat(statement.getResultSetConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
            assertThat(statement.getResultSetHoldability(), is(ResultSet.HOLD_CURSORS_OVER_COMMIT));
        }
    }
    
    @Test
    void assertSelectWithMetaData() throws SQLException {
        try (Statement statement = getEncryptConnectionWithProps().createStatement()) {
            ResultSetMetaData metaData = statement.executeQuery(SELECT_SQL_WITH_STAR).getMetaData();
            assertThat(metaData.getColumnCount(), is(2));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("pwd"));
            }
        }
    }
    
    @Test
    void assertSelectWithCipherColumn() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL_WITH_CIPHER);
            int count = 1;
            List<Object> ids = Arrays.asList(1, 5);
            while (resultSet.next()) {
                assertThat(resultSet.getObject("id"), is(ids.get(count - 1)));
                assertThat(resultSet.getObject("pwd"), is("decryptValue"));
                count += 1;
            }
            assertThat(count - 1, is(ids.size()));
        }
    }
    
    private void assertResultSet(final int resultSetCount, final int id, final Object pwd) throws SQLException {
        try (
                Connection connection = getActualDataSources().get("encrypt").getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL_TO_ASSERT);
            int count = 1;
            while (resultSet.next()) {
                if (id == count) {
                    assertThat(resultSet.getObject("cipher_pwd"), is(pwd));
                }
                count += 1;
            }
            assertThat(count - 1, is(resultSetCount));
        }
    }
    
    @Test
    void assertQueryWithNull() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery(null));
        }
    }
    
    @Test
    void assertQueryWithEmptyString() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery(""));
        }
    }
    
    @Test
    void assertShowColumnsTable() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SHOW_COLUMNS_SQL);
            int count = 0;
            while (resultSet.next()) {
                if ("pwd".equals(resultSet.getString("FIELD"))) {
                    count++;
                }
            }
            assertThat(count, is(1));
        }
    }
}
