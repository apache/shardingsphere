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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtomikosTransactionManagerProviderTest {
    
    private final AtomikosTransactionManagerProvider transactionManagerProvider = new AtomikosTransactionManagerProvider();
    
    @Mock
    private UserTransactionManager userTransactionManager;
    
    @Mock
    private UserTransactionService userTransactionService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(AtomikosTransactionManagerProvider.class.getDeclaredField("transactionManager"), transactionManagerProvider, userTransactionManager);
        Plugins.getMemberAccessor().set(AtomikosTransactionManagerProvider.class.getDeclaredField("userTransactionService"), transactionManagerProvider, userTransactionService);
    }
    
    @Test
    void assertRegisterRecoveryResource() {
        transactionManagerProvider.registerRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).registerResource(any(AtomikosXARecoverableResource.class));
    }
    
    @Test
    void assertRemoveRecoveryResource() {
        transactionManagerProvider.removeRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).removeResource(any(AtomikosXARecoverableResource.class));
    }
    
    @Test
    void assertEnListResource() throws SystemException, RollbackException {
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Transaction transaction = mock(Transaction.class);
        when(userTransactionManager.getTransaction()).thenReturn(transaction);
        transactionManagerProvider.enlistResource(singleXAResource);
        verify(transaction).enlistResource(singleXAResource);
    }
    
    @Test
    void assertTransactionManager() {
        assertThat(transactionManagerProvider.getTransactionManager(), is(userTransactionManager));
    }
    
    @Test
    void assertClose() {
        transactionManagerProvider.close();
        verify(userTransactionService).shutdown(true);
    }
    
    @Test
    void assertInit() throws Exception {
        transactionManagerProvider.init();
        assertNull(transactionManagerProvider.getTransactionManager().getTransaction());
        assertFalse(transactionManagerProvider.getTransactionManager().getForceShutdown());
        assertTrue(transactionManagerProvider.getTransactionManager().getStartupTransactionService());
    }
}
