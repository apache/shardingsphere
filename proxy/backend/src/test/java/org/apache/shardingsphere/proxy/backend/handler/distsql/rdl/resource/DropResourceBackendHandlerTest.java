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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceInUsedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropResourceBackendHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private DropResourceStatement dropResourceStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    @Mock
    private ShadowRule shadowRule;
    
    @Mock
    private SingleTableRule singleTableRule;
    
    private ContextManager contextManager;
    
    private DropResourceBackendHandler dropResourceBackendHandler;
    
    @Before
    public void setUp() {
        resourceMetaData = mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("test", database));
        when(metaDataContexts.getMetaData().containsDatabase("test")).thenReturn(true);
        contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        dropResourceBackendHandler = new DropResourceBackendHandler(dropResourceStatement, connectionSession);
    }
    
    @Test
    public void assertExecute() throws SQLException {
        when(ruleMetaData.getRules()).thenReturn(Collections.emptyList());
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("test")).thenReturn(database);
        DropResourceStatement dropResourceStatement = new DropResourceStatement(Collections.singleton("foo_ds"), false);
        assertThat(dropResourceBackendHandler.execute("test", dropResourceStatement), instanceOf(UpdateResponseHeader.class));
        verify(contextManager).dropResources("test", dropResourceStatement.getNames());
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertResourceNameNotExistedExecute() {
        dropResourceBackendHandler.execute("test", new DropResourceStatement(Collections.singleton("foo_ds"), false));
    }
    
    @Test(expected = ResourceInUsedException.class)
    public void assertResourceNameInUseExecute() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("test")).thenReturn(database);
        dropResourceBackendHandler.execute("test", new DropResourceStatement(Collections.singleton("foo_ds"), false));
    }
    
    @Test(expected = ResourceInUsedException.class)
    public void assertResourceNameInUseWithoutIgnoreSingleTables() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(singleTableRule));
        when(singleTableRule.getType()).thenReturn("SingleTableRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleTableRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("test")).thenReturn(database);
        dropResourceBackendHandler.execute("test", new DropResourceStatement(Collections.singleton("foo_ds"), false));
    }
    
    @Test
    public void assertResourceNameInUseIgnoreSingleTables() throws SQLException {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(singleTableRule));
        when(singleTableRule.getType()).thenReturn("SingleTableRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleTableRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("test")).thenReturn(database);
        DropResourceStatement dropResourceStatement = new DropResourceStatement(Collections.singleton("foo_ds"), true);
        assertThat(dropResourceBackendHandler.execute("test", dropResourceStatement), instanceOf(UpdateResponseHeader.class));
        verify(contextManager).dropResources("test", dropResourceStatement.getNames());
    }
    
    @Test
    public void assertExecuteWithIfExists() throws SQLException {
        DropResourceStatement dropResourceStatement = new DropResourceStatement(true, Collections.singleton("foo_ds"), true);
        assertThat(dropResourceBackendHandler.execute("test", dropResourceStatement), instanceOf(UpdateResponseHeader.class));
        verify(contextManager).dropResources("test", dropResourceStatement.getNames());
    }
    
    @Test(expected = DistSQLException.class)
    public void assertResourceNameInUseWithIfExists() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("test")).thenReturn(database);
        DropResourceStatement dropResourceStatement = new DropResourceStatement(true, Collections.singleton("foo_ds"), true);
        dropResourceBackendHandler.execute("test", dropResourceStatement);
    }
}
