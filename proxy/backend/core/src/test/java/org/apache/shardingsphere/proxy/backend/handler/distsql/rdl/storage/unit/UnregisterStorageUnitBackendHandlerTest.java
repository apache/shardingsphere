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

import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.DistSQLException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.StorageUnitInUsedException;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnregisterStorageUnitBackendHandlerTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private RuleMetaData ruleMetaData;
    
    @Mock
    private ShadowRule shadowRule;
    
    @Mock
    private SingleRule singleRule;
    
    private ContextManager contextManager;
    
    @Mock
    private ModeContextManager modeContextManager;
    
    private UnregisterStorageUnitBackendHandler handler;
    
    @BeforeEach
    void setUp() {
        resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        handler = new UnregisterStorageUnitBackendHandler(mock(UnregisterStorageUnitStatement.class), mock(ConnectionSession.class));
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", database));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getInstanceContext().getModeContextManager()).thenReturn(modeContextManager);
        return result;
    }
    
    @Test
    void assertExecute() throws SQLException {
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false);
        assertThat(handler.execute("foo_db", unregisterStorageUnitStatement), instanceOf(UpdateResponseHeader.class));
        verify(modeContextManager).unregisterStorageUnits("foo_db", unregisterStorageUnitStatement.getStorageUnitNames());
    }
    
    @Test
    void assertStorageUnitNameNotExistedExecute() {
        when(ProxyContext.getInstance().getDatabase("foo_db").getResourceMetaData().getDataSources()).thenReturn(Collections.emptyMap());
        assertThrows(MissingRequiredStorageUnitsException.class,
                () -> handler.execute("foo_db", new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false)));
    }
    
    @Test
    void assertStorageUnitNameInUseExecute() {
        when(ruleMetaData.findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        assertThrows(StorageUnitInUsedException.class,
                () -> handler.execute("foo_db", new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false)));
    }
    
    @Test
    void assertStorageUnitNameInUseWithoutIgnoreSingleTables() {
        when(ruleMetaData.findRules(DataNodeContainedRule.class)).thenReturn(Collections.singleton(singleRule));
        when(singleRule.getType()).thenReturn("SingleRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        assertThrows(StorageUnitInUsedException.class,
                () -> handler.execute("foo_db", new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false)));
    }
    
    @Test
    void assertStorageUnitNameInUseIgnoreSingleTables() throws SQLException {
        when(ruleMetaData.findRules(DataNodeContainedRule.class)).thenReturn(Collections.singleton(singleRule));
        when(singleRule.getType()).thenReturn("SingleRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), true);
        assertThat(handler.execute("foo_db", unregisterStorageUnitStatement), instanceOf(UpdateResponseHeader.class));
        verify(modeContextManager).unregisterStorageUnits("foo_db", unregisterStorageUnitStatement.getStorageUnitNames());
    }
    
    @Test
    void assertExecuteWithIfExists() throws SQLException {
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(true, Collections.singleton("foo_ds"), true);
        assertThat(handler.execute("foo_db", unregisterStorageUnitStatement), instanceOf(UpdateResponseHeader.class));
        verify(modeContextManager).unregisterStorageUnits("foo_db", unregisterStorageUnitStatement.getStorageUnitNames());
    }
    
    @Test
    void assertStorageUnitNameInUseWithIfExists() {
        when(ruleMetaData.findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        UnregisterStorageUnitStatement unregisterStorageUnitStatement = new UnregisterStorageUnitStatement(true, Collections.singleton("foo_ds"), true);
        assertThrows(DistSQLException.class, () -> handler.execute("foo_db", unregisterStorageUnitStatement));
    }
}
