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

import org.apache.shardingsphere.transaction.xa.narayana.manager.fixture.ReflectiveUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceXAResourceRecoveryHelperTest {
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private XAResource xaResource;
    
    @Mock
    private XAConnection xaConnection;
    
    private DataSourceXAResourceRecoveryHelper recoveryHelper;
    
    @Before
    public void setUp() throws SQLException {
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        when(xaDataSource.getXAConnection()).thenReturn(xaConnection);
        recoveryHelper = new DataSourceXAResourceRecoveryHelper(xaDataSource);
    }
    
    @Test
    public void assertGetXAResourcesCreatingConnecting() throws SQLException {
        recoveryHelper.getXAResources();
        XAResource[] xaResources = recoveryHelper.getXAResources();
        assertThat(xaResources.length, is(1));
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(xaConnection, times(1)).getXAResource();
        verify(xaDataSource, times(1)).getXAConnection();
    }
    
    @Test
    public void assertGetXAResourcesWithoutConnecting() throws SQLException {
        ReflectiveUtil.setProperty(recoveryHelper, "delegate", xaResource);
        recoveryHelper.getXAResources();
        XAResource[] xaResources = recoveryHelper.getXAResources();
        assertThat(xaResources.length, is(1));
        assertThat(xaResources[0], sameInstance(recoveryHelper));
        verify(xaConnection, times(0)).getXAResource();
        verify(xaDataSource, times(0)).getXAConnection();
    }
    
    @Test
    public void assertDelegateRecover() throws XAException, SQLException {
        recoveryHelper.getXAResources();
        recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        verify(xaResource, times(1)).recover(XAResource.TMSTARTRSCAN);
        verify(xaConnection, times(0)).close();
    }
    
    @Test
    public void assertDelegateRecoverAndCloseConnection() throws XAException, SQLException {
        recoveryHelper.getXAResources();
        recoveryHelper.recover(XAResource.TMENDRSCAN);
        verify(xaResource).recover(XAResource.TMENDRSCAN);
        verify(xaConnection).close();
    }
    
    @Test
    public void assertDelegateStart() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.start(null, 0);
        verify(xaResource, times(1)).start(null, 0);
    }
    
    @Test
    public void assertDelegateEnd() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.end(null, 0);
        verify(xaResource, times(1)).end(null, 0);
    }
    
    @Test
    public void assertDelegatePrepare() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.prepare(null);
        verify(xaResource, times(1)).prepare(null);
    }
    
    @Test
    public void assertDelegateCommit() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.commit(null, true);
        verify(xaResource, times(1)).commit(null, true);
    }
    
    @Test
    public void assertDelegateRollback() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.rollback(null);
        verify(xaResource, times(1)).rollback(null);
    }
    
    @Test
    public void assertDelegateIsSameRM() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.isSameRM(null);
        verify(xaResource, times(1)).isSameRM(null);
    }
    
    @Test
    public void assertDelegateForget() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.forget(null);
        verify(xaResource, times(1)).forget(null);
    }
    
    @Test
    public void assertDelegateGetTransactionTimeout() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.getTransactionTimeout();
        verify(xaResource, times(1)).getTransactionTimeout();
    }
    
    @Test
    public void assertDelegateSetTransactionTimeout() throws XAException {
        recoveryHelper.getXAResources();
        recoveryHelper.setTransactionTimeout(0);
        verify(xaResource, times(1)).setTransactionTimeout(0);
    }
}
