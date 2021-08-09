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
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropResourceBackendHandlerTest {
    
    @Mock
    private DropResourceStatement dropResourceStatement;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
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
    
    private DropResourceBackendHandler dropResourceBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        dropResourceBackendHandler = new DropResourceBackendHandler(dropResourceStatement, backendConnection);
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        when(metaDataContexts.getMetaData("test")).thenReturn(metaData);
        when(metaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaData.getResource()).thenReturn(resource);
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
        when(shadowRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("test0")));
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("test0", dataSource));
        try {
            dropResourceBackendHandler.execute("test", createDropResourceStatement());
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Resources [test0] in the rule are still in used."));
        }
    }
    
    private DropResourceStatement createDropResourceStatement() {
        return new DropResourceStatement(Collections.singleton("test0"));
    }
}
