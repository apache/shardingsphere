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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.storage.unit;

import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RegisterStorageUnitBackendHandlerTest {
    
    @Mock
    private DataSourcePropertiesValidateHandler validateHandler;
    
    @Mock
    private RegisterStorageUnitStatement registerStorageUnitStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    private RegisterStorageUnitBackendHandler registerStorageUnitBackendHandler;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        when(connectionSession.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        registerStorageUnitBackendHandler = new RegisterStorageUnitBackendHandler(registerStorageUnitStatement, connectionSession);
        Plugins.getMemberAccessor().set(registerStorageUnitBackendHandler.getClass().getDeclaredField("validateHandler"), registerStorageUnitBackendHandler, validateHandler);
    }
    
    @Test
    public void assertExecute() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().getDatabase("test_db")).thenReturn(database);
            ResponseHeader responseHeader = registerStorageUnitBackendHandler.execute("test_db", createRegisterStorageUnitStatement());
            assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
        }
    }
    
    @Test(expected = DuplicateStorageUnitException.class)
    public void assertExecuteWithDuplicateStorageUnitNamesInStatement() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            registerStorageUnitBackendHandler.execute("test_db", createRegisterStorageUnitStatementWithDuplicateStorageUnitNames());
        }
    }
    
    @Test(expected = DuplicateStorageUnitException.class)
    public void assertExecuteWithDuplicateStorageUnitNamesWithResourceMetaData() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("test_db").keySet()).thenReturn(Collections.singleton("ds_0"));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            registerStorageUnitBackendHandler.execute("test_db", createRegisterStorageUnitStatement());
        }
    }
    
    @Test(expected = InvalidStorageUnitsException.class)
    public void assertExecuteWithDuplicateStorageUnitNamesWithDataSourceContainedRule() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        DataSourceContainedRule rule = mock(DataSourceContainedRule.class);
        when(rule.getDataSourceMapper()).thenReturn(Collections.singletonMap("ds_0", Collections.emptyList()));
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(rule));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().getDatabase("test_db")).thenReturn(database);
            registerStorageUnitBackendHandler.execute("test_db", createRegisterStorageUnitStatement());
        }
    }
    
    @Test
    public void assertCheckStatementWithIfNotExists() {
        RegisterStorageUnitStatement registerStorageUnitStatementWithIfNotExists = new RegisterStorageUnitStatement(true, Collections.singleton(
                new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "db_1", "root", "", new Properties())));
        registerStorageUnitBackendHandler.checkSQLStatement("test_db", registerStorageUnitStatementWithIfNotExists);
    }
    
    private RegisterStorageUnitStatement createRegisterStorageUnitStatement() {
        return new RegisterStorageUnitStatement(false, Collections.singleton(new URLBasedDataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/test0", "root", "", new Properties())));
    }
    
    private RegisterStorageUnitStatement createRegisterStorageUnitStatementWithDuplicateStorageUnitNames() {
        Collection<DataSourceSegment> result = new LinkedList<>();
        result.add(new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()));
        result.add(new URLBasedDataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", "root", "", new Properties()));
        return new RegisterStorageUnitStatement(false, result);
    }
}
