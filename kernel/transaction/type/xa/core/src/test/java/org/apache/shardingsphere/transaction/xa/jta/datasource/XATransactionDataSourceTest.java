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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class XATransactionDataSourceTest {
    
    @Mock
    private XATransactionManagerProvider xaTransactionManagerProvider;
    
    @Mock
    private TransactionManager transactionManager;
    
    @Mock
    private Transaction transaction;
    
    @BeforeEach
    void setUp() throws SystemException {
        when(xaTransactionManagerProvider.getTransactionManager()).thenReturn(transactionManager);
        when(transactionManager.getTransaction()).thenReturn(transaction);
    }
    
    @Test
    void assertGetAtomikosConnection() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        try (Connection ignored = transactionDataSource.getConnection()) {
            verify(xaTransactionManagerProvider, times(0)).getTransactionManager();
        }
    }
    
    @Test
    void assertGetHikariConnection() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
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
    void assertCloseAtomikosDataSourceBean() {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds11");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds11", dataSource, xaTransactionManagerProvider);
        transactionDataSource.close();
        verify(xaTransactionManagerProvider, times(0)).removeRecoveryResource(anyString(), any(XADataSource.class));
    }
    
    @Test
    void assertCloseHikariDataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        transactionDataSource.close();
        verify(xaTransactionManagerProvider).removeRecoveryResource(anyString(), any(XADataSource.class));
    }
}
