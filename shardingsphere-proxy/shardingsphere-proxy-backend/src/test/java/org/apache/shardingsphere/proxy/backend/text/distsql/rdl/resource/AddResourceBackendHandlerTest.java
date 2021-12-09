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

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfigurationValidator;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AddResourceBackendHandlerTest {
    
    @Mock
    private DataSourceConfigurationValidator dataSourceConfigurationValidator;
    
    @Mock
    private AddResourceStatement addResourceStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    private AddResourceBackendHandler addResourceBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        addResourceBackendHandler = new AddResourceBackendHandler(new MySQLDatabaseType(), addResourceStatement, connectionSession);
        Field field = addResourceBackendHandler.getClass().getDeclaredField("dataSourceConfigValidator");
        field.setAccessible(true);
        field.set(addResourceBackendHandler, dataSourceConfigurationValidator);
    }
    
    @Test
    public void assertExecute() throws Exception {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test_schema"));
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(metaData);
        when(metaData.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.emptyMap());
        ResponseHeader responseHeader = addResourceBackendHandler.execute("test_schema", createAddResourceStatement());
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void assertExecuteWithDuplicateResourceNames() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test_schema"));
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(metaData);
        when(metaData.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.emptyMap());
        addResourceBackendHandler.execute("test_schema", createAlterResourceStatementWithDuplicateResourceNames());
    }
    
    private AddResourceStatement createAddResourceStatement() {
        return new AddResourceStatement(Collections.singleton(new DataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/test0", null, null, null, "root", "", new Properties())));
    }
    
    private AddResourceStatement createAlterResourceStatementWithDuplicateResourceNames() {
        List<DataSourceSegment> result = new LinkedList<>();
        result.add(new DataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_0", null, null, null, "root", "", new Properties()));
        result.add(new DataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", null, null, null, "root", "", new Properties()));
        return new AddResourceStatement(result);
    }
}
