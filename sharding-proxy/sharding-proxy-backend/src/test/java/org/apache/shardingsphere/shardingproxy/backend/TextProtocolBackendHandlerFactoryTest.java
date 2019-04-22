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

package org.apache.shardingsphere.shardingproxy.backend;

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.ConnectionStateHandler;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UseDatabaseBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.set.ShardingCTLSetBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.SkipBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TextProtocolBackendHandlerFactoryTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(backendConnection.getTransactionType()).thenReturn(TransactionType.LOCAL);
        LogicSchema logicSchema = mock(LogicSchema.class);
        JDBCBackendDataSource backendDataSource = mock(JDBCBackendDataSource.class);
        when(backendDataSource.getShardingTransactionManagerEngine()).thenReturn(mock(ShardingTransactionManagerEngine.class));
        when(logicSchema.getBackendDataSource()).thenReturn(backendDataSource);
        when(backendConnection.getLogicSchema()).thenReturn(logicSchema);
    }
    
    @Test
    public void assertNewInstance() {
        String sql = "BEGIN";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewTransactionBackendHandlerInstanceOfCommitOperate() {
        String sql = "SET AUTOCOMMIT=1";
        ConnectionStateHandler stateHandler = mock(ConnectionStateHandler.class);
        when(backendConnection.getStateHandler()).thenReturn(stateHandler);
        when(stateHandler.isInTransaction()).thenReturn(true);
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewIgnoreBackendHandlerInstance() {
        String sql = "SET AUTOCOMMIT=1";
        ConnectionStateHandler stateHandler = mock(ConnectionStateHandler.class);
        when(backendConnection.getStateHandler()).thenReturn(stateHandler);
        when(stateHandler.isInTransaction()).thenReturn(false);
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(SkipBackendHandler.class));
    }
    
    @Test
    public void assertNewShardingCTLBackendHandlerInstance() {
        String sql = "sctl:set transaction_type=XA";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(ShardingCTLSetBackendHandler.class));
    }
    
    @Test
    public void assertNewSchemaBroadcastBackendHandlerInstance() {
        String sql = "set @num=1";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(BroadcastBackendHandler.class));
    }
    
    @Test
    public void assertNewUseSchemaBackendHandlerInstance() {
        String sql = "use sharding_db";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(UseDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewShowDatabasesBackendHandlerInstance() {
        String sql = "show databases;";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(ShowDatabasesBackendHandler.class));
    }
    
    @Test
    public void assertNewDefaultInstance() {
        String sql = "select * from t_order limit 1";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(sql, backendConnection);
        assertThat(actual, instanceOf(QueryBackendHandler.class));
    }
}
