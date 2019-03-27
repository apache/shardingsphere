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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import org.apache.shardingsphere.shardingjdbc.common.base.AbstractEncryptJDBCDatabaseAndTableTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptPreparedStatementTest extends AbstractEncryptJDBCDatabaseAndTableTest {
    
    private static final String INSERT_SQL = "insert into t_query_encrypt(id, pwd) values(?,?)";
    
    private static final String DELETE_SQL = "delete from t_query_encrypt where pwd = ? and id = ?";
    
    private static final String UPDATE_SQL = "update t_query_encrypt set pwd =? where pwd = ?";
    
    private static final String SELECT_SQL = "select * from t_query_encrypt where pwd = ? ";
    
    private static final String SELECT_ALL_SQL = "select id, pwd, assist_pwd from t_query_encrypt";
    
    private static final List<Object> parameters = Arrays.asList((Object) 2, 'b');
    
    private static final List<Object> batchParameters = Arrays.asList((Object) 3, 'c', 4, 'd');
    
    private EncryptConnection encryptConnection;
    
    @Before
    public void setUp() {
        encryptConnection = getEncryptDataSource().getConnection();
    }
    
    @Test
    public void assertInsertWithExecute() throws SQLException {
        try (PreparedStatement statement = encryptConnection.prepareStatement(INSERT_SQL)) {
            statement.setObject(1, parameters.get(0));
            statement.setObject(2, parameters.get(1));
            statement.execute();
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertInsertWithBatchExecute() throws SQLException {
        try (PreparedStatement statement = encryptConnection.prepareStatement(INSERT_SQL)) {
            statement.setObject(1, batchParameters.get(0));
            statement.setObject(2, batchParameters.get(1));
            statement.addBatch();
            statement.setObject(1, batchParameters.get(2));
            statement.setObject(2, batchParameters.get(3));
            statement.addBatch();
            statement.executeBatch();
        }
        assertResultSet(4, 3, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertDeleteWithExecute() throws SQLException {
        try (PreparedStatement statement = encryptConnection.prepareStatement(DELETE_SQL)) {
            statement.setObject(1, 'a');
            statement.setObject(2, 1);
            statement.execute();
        }
        assertResultSet(1, 5, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (PreparedStatement statement = encryptConnection.prepareStatement(UPDATE_SQL)) {
            statement.setObject(1, 'f');
            statement.setObject(2, 'a');
            result = statement.executeUpdate();
        }
        assertThat(result, is(2));
        assertResultSet(2, 1, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertSelectWithExecuteQuery() throws SQLException {
        try (PreparedStatement statement = encryptConnection.prepareStatement(SELECT_SQL)) {
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
    
    private void assertResultSet(final int resultSetCount, final int id, final Object pwd, final Object assist_pwd) throws SQLException {
        try (Connection conn = getDatabaseTypeMap().values().iterator().next().values().iterator().next().getConnection(); 
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(SELECT_ALL_SQL);
            int count = 1;
            while (resultSet.next()) {
                if (id == count) {
                    assertThat(pwd, is(resultSet.getObject("pwd")));
                    assertThat(assist_pwd, is(resultSet.getObject("assist_pwd")));
                }
                count += 1;
            }
            assertThat(count - 1, is(resultSetCount));
        }
    }
}
