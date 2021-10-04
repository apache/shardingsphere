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
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceDefinitionViolationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropResourceBackendHandlerTest {
    
    @Mock
    private DropResourceStatement dropResourceStatement;
    
    @Mock
    private BackendConnection backendConnection;
    
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
    
    private DropResourceBackendHandler dropResourceBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        dropResourceBackendHandler = new DropResourceBackendHandler(dropResourceStatement, backendConnection);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        when(metaDataContexts.getMetaData("test")).thenReturn(metaData);
        when(metaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaData.getResource()).thenReturn(resource);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertExecute() throws DistSQLException {
        when(ruleMetaData.getRules()).thenReturn(Collections.emptyList());
        Map<String, DataSource> dataSources = new HashMap<>(1, 1);
        dataSources.put("test0", dataSource);
        when(resource.getDataSources()).thenReturn(dataSources);
        ResponseHeader responseHeader = dropResourceBackendHandler.execute("test", createDropResourceStatement());
        assertTrue(responseHeader instanceof UpdateResponseHeader);
        assertNull(resource.getDataSources().get("test0"));
    }
    
    @Test
    public void assertResourceNameNotExistedExecute() {
        try {
            dropResourceBackendHandler.execute("test", createDropResourceStatement());
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resources [test0] do not exist in schema test."));
        }
    }
    
    @Test
    public void assertResourceNameInUseExecute() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(shadowRule));
        when(shadowRule.getType()).thenReturn("ShadowRule");
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("test0")));
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("test0", dataSource));
        try {
            dropResourceBackendHandler.execute("test", createDropResourceStatement());
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resource [test0] is still used by [ShadowRule]."));
        }
    }
    
    @Test
    public void assertResourceNameInUseWithoutIgnoreSingleTables() {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(singleTableRule));
        when(singleTableRule.getType()).thenReturn("SingleTableRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("test0");
        when(singleTableRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("test0", dataSource));
        try {
            dropResourceBackendHandler.execute("test", createDropResourceStatement());
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resource [test0] is still used by [SingleTableRule]."));
        }
    }
    
    @Test
    public void assertResourceNameInUseIgnoreSingleTables() throws ResourceDefinitionViolationException {
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(singleTableRule));
        when(singleTableRule.getType()).thenReturn("SingleTableRule");
        DataNode dataNode = mock(DataNode.class);
        when(dataNode.getDataSourceName()).thenReturn("test0");
        when(singleTableRule.getAllDataNodes()).thenReturn(Collections.singletonMap("", Collections.singleton(dataNode)));
        when(resource.getDataSources()).thenReturn(getDataSourceMapForSupportRemove());
        ResponseHeader responseHeader = dropResourceBackendHandler.execute("test", createDropResourceStatementIgnoreSingleTables());
        assertTrue(responseHeader instanceof UpdateResponseHeader);
        assertNull(resource.getDataSources().get("test0"));
    }
    
    private Map<String, DataSource> getDataSourceMapForSupportRemove() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("test0", dataSource);
        return result;
    }
    
    private DropResourceStatement createDropResourceStatement() {
        return new DropResourceStatement(Collections.singleton("test0"), false);
    }
    
    private DropResourceStatement createDropResourceStatementIgnoreSingleTables() {
        return new DropResourceStatement(Collections.singleton("test0"), true);
    }
}
