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

public final class EncryptPreparedStatementTest extends AbstractEncryptJDBCDatabaseAndTableTest {
    
    private static final String INSERT_SQL = "insert into t_query_encrypt(id, pwd) values(?,?)";
    
    private static final String SELECT_ALL_SQL = "select id, pwd, assist_pwd from t_query_encrypt";
    
    private static final List<Object> parameters = Arrays.asList((Object) 2, 'b');
    
    private static final List<Object> batchParameters = Arrays.asList((Object) 3, 'c', 4, 'd');
    
    private EncryptConnection encryptConnection;
    
    @Before
    public void setUp() {
        encryptConnection = getEncryptDataSource().getConnection();
    }
    
    @Test
    private void assertInsertWithExecute() throws SQLException {
        try (PreparedStatement statement = encryptConnection.prepareStatement(INSERT_SQL)) {
            statement.setObject(1, parameters.get(0));
            statement.setObject(2, parameters.get(1));
            statement.execute();
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
            assertThat(count, is(resultSetCount));
        }
    }
    
    
    @Test
    public void testGetResultSet() throws SQLException {
        assertInsertWithExecute();
    }
    
    @Test
    public void testExecuteUpdate() {
    }
    
    @Test
    public void testExecute() {
    }
    
    @Test
    public void testAddBatch() {
    }
    
    @Test
    public void testExecuteBatch() {
    }
    
    @Test
    public void testClearBatch() {
    }
    
    @Test
    public void testGetGeneratedKeys() {
    }
    
    @Test
    public void testGetConnection() {
    }
    
    @Test
    public void testGetResultSetConcurrency() {
    }
    
    @Test
    public void testGetResultSetType() {
    }
    
    @Test
    public void testGetResultSetHoldability() {
    }
    
    @Test
    public void testIsAccumulate() {
    }
    
    @Test
    public void testGetRoutedStatements() {
    }
}
