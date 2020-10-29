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
import org.apache.shardingsphere.transaction.xa.narayana.manager.fixture.ReflectiveUtil;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class NarayanaXATransactionManagerTest {
    
    private final NarayanaXATransactionManager narayanaXATransactionManager = new NarayanaXATransactionManager();
    
    @Mock
    private TransactionManager transactionManager;
    
    @Mock
    private XARecoveryModule xaRecoveryModule;
    
    @Mock
    private RecoveryManagerService recoveryManagerService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Before
    public void setUp() {
        ReflectiveUtil.setProperty(narayanaXATransactionManager, "xaRecoveryModule", xaRecoveryModule);
        ReflectiveUtil.setProperty(narayanaXATransactionManager, "transactionManager", transactionManager);
        ReflectiveUtil.setProperty(narayanaXATransactionManager, "recoveryManagerService", recoveryManagerService);
    }
    
    @Test
    public void assertInit() {
        narayanaXATransactionManager.init();
        verify(recoveryManagerService).create();
        verify(recoveryManagerService).start();
    }
    
    @Test
    public void assertRegisterRecoveryResource() {
        narayanaXATransactionManager.registerRecoveryResource("ds1", xaDataSource);
        verify(xaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
    }
    
    @Test
    public void assertRemoveRecoveryResource() {
        narayanaXATransactionManager.removeRecoveryResource("ds1", xaDataSource);
        verify(xaRecoveryModule).removeXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
    }
    
    @Test
    public void assertEnlistResource() throws SystemException, RollbackException {
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Transaction transaction = mock(Transaction.class);
        when(transactionManager.getTransaction()).thenReturn(transaction);
        narayanaXATransactionManager.enlistResource(singleXAResource);
        verify(transaction).enlistResource(singleXAResource.getDelegate());
    }
    
    @Test
    public void assertGetTransactionManager() {
        assertThat(narayanaXATransactionManager.getTransactionManager(), is(transactionManager));
    }
    
    @Test
    public void assertClose() throws Exception {
        narayanaXATransactionManager.close();
        verify(recoveryManagerService).stop();
        verify(recoveryManagerService).destroy();
    }
}
