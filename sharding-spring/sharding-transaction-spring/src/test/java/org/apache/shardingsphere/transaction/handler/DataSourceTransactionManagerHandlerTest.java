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

package org.apache.shardingsphere.transaction.handler;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceTransactionManagerHandlerTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private DataSourceTransactionManager transactionManager;
    
    private DataSourceTransactionManagerHandler dataSourceTransactionManagerHandler;
    
    @Before
    public void setUp() {
        when(transactionManager.getDataSource()).thenReturn(dataSource);
        dataSourceTransactionManagerHandler = new DataSourceTransactionManagerHandler(transactionManager);
    }
    
    @Test
    public void assertSwitchTransactionTypeSuccess() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        dataSourceTransactionManagerHandler.switchTransactionType(TransactionType.XA);
        verify(statement).execute(anyString());
        TransactionSynchronizationManager.unbindResourceIfPossible(dataSource);
    }
    
    @Test(expected = ShardingException.class)
    public void assertSwitchTransactionTypeFailExecute() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute(anyString())).thenThrow(new SQLException("Mock send switch transaction type SQL failed"));
        try {
            dataSourceTransactionManagerHandler.switchTransactionType(TransactionType.XA);
        } finally {
            TransactionSynchronizationManager.unbindResourceIfPossible(dataSource);
        }
    }
    
    @Test(expected = ShardingException.class)
    public void assertSwitchTransactionTypeFailGetConnection() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Mock get connection failed"));
        try {
            dataSourceTransactionManagerHandler.switchTransactionType(TransactionType.XA);
        } finally {
            TransactionSynchronizationManager.unbindResourceIfPossible(dataSource);
        }
    }
    
    @Test
    public void assertUnbindResource() {
        ConnectionHolder holder = mock(ConnectionHolder.class);
        Connection connection = mock(Connection.class);
        when(holder.getConnection()).thenReturn(connection);
        TransactionSynchronizationManager.bindResource(dataSource, holder);
        dataSourceTransactionManagerHandler.unbindResource();
        assertNull(TransactionSynchronizationManager.getResource(dataSource));
    }
}
