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

package org.apache.shardingsphere.transaction.xa.jta.datasource;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class XATransactionDataSourceTest {
    
    @Mock
    private XATransactionManager xaTransactionManager;
    
    @Mock
    private TransactionManager transactionManager;
    
    @Mock
    private Transaction transaction;
    
    @Before
    public void setUp() throws SystemException {
        when(xaTransactionManager.getTransactionManager()).thenReturn(transactionManager);
        when(transactionManager.getTransaction()).thenReturn(transaction);
    }
    
    @Test
    public void assertGetAtomikosConnection() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds1", dataSource, xaTransactionManager);
        try (Connection ignored = transactionDataSource.getConnection()) {
            verify(xaTransactionManager, times(0)).getTransactionManager();
        }
    }
    
    @Test
    public void assertGetHikariConnection() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds1", dataSource, xaTransactionManager);
        try (Connection ignored = transactionDataSource.getConnection()) {
            verify(transaction).enlistResource(any(SingleXAResource.class));
            verify(transaction).registerSynchronization(any(Synchronization.class));
        }
        try (Connection ignored = transactionDataSource.getConnection()) {
            verify(transaction).enlistResource(any(SingleXAResource.class));
            verify(transaction).registerSynchronization(any(Synchronization.class));
        }
    }
    
    @Test
    public void assertCloseAtomikosDataSourceBean() {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds11");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds11", dataSource, xaTransactionManager);
        transactionDataSource.close();
        verify(xaTransactionManager, times(0)).removeRecoveryResource(anyString(), any(XADataSource.class));
       
    }
    
    @Test
    public void assertCloseHikariDataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(DatabaseTypeRegistry.getActualDatabaseType("H2"), "ds1", dataSource, xaTransactionManager);
        transactionDataSource.close();
        verify(xaTransactionManager).removeRecoveryResource(anyString(), any(XADataSource.class));
        
    }
}
