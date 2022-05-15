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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource;

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropResourceBackendHandlerTest {
    
    @Mock
    private DropResourceStatement dropResourceStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereResource resource;
    
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
    public void setUp() throws Exception {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getAllDatabaseNames()).thenReturn(Collections.singleton("test"));
        when(metaDataContexts.getMetaData("test")).thenReturn(metaData);
        when(metaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaData.getResource()).thenReturn(resource);
        contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        dropResourceBackendHandler = new DropResourceBackendHandler(dropResourceStatement, connectionSession);
    }
    
    @Test
    public void assertExecute() throws DistSQLException {
        when(ruleMetaData.getRules()).thenReturn(Collections.emptyList());
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        DropResourceStatement dropResourceStatement = new DropResourceStatement(Collections.singleton("foo_ds"), false);
        assertThat(dropResourceBackendHandler.execute("test", dropResourceStatement), instanceOf(UpdateResponseHeader.class));
        verify(contextManager).dropResource("test", dropResourceStatement.getNames());
    }
    
    @Test
    public void assertResourceNameNotExistedExecute() {
        try {
            dropResourceBackendHandler.execute("test", new DropResourceStatement(Collections.singleton("foo_ds"), false));
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resources [foo_ds] do not exist in database test."));
        }
    }
    
    @Test
    public void assertResourceNameInUseExecute() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        try {
            dropResourceBackendHandler.execute("test", new DropResourceStatement(Collections.singleton("foo_ds"), false));
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resource [foo_ds] is still used by [ShadowRule]."));
        }
    }
    
    @Test
    public void assertResourceNameInUseWithoutIgnoreSingleTables() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(singleTableRule));
        when(singleTableRule.getType()).thenReturn("SingleTableRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleTableRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        try {
            dropResourceBackendHandler.execute("test", new DropResourceStatement(Collections.singleton("foo_ds"), false));
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resource [foo_ds] is still used by [SingleTableRule]."));
        }
    }
    
    @Test
    public void assertResourceNameInUseIgnoreSingleTables() throws DistSQLException {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(singleTableRule));
        when(singleTableRule.getType()).thenReturn("SingleTableRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("foo_ds");
        when(singleTableRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        DropResourceStatement dropResourceStatement = new DropResourceStatement(Collections.singleton("foo_ds"), true);
        assertThat(dropResourceBackendHandler.execute("test", dropResourceStatement), instanceOf(UpdateResponseHeader.class));
        verify(contextManager).dropResource("test", dropResourceStatement.getNames());
    }
    
    @Test
    public void assertExecuteWithIfExists() throws DistSQLException {
        DropResourceStatement dropResourceStatement = new DropResourceStatement(true, Collections.singleton("foo_ds"), true);
        assertThat(dropResourceBackendHandler.execute("test", dropResourceStatement), instanceOf(UpdateResponseHeader.class));
        verify(contextManager).dropResource("test", dropResourceStatement.getNames());
    }
    
    @Test(expected = DistSQLException.class)
    public void assertResourceNameInUseWithIfExists() throws DistSQLException {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        DropResourceStatement dropResourceStatement = new DropResourceStatement(true, Collections.singleton("foo_ds"), true);
        dropResourceBackendHandler.execute("test", dropResourceStatement);
    }
}
