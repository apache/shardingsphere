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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataSourceXAResourceRecoveryHelperTest {
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private XAResource xaResource;
    
    @Mock
    private XAConnection xaConnection;
    
    private DataSourceXAResourceRecoveryHelper recoveryHelper;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        recoveryHelper = new DataSourceXAResourceRecoveryHelper(xaDataSource);
    }
    
    @Test
    void assertGetXAResourcesCreatingConnecting() throws SQLException {
        recoveryHelper.getXAResources();
        XAResource[] xaResources = recoveryHelper.getXAResources();
        assertThat(xaResources.length, is(1));
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(xaConnection).getXAResource();
        verify(xaDataSource).getXAConnection();
    }
    
    @Test
    void assertGetXAResourcesWithoutConnecting() throws SQLException, ReflectiveOperationException {
        Plugins.getMemberAccessor().set(DataSourceXAResourceRecoveryHelper.class.getDeclaredField("delegate"), recoveryHelper, xaResource);
        recoveryHelper.getXAResources();
        XAResource[] xaResources = recoveryHelper.getXAResources();
        assertThat(xaResources.length, is(1));
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(xaConnection, never()).getXAResource();
        verify(xaDataSource, never()).getXAConnection();
    }
    
    @Test
    void assertDelegateRecover() throws XAException, SQLException {
        recoveryHelper.getXAResources();
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        verify(xaResource).recover(XAResource.TMSTARTRSCAN);
        verify(xaConnection, never()).close();
    }
    
    @Test
    void assertDelegateRecoverAndCloseConnection() throws XAException, SQLException {
        recoveryHelper.getXAResources();
        recoveryHelper.recover(XAResource.TMENDRSCAN);
        verify(xaResource).recover(XAResource.TMENDRSCAN);
        verify(xaConnection).close();
    }
    
    @Test
    void assertDelegateStart() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.start(null, 0);
        verify(xaResource).start(null, 0);
    }
    
    @Test
    void assertDelegateEnd() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.end(null, 0);
        verify(xaResource).end(null, 0);
    }
    
    @Test
    void assertDelegatePrepare() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.prepare(null);
        verify(xaResource).prepare(null);
    }
    
    @Test
    void assertDelegateCommit() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.commit(null, true);
        verify(xaResource).commit(null, true);
    }
    
    @Test
    void assertDelegateRollback() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.rollback(null);
        verify(xaResource).rollback(null);
    }
    
    @Test
    void assertDelegateIsSameRM() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.isSameRM(null);
        verify(xaResource).isSameRM(null);
    }
    
    @Test
    void assertDelegateForget() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.forget(null);
        verify(xaResource).forget(null);
    }
    
    @Test
    void assertDelegateGetTransactionTimeout() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.getTransactionTimeout();
        verify(xaResource).getTransactionTimeout();
    }
    
    @Test
    void assertDelegateSetTransactionTimeout() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.setTransactionTimeout(0);
        verify(xaResource).setTransactionTimeout(0);
    }
}
