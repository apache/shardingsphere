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

package org.apache.shardingsphere.proxy.backend.handler.database;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseOperateBackendHandlerFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(null);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
    }
    
    @AfterEach
    void tearDown() {
        setGovernanceMetaDataContexts(false);
    }
    
    @Test
    void assertExecuteCreateDatabaseContext() throws SQLException {
        CreateDatabaseStatement sqlStatement = mock(CreateDatabaseStatement.class);
        when(sqlStatement.getDatabaseName()).thenReturn("new_db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropDatabaseContext() throws SQLException {
        DropDatabaseStatement sqlStatement = mock(DropDatabaseStatement.class);
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteCreateDatabaseContextWithException() {
        CreateDatabaseStatement sqlStatement = mock(CreateDatabaseStatement.class);
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        setGovernanceMetaDataContexts(true);
        try {
            DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession);
        } catch (final DatabaseCreateExistsException ex) {
            assertNull(ex.getMessage());
        }
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        return result;
    }
    
    private void setGovernanceMetaDataContexts(final boolean isGovernance) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData();
        MetaDataContexts metaDataContexts = isGovernance ? mockMetaDataContexts() : new MetaDataContexts(metaData, new ShardingSphereStatistics());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        when(result.getMetaData().getDatabase("foo_db").getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    void assertDatabaseOperateBackendHandlerFactoryReturnCreateDatabaseBackendHandler() {
        assertThat(DatabaseOperateBackendHandlerFactory.newInstance(mock(CreateDatabaseStatement.class), mock(ConnectionSession.class)), instanceOf(CreateDatabaseBackendHandler.class));
    }
    
    @Test
    void assertDatabaseOperateBackendHandlerFactoryReturnDropDatabaseBackendHandler() {
        assertThat(DatabaseOperateBackendHandlerFactory.newInstance(mock(DropDatabaseStatement.class), mock(ConnectionSession.class)), instanceOf(DropDatabaseBackendHandler.class));
    }
    
    @Test
    void assertDatabaseOperateBackendHandlerFactoryThrowUnsupportedOperationException() {
        assertThrows(UnsupportedSQLOperationException.class, () -> DatabaseOperateBackendHandlerFactory.newInstance(mock(AlterDatabaseStatement.class), mock(ConnectionSession.class)));
    }
}
