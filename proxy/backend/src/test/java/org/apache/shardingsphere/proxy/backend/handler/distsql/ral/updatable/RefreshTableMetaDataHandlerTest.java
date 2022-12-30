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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RefreshTableMetaDataHandlerTest extends ProxyContextRestorer {
    
    private ConnectionSession connectionSession;
    
    private final ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
    
    private final ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
    
    @Before
    public void setup() {
        ProxyContext.init(contextManager);
        ConfigurationProperties configurationProps = new ConfigurationProperties(createProperties());
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getProps()).thenReturn(configurationProps);
        connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertNoDatabaseSelected() throws SQLException {
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), connectionSession);
        backendHandler.execute();
    }
    
    @Test(expected = UnknownDatabaseException.class)
    public void assertUnknownDatabaseException() throws SQLException {
        when(connectionSession.getDatabaseName()).thenReturn("db");
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), connectionSession);
        backendHandler.execute();
    }
    
    @Test(expected = EmptyStorageUnitException.class)
    public void assertEmptyResource() throws SQLException {
        when(connectionSession.getDatabaseName()).thenReturn("sharding_db");
        when(shardingSphereMetaData.containsDatabase("sharding_db")).thenReturn(true);
        when(contextManager.getDataSourceMap("sharding_db")).thenReturn(Collections.emptyMap());
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), connectionSession);
        backendHandler.execute();
    }
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertMissingRequiredResources() throws SQLException {
        when(connectionSession.getDatabaseName()).thenReturn("sharding_db");
        when(shardingSphereMetaData.containsDatabase("sharding_db")).thenReturn(true);
        Map<String, DataSource> dataSources = createDataSources();
        when(contextManager.getDataSourceMap("sharding_db")).thenReturn(dataSources);
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement("t_order", "ds_1", null), connectionSession);
        backendHandler.execute();
    }
    
    @Test
    public void assertUpdate() throws SQLException {
        when(connectionSession.getDatabaseName()).thenReturn("sharding_db");
        when(shardingSphereMetaData.containsDatabase("sharding_db")).thenReturn(true);
        Map<String, DataSource> dataSources = createDataSources();
        when(contextManager.getDataSourceMap("sharding_db")).thenReturn(dataSources);
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), connectionSession);
        ResponseHeader actual = backendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put("ds_0", new MockedDataSource());
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE.getKey(), ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE.getDefaultValue());
        return result;
    }
}
