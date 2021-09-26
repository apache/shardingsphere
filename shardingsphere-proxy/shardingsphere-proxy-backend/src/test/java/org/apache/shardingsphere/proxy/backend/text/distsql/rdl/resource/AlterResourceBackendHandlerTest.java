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
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterResourceBackendHandlerTest {
    
    @Mock
    private DataSourceValidator dataSourceValidator;
    
    @Mock
    private AlterResourceStatement alterResourceStatement;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    @Mock
    private DataSource dataSource;
    
    private AlterResourceBackendHandler alterResourceBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        alterResourceBackendHandler = new AlterResourceBackendHandler(new MySQLDatabaseType(), alterResourceStatement, backendConnection);
        Field field = alterResourceBackendHandler.getClass().getDeclaredField("dataSourceValidator");
        field.setAccessible(true);
        field.set(alterResourceBackendHandler, dataSourceValidator);
    }
    
    @Test
    public void assertExecute() throws Exception {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test_schema"));
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(metaData);
        when(metaData.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("ds_0", dataSource));
        when(dataSourceValidator.validate(any(DataSourceConfiguration.class))).thenReturn(true);
        ResponseHeader responseHeader = alterResourceBackendHandler.execute("test_schema", createAlterResourceStatement("ds_0"));
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void assertExecuteWithDuplicateResourceNames() throws DistSQLException {
        alterResourceBackendHandler.execute("test_schema", createAlterResourceStatementWithDuplicateResourceNames());
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertExecuteWithNotExistedResourceNames() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test_schema"));
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(metaData);
        when(metaData.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("ds_0", dataSource));
        alterResourceBackendHandler.execute("test_schema", createAlterResourceStatement("not_existed"));
    }
    
    private AlterResourceStatement createAlterResourceStatement(final String resourceName) {
        return new AlterResourceStatement(Collections.singleton(new DataSourceSegment(resourceName, "jdbc:mysql://127.0.0.1:3306/ds_0", null, null, null, "root", "", new Properties())));
    }
    
    private AlterResourceStatement createAlterResourceStatementWithDuplicateResourceNames() {
        List<DataSourceSegment> result = new LinkedList<>();
        result.add(new DataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_0", null, null, null, "root", "", new Properties()));
        result.add(new DataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", null, null, null, "root", "", new Properties()));
        return new AlterResourceStatement(result);
    }
}
