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

import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.StorageUnitInUsedException;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.DistSQLException;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.type.UnregisterStorageUnitExecutor;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnregisterStorageUnitExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRule shadowRule;
    
    @Mock
    private SingleRule singleRule;
    
    private ContextManager contextManager;
    
    @Mock
    private ModeContextManager modeContextManager;
    
    private UnregisterStorageUnitExecutor executor;
    
    @BeforeEach
    void setUp() {
        resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.emptyMap());
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/foo_db"));
        StorageUnit storageUnit = new StorageUnit(mock(StorageNode.class), dataSourcePoolProps, new MockedDataSource());
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getName()).thenReturn("foo_db");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        executor = new UnregisterStorageUnitExecutor();
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getInstanceContext().getModeContextManager()).thenReturn(modeContextManager);
        return result;
    }
    
    @Test
    void assertExecute() throws SQLException {
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData().getInUsedStorageUnitNameAndRulesMap()).thenReturn(Collections.emptyMap());
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false);
        executor.setDatabase(database);
        executor.executeUpdate(unregisterStorageUnitStatement, contextManager);
        verify(modeContextManager).unregisterStorageUnits("foo_db", unregisterStorageUnitStatement.getStorageUnitNames());
    }
    
    @Test
    void assertStorageUnitNameNotExistedExecute() {
        when(ProxyContext.getInstance().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        executor.setDatabase(database);
        assertThrows(MissingRequiredStorageUnitsException.class,
                () -> executor.executeUpdate(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false), mock(ContextManager.class)));
    }
    
    @Test
    void assertStorageUnitNameInUseExecute() {
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(shadowRule)));
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
        assertThrows(StorageUnitInUsedException.class, () -> executor.executeUpdate(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false), mock(ContextManager.class)));
    }
    
    @Test
    void assertStorageUnitNameInUseWithoutIgnoreSingleTables() {
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(singleRule)));
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
        assertThrows(StorageUnitInUsedException.class, () -> executor.executeUpdate(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false), mock(ContextManager.class)));
    }
    
    @Test
    void assertStorageUnitNameInUseIgnoreSingleTables() throws SQLException {
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(singleRule)));
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), true, false);
        executor.setDatabase(database);
        executor.executeUpdate(unregisterStorageUnitStatement, contextManager);
        verify(modeContextManager).unregisterStorageUnits("foo_db", unregisterStorageUnitStatement.getStorageUnitNames());
    }
    
    @Test
    void assertExecuteWithIfExists() throws SQLException {
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(true, Collections.singleton("foo_ds"), true, false);
        executor.setDatabase(database);
        executor.executeUpdate(unregisterStorageUnitStatement, contextManager);
        verify(modeContextManager).unregisterStorageUnits("foo_db", unregisterStorageUnitStatement.getStorageUnitNames());
    }
    
    @Test
    void assertStorageUnitNameInUseWithIfExists() {
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(shadowRule)));
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(true, Collections.singleton("foo_ds"), true, false);
        executor.setDatabase(database);
        assertThrows(DistSQLException.class, () -> executor.executeUpdate(unregisterStorageUnitStatement, contextManager));
    }
}
