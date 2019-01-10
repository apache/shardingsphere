/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.jta.connection;

import io.shardingsphere.transaction.xa.jta.ShardingXAResource;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShardingXAConnectionTest {
    
    @Mock
    private XAConnection xaConnection;
    
    private ShardingXAConnection shardingXAConnection;
    
    @Before
    public void setUp() {
        shardingXAConnection = new ShardingXAConnection("ds1", xaConnection);
    }
    
    @Test
    @SneakyThrows
    public void assertGetConnection() {
        shardingXAConnection.getConnection();
        verify(xaConnection).getConnection();
    }
    
    @Test
    @SneakyThrows
    public void assertGetXAResource() {
        XAResource actual = shardingXAConnection.getXAResource();
        assertThat(actual, instanceOf(ShardingXAResource.class));
    }
    
    @Test
    @SneakyThrows
    public void close() {
        shardingXAConnection.close();
        verify(xaConnection).close();
    }
    
    @Test
    public void assertAddConnectionEventListener() {
        shardingXAConnection.addConnectionEventListener(mock(ConnectionEventListener.class));
        verify(xaConnection).addConnectionEventListener(any(ConnectionEventListener.class));
    }
    
    @Test
    public void assertRemoveConnectionEventListener() {
        shardingXAConnection.removeConnectionEventListener(mock(ConnectionEventListener.class));
        verify(xaConnection).removeConnectionEventListener(any(ConnectionEventListener.class));
    }
    
    @Test
    public void assertAddStatementEventListener() {
        shardingXAConnection.addStatementEventListener(mock(StatementEventListener.class));
        verify(xaConnection).addStatementEventListener(any(StatementEventListener.class));
    }
    
    @Test
    public void removeStatementEventListener() {
        shardingXAConnection.removeStatementEventListener(mock(StatementEventListener.class));
        verify(xaConnection).removeStatementEventListener(any(StatementEventListener.class));
    }
}
