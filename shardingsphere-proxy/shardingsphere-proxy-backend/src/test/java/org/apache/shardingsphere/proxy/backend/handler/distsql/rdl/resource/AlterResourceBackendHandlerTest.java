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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterResourceBackendHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private DataSourcePropertiesValidator validator;
    
    @Mock
    private AlterResourceStatement alterResourceStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResource resource;
    
    @Mock
    private DataSource dataSource;
    
    private AlterResourceBackendHandler alterResourceBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        when(metaDataContexts.getMetaData().getDatabase("test_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().containsDatabase("test_db")).thenReturn(true);
        when(connectionSession.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        alterResourceBackendHandler = new AlterResourceBackendHandler(alterResourceStatement, connectionSession);
        Field field = alterResourceBackendHandler.getClass().getDeclaredField("validator");
        field.setAccessible(true);
        field.set(alterResourceBackendHandler, validator);
    }
    
    @Test
    public void assertExecute() throws Exception {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(database.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("ds_0", mockHikariDataSource("ds_0")));
        assertThat(alterResourceBackendHandler.execute("test_db", createAlterResourceStatement("ds_0")), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void assertExecuteWithDuplicateResourceNames() throws DistSQLException {
        alterResourceBackendHandler.execute("test_db", createAlterResourceStatementWithDuplicateResourceNames());
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertExecuteWithNotExistedResourceNames() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("test_db", database));
        when(database.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("ds_0", dataSource));
        alterResourceBackendHandler.execute("test_db", createAlterResourceStatement("not_existed"));
    }
    
    @Test(expected = InvalidResourcesException.class)
    public void assertExecuteWithAlterDatabase() throws Exception {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(database.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("ds_0", mockHikariDataSource("ds_1")));
        ResponseHeader responseHeader = alterResourceBackendHandler.execute("test_db", createAlterResourceStatement("ds_0"));
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    private AlterResourceStatement createAlterResourceStatement(final String resourceName) {
        return new AlterResourceStatement(Collections.singleton(new URLBasedDataSourceSegment(resourceName, "jdbc:mysql://127.0.0.1:3306/ds_0", "root", "", new Properties())));
    }
    
    private AlterResourceStatement createAlterResourceStatementWithDuplicateResourceNames() {
        Collection<DataSourceSegment> result = new LinkedList<>();
        result.add(new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()));
        result.add(new URLBasedDataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", "root", "", new Properties()));
        return new AlterResourceStatement(result);
    }
    
    private HikariDataSource mockHikariDataSource(final String database) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:mysql://127.0.0.1:3306/%s?serverTimezone=UTC&useSSL=false", database));
        return result;
    }
}
