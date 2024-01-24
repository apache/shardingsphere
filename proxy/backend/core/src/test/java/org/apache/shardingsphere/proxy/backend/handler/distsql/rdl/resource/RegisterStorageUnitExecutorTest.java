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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource;

import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidateHandler;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.type.RegisterStorageUnitExecutor;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterStorageUnitExecutorTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    private RegisterStorageUnitExecutor executor;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        executor = new RegisterStorageUnitExecutor();
        Plugins.getMemberAccessor().set(executor.getClass().getDeclaredField("validateHandler"), executor, mock(DataSourcePoolPropertiesValidateHandler.class));
    }
    
    @Test
    void assertExecute() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.setDatabase(database);
        assertDoesNotThrow(() -> executor.execute(createRegisterStorageUnitStatement()));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNamesInStatement() {
        executor.setDatabase(database);
        assertThrows(DuplicateStorageUnitException.class, () -> executor.execute(createRegisterStorageUnitStatementWithDuplicateStorageUnitNames()));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNamesWithResourceMetaData() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStorageUnits("foo_db").keySet()).thenReturn(Collections.singleton("ds_0"));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.setDatabase(database);
        assertThrows(DuplicateStorageUnitException.class, () -> executor.execute(createRegisterStorageUnitStatement()));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNamesWithDataSourceContainedRule() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        DataSourceContainedRule rule = mock(DataSourceContainedRule.class);
        when(rule.getDataSourceMapper()).thenReturn(Collections.singletonMap("ds_0", Collections.emptyList()));
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(rule));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.setDatabase(database);
        assertThrows(InvalidStorageUnitsException.class, () -> executor.execute(createRegisterStorageUnitStatement()));
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
