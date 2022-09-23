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

package org.apache.shardingsphere.transaction.xa.bitronix.manager;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.resource.ResourceRegistrar;
import org.apache.shardingsphere.transaction.xa.bitronix.manager.fixture.ReflectiveUtil;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BitronixXATransactionManagerProviderTest {
    
    private final BitronixXATransactionManagerProvider transactionManagerProvider = new BitronixXATransactionManagerProvider();
    
    @Mock
    private BitronixTransactionManager transactionManager;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Before
    public void setUp() {
        ReflectiveUtil.setProperty(transactionManagerProvider, "transactionManager", transactionManager);
    }
    
    @Test
    public void assertRegisterRecoveryResourceThenRemove() {
        transactionManagerProvider.registerRecoveryResource("ds1", xaDataSource);
        assertNotNull(ResourceRegistrar.get("ds1"));
        transactionManagerProvider.removeRecoveryResource("ds1", xaDataSource);
        assertNull(ResourceRegistrar.get("ds1"));
    }
    
    @Test
    public void assertEnlistResource() throws SystemException, RollbackException {
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Transaction transaction = mock(Transaction.class);
        when(transactionManager.getTransaction()).thenReturn(transaction);
        transactionManagerProvider.enlistResource(singleXAResource);
        verify(transaction).enlistResource(singleXAResource);
    }
    
    @Test
    public void assertGetTransactionManager() {
        assertThat(transactionManagerProvider.getTransactionManager(), is(transactionManager));
    }
    
    @Test
    public void assertClose() {
        transactionManagerProvider.close();
        verify(transactionManager).shutdown();
    }
}
