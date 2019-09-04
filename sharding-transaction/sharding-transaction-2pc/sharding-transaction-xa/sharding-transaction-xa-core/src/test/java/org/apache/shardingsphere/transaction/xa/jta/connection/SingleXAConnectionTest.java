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

package org.apache.shardingsphere.transaction.xa.jta.connection;

import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class SingleXAConnectionTest {
    
    @Mock
    private XAConnection xaConnection;
    
    @Mock
    private Connection connection;
    
    private SingleXAConnection singleXAConnection;
    
    @Before
    public void setUp() {
        singleXAConnection = new SingleXAConnection("ds1", connection, xaConnection);
    }
    
    @Test
    public void assertGetConnection() {
        Connection actual = singleXAConnection.getConnection();
        assertThat(actual, is(connection));
    }
    
    @Test
    public void assertGetXAResource() throws SQLException {
        XAResource actual = singleXAConnection.getXAResource();
        assertThat(actual, instanceOf(SingleXAResource.class));
    }
    
    @Test
    public void close() throws SQLException {
        singleXAConnection.close();
        verify(xaConnection).close();
    }
    
    @Test
    public void assertAddConnectionEventListener() {
        singleXAConnection.addConnectionEventListener(mock(ConnectionEventListener.class));
        verify(xaConnection).addConnectionEventListener(any(ConnectionEventListener.class));
    }
    
    @Test
    public void assertRemoveConnectionEventListener() {
        singleXAConnection.removeConnectionEventListener(mock(ConnectionEventListener.class));
        verify(xaConnection).removeConnectionEventListener(any(ConnectionEventListener.class));
    }
    
    @Test
    public void assertAddStatementEventListener() {
        singleXAConnection.addStatementEventListener(mock(StatementEventListener.class));
        verify(xaConnection).addStatementEventListener(any(StatementEventListener.class));
    }
    
    @Test
    public void removeStatementEventListener() {
        singleXAConnection.removeStatementEventListener(mock(StatementEventListener.class));
        verify(xaConnection).removeStatementEventListener(any(StatementEventListener.class));
    }
}
