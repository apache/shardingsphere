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

package org.apache.shardingsphere.proxy.backend;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.UnicastBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.UseDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.set.ShardingCTLSetBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.show.ShardingCTLShowBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TextProtocolBackendHandlerFactoryTest {
    
    private final DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType("MySQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(backendConnection.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.LOCAL);
        setTransactionContexts();
        when(backendConnection.getSchemaName()).thenReturn("schema");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setTransactionContexts() {
        Field transactionContexts = ProxyContext.getInstance().getClass().getDeclaredField("transactionContexts");
        transactionContexts.setAccessible(true);
        transactionContexts.set(ProxyContext.getInstance(), createSchemaContext());
    }
    
    private TransactionContexts createSchemaContext() {
        TransactionContexts result = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        when(result.getEngines().get("schema")).thenReturn(new ShardingTransactionManagerEngine());
        return result;
    }
    
    @Test
    public void assertNewInstanceWithSCTL() {
        String sql = "sctl:set transaction_type=XA";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(ShardingCTLSetBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceSCTLWithComment() {
        String sql = "/*ApplicationName=DataGrip 2018.1.4*/ sctl:show cached_connections;";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(ShardingCTLShowBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithBegin() {
        String sql = "BEGIN";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithStartTransaction() {
        String sql = "START TRANSACTION";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSetAutoCommitToOff() {
        String sql = "SET AUTOCOMMIT=0";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithScopeSetAutoCommitToOff() {
        String sql = "SET @@SESSION.AUTOCOMMIT = OFF";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSetAutoCommitToOnForInTransaction() {
        when(backendConnection.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SET AUTOCOMMIT=1";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithScopeSetAutoCommitToOnForInTransaction() {
        when(backendConnection.getTransactionStatus().isInTransaction()).thenReturn(true);
        String sql = "SET @@SESSION.AUTOCOMMIT = ON";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSetAutoCommitToOnForNotInTransaction() {
        String sql = "SET AUTOCOMMIT=1";
        when(backendConnection.getTransactionStatus().isInTransaction()).thenReturn(false);
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(SkipBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithUse() {
        String sql = "use sharding_db";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(UseDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithShowDatabase() {
        String sql = "show databases";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(ShowDatabasesBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithSet() {
        String sql = "set @num=1";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(BroadcastBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithShow() {
        String sql = "SHOW VARIABLES LIKE %x%";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(UnicastBackendHandler.class));
        sql = "SHOW VARIABLES WHERE Variable_name ='language'";
        actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(UnicastBackendHandler.class));
        sql = "SHOW CHARACTER SET";
        actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(UnicastBackendHandler.class));
        sql = "SHOW COLLATION";
        actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(UnicastBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithQuery() {
        String sql = "select * from t_order limit 1";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(QueryBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithEmptyString() {
        String sql = "";
        TextProtocolBackendHandler actual = TextProtocolBackendHandlerFactory.newInstance(databaseType, sql, backendConnection);
        assertThat(actual, instanceOf(SkipBackendHandler.class));
    }
}
