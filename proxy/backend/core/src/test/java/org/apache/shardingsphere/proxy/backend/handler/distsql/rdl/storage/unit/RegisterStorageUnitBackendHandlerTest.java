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
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterStorageUnitBackendHandlerTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    private RegisterStorageUnitBackendHandler handler;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        handler = new RegisterStorageUnitBackendHandler(mock(RegisterStorageUnitStatement.class), connectionSession);
        Plugins.getMemberAccessor().set(handler.getClass().getDeclaredField("validateHandler"), handler, mock(DataSourcePropertiesValidateHandler.class));
    }
    
    @Test
    void assertExecute() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        ResponseHeader responseHeader = handler.execute("foo_db", createRegisterStorageUnitStatement());
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNamesInStatement() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(DuplicateStorageUnitException.class, () -> handler.execute("foo_db", createRegisterStorageUnitStatementWithDuplicateStorageUnitNames()));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNamesWithResourceMetaData() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("foo_db").keySet()).thenReturn(Collections.singleton("ds_0"));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(DuplicateStorageUnitException.class, () -> handler.execute("foo_db", createRegisterStorageUnitStatement()));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNamesWithDataSourceContainedRule() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        DataSourceContainedRule rule = mock(DataSourceContainedRule.class);
        when(rule.getDataSourceMapper()).thenReturn(Collections.singletonMap("ds_0", Collections.emptyList()));
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(rule));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        assertThrows(InvalidStorageUnitsException.class, () -> handler.execute("foo_db", createRegisterStorageUnitStatement()));
    }
    
    @Test
    void assertCheckStatementWithIfNotExists() {
        RegisterStorageUnitStatement registerStorageUnitStatementWithIfNotExists = new RegisterStorageUnitStatement(true, Collections.singleton(
                new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "db_1", "root", "", new Properties())));
        handler.checkSQLStatement("foo_db", registerStorageUnitStatementWithIfNotExists);
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
