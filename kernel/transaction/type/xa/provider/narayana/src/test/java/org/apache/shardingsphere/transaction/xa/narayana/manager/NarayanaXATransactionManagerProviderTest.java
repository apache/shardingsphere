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

package org.apache.shardingsphere.transaction.xa.narayana.manager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NarayanaXATransactionManagerProviderTest {
    
    private final NarayanaXATransactionManagerProvider transactionManagerProvider = new NarayanaXATransactionManagerProvider();
    
    @Mock
    private TransactionManager transactionManager;
    
    @Mock
    private XARecoveryModule xaRecoveryModule;
    
    @Mock
    private RecoveryManagerService recoveryManagerService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(NarayanaXATransactionManagerProvider.class.getDeclaredField("xaRecoveryModule"), transactionManagerProvider, xaRecoveryModule);
        Plugins.getMemberAccessor().set(NarayanaXATransactionManagerProvider.class.getDeclaredField("transactionManager"), transactionManagerProvider, transactionManager);
        Plugins.getMemberAccessor().set(NarayanaXATransactionManagerProvider.class.getDeclaredField("recoveryManagerService"), transactionManagerProvider, recoveryManagerService);
    }
    
    @Test
    void assertRegisterRecoveryResource() {
        transactionManagerProvider.registerRecoveryResource("ds1", xaDataSource);
        verify(xaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
    }
    
    @Test
    void assertRemoveRecoveryResource() {
        transactionManagerProvider.removeRecoveryResource("ds1", xaDataSource);
        verify(xaRecoveryModule).removeXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
    }
    
    @Test
    void assertEnlistResource() throws SystemException, RollbackException {
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Transaction transaction = mock(Transaction.class);
        when(transactionManager.getTransaction()).thenReturn(transaction);
        transactionManagerProvider.enlistResource(singleXAResource);
        verify(transaction).enlistResource(singleXAResource.getDelegate());
    }
    
    @Test
    void assertGetTransactionManager() {
        assertThat(transactionManagerProvider.getTransactionManager(), is(transactionManager));
    }
    
    @Test
    void assertClose() throws Exception {
        transactionManagerProvider.close();
        verify(recoveryManagerService).stop();
        verify(recoveryManagerService).destroy();
    }
}
