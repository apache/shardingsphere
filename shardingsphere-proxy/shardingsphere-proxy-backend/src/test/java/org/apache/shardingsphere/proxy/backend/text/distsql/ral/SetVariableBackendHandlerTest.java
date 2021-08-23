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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.variable.SetVariableStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.mode.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.persist.PersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.SetDistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SetVariableBackendHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private final BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(PersistService.class),
                getMetaDataMap(), mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
            ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
            when(metaData.getResource()).thenReturn(new ShardingSphereResource(Collections.emptyMap(), null, null, new MySQLDatabaseType()));
            when(metaData.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()));
            when(metaData.getSchema()).thenReturn(schema);
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    @Test
    public void assertSwitchTransactionTypeXA() throws SQLException {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        SetDistSQLBackendHandler shardingCTLBackendHandler = new SetDistSQLBackendHandler(new SetVariableStatement("transaction_type", "XA"), backendConnection);
        ResponseHeader actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() throws SQLException {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        SetDistSQLBackendHandler shardingCTLBackendHandler = new SetDistSQLBackendHandler(new SetVariableStatement("transaction_type", "BASE"), backendConnection);
        ResponseHeader actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() throws SQLException {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        SetDistSQLBackendHandler shardingCTLBackendHandler = new SetDistSQLBackendHandler(new SetVariableStatement("transaction_type", "LOCAL"), backendConnection);
        ResponseHeader actual = shardingCTLBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertSwitchTransactionTypeFailed() throws SQLException {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        new SetDistSQLBackendHandler(new SetVariableStatement("transaction_type", "XXX"), backendConnection).execute();
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertNotSupportedVariable() throws SQLException {
        new SetDistSQLBackendHandler(new SetVariableStatement("@@session", "XXX"), backendConnection).execute();
    }
}
