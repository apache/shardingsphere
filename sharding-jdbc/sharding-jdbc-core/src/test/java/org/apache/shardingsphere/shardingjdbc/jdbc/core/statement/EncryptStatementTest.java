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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptStatementTest extends AbstractEncryptJDBCDatabaseAndTableTest {
    private static final String INSERT_SQL = "insert into t_encrypt(id, pwd) values(2,'b')";
    
    private static final String DELETE_SQL = "delete from t_encrypt where pwd = 'a' and id = 1";
    
    private static final String UPDATE_SQL = "update t_encrypt set pwd ='f' where pwd = 'a'";
    
    private static final String SELECT_SQL = "select * from t_encrypt where pwd = 'a' ";
    
    private static final String SELECT_ALL_SQL = "select id, pwd from t_encrypt";
    
    private EncryptConnection encryptConnection;
    
    @Before
    public void setUp() {
        encryptConnection = getEncryptDataSource().getConnection();
    }
    
    @Test
    public void assertInsertWithExecute() throws SQLException {
        try (Statement statement = encryptConnection.createStatement()) {
            statement.execute(INSERT_SQL);
        }
        assertResultSet(3, 2, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertDeleteWithExecute() throws SQLException {
        try (Statement statement = encryptConnection.createStatement()) {
            statement.execute(DELETE_SQL);
        }
        assertResultSet(1, 5, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (Statement statement = encryptConnection.createStatement()) {
            result = statement.executeUpdate(UPDATE_SQL);
        }
        assertThat(result, is(2));
        assertResultSet(2, 1, "encryptValue", "assistedEncryptValue");
    }
    
    @Test
    public void assertSelectWithExecuteQuery() throws SQLException {
        try (Statement statement = encryptConnection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL);
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertThat(resultSet.getString(2), is("decryptValue"));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
            assertThat(resultSet.getString(2), is("decryptValue"));
        }
    }
    
    private void assertResultSet(final int resultSetCount, final int id, final Object pwd) throws SQLException {
        try (Connection conn = getDatabaseTypeMap().values().iterator().next().values().iterator().next().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(SELECT_ALL_SQL);
            int count = 1;
            while (resultSet.next()) {
                if (id == count) {
                    assertThat(pwd, is(resultSet.getObject("pwd")));
                }
                count += 1;
            }
            assertThat(count - 1, is(resultSetCount));
        }
    }
}
