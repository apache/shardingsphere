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

package org.apache.shardingsphere.proxy.backend.text.admin;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizerContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.database.DatabaseOperateBackendHandlerFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseOperateBackendHandlerFactoryTest {
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), getMetaDataMap(), 
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContext.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertExecuteMySQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new PostgreSQLCreateDatabaseStatement());
    }
    
    private void assertExecuteCreateDatabaseContext(final CreateDatabaseStatement sqlStatement) throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        sqlStatement.setDatabaseName("new_db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteMySQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new MySQLDropDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new PostgreSQLDropDatabaseStatement());
    }
    
    private void assertExecuteDropDatabaseContext(final DropDatabaseStatement sqlStatement) throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        sqlStatement.setDatabaseName("schema");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteMySQLCreateDatabaseContextWithException() throws SQLException {
        assertExecuteCreateDatabaseContextWithException(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLCreateDatabaseContextWithException() throws SQLException {
        assertExecuteCreateDatabaseContextWithException(new PostgreSQLCreateDatabaseStatement());
    }
    
    public void assertExecuteCreateDatabaseContextWithException(final CreateDatabaseStatement sqlStatement) throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        sqlStatement.setDatabaseName("schema");
        setGovernanceMetaDataContexts(true);
        try {
            DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connection);
        } catch (final DBCreateExistsException ex) {
            assertNull(ex.getMessage());
        }
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap("schema", metaData);
    }
    
    private void setGovernanceMetaDataContexts(final boolean isGovernance) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = isGovernance ? mockMetaDataContexts() : new MetaDataContexts(mock(MetaDataPersistService.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(result.getAllSchemaNames()).thenReturn(Collections.singletonList("schema"));
        when(result.getMetaData("schema").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(result.getMetaData("schema").getResource().getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getMetaData("schema").getResource().getNotExistedResources(any())).thenReturn(Collections.emptyList());
        return result;
    }
    
    @After
    public void setDown() {
        setGovernanceMetaDataContexts(false);
    }
}
