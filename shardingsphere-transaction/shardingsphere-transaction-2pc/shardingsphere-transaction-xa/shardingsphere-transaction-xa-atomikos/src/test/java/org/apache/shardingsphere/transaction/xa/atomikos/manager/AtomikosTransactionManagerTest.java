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

package org.apache.shardingsphere.transaction.xa.atomikos.manager;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.apache.shardingsphere.transaction.xa.atomikos.manager.fixture.ReflectiveUtil;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AtomikosTransactionManagerTest {
    
    private final AtomikosTransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
    
    @Mock
    private UserTransactionManager userTransactionManager;
    
    @Mock
    private UserTransactionService userTransactionService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Before
    public void setUp() {
        ReflectiveUtil.setProperty(atomikosTransactionManager, "transactionManager", userTransactionManager);
        ReflectiveUtil.setProperty(atomikosTransactionManager, "userTransactionService", userTransactionService);
    }
    
    @Test
    public void assertRegisterRecoveryResource() {
        atomikosTransactionManager.registerRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).registerResource(any(AtomikosXARecoverableResource.class));
    }
    
    @Test
    public void assertRemoveRecoveryResource() {
        atomikosTransactionManager.removeRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).removeResource(any(AtomikosXARecoverableResource.class));
    }
    
    @Test
    public void assertEnListResource() throws SystemException, RollbackException {
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Transaction transaction = mock(Transaction.class);
        when(userTransactionManager.getTransaction()).thenReturn(transaction);
        atomikosTransactionManager.enlistResource(singleXAResource);
        verify(transaction).enlistResource(singleXAResource);
    }
    
    @Test
    public void assertTransactionManager() {
        assertThat(atomikosTransactionManager.getTransactionManager(), is(userTransactionManager));
    }
    
    @Test
    public void assertClose() {
        atomikosTransactionManager.close();
        verify(userTransactionService).shutdown(true);
    }
}
