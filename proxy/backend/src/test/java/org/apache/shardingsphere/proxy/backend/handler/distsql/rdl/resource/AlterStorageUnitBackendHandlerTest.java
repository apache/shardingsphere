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
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterStorageUnitBackendHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private DataSourcePropertiesValidator validator;
    
    @Mock
    private AlterStorageUnitStatement alterStorageUnitStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    @Mock
    private DataSource dataSource;
    
    private AlterStorageUnitBackendHandler alterStorageUnitBackendHandler;
    
    @Before
    public void setUp() throws Exception {
        when(metaDataContexts.getMetaData().getDatabase("test_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().containsDatabase("test_db")).thenReturn(true);
        when(connectionSession.getProtocolType()).thenReturn(new MySQLDatabaseType());
        alterStorageUnitBackendHandler = new AlterStorageUnitBackendHandler(alterStorageUnitStatement, connectionSession);
        Field field = alterStorageUnitBackendHandler.getClass().getDeclaredField("validator");
        field.setAccessible(true);
        field.set(alterStorageUnitBackendHandler, validator);
    }
    
    @Test
    public void assertExecute() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", mockHikariDataSource("ds_0")));
        assertThat(alterStorageUnitBackendHandler.execute("test_db", createAlterStorageUnitStatement("ds_0")), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void assertExecuteWithDuplicateStorageUnitNames() {
        alterStorageUnitBackendHandler.execute("test_db", createAlterStorageUnitStatementWithDuplicateStorageUnitNames());
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertExecuteWithNotExistedStorageUnitNames() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("test_db", database));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", dataSource));
        alterStorageUnitBackendHandler.execute("test_db", createAlterStorageUnitStatement("not_existed"));
    }
    
    @Test(expected = InvalidResourcesException.class)
    public void assertExecuteWithAlterDatabase() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", mockHikariDataSource("ds_1")));
        ResponseHeader responseHeader = alterStorageUnitBackendHandler.execute("test_db", createAlterStorageUnitStatement("ds_0"));
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    private AlterStorageUnitStatement createAlterStorageUnitStatement(final String resourceName) {
        return new AlterStorageUnitStatement(Collections.singleton(new URLBasedDataSourceSegment(resourceName, "jdbc:mysql://127.0.0.1:3306/ds_0", "root", "", new Properties())));
    }
    
    private AlterStorageUnitStatement createAlterStorageUnitStatementWithDuplicateStorageUnitNames() {
        Collection<DataSourceSegment> result = new LinkedList<>();
        result.add(new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()));
        result.add(new URLBasedDataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", "root", "", new Properties()));
        return new AlterStorageUnitStatement(result);
    }
    
    private HikariDataSource mockHikariDataSource(final String database) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:mysql://127.0.0.1:3306/%s?serverTimezone=UTC&useSSL=false", database));
        return result;
    }
}
