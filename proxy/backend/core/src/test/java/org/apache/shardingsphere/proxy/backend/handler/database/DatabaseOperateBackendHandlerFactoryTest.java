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

import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
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
import java.util.Map;
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
    
    @Mock
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereResourceMetaData.class), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
    }
    
    @AfterEach
    void tearDown() {
        setGovernanceMetaDataContexts(false);
    }
    
    @Test
    void assertExecuteMySQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    void assertExecutePostgreSQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new PostgreSQLCreateDatabaseStatement());
    }
    
    private void assertExecuteCreateDatabaseContext(final CreateDatabaseStatement sqlStatement) throws SQLException {
        sqlStatement.setDatabaseName("new_db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteMySQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new MySQLDropDatabaseStatement());
    }
    
    @Test
    void assertExecutePostgreSQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new PostgreSQLDropDatabaseStatement());
    }
    
    private void assertExecuteDropDatabaseContext(final DropDatabaseStatement sqlStatement) throws SQLException {
        sqlStatement.setDatabaseName("foo_db");
        setGovernanceMetaDataContexts(true);
        ResponseHeader response = DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteMySQLCreateDatabaseContextWithException() {
        assertExecuteCreateDatabaseContextWithException(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    void assertExecutePostgreSQLCreateDatabaseContextWithException() {
        assertExecuteCreateDatabaseContextWithException(new PostgreSQLCreateDatabaseStatement());
    }
    
    private void assertExecuteCreateDatabaseContextWithException(final CreateDatabaseStatement sqlStatement) {
        sqlStatement.setDatabaseName("foo_db");
        setGovernanceMetaDataContexts(true);
        try {
            DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession);
        } catch (final DatabaseCreateExistsException ex) {
            assertNull(ex.getMessage());
        }
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("foo_db", database);
    }
    
    private void setGovernanceMetaDataContexts(final boolean isGovernance) {
        MetaDataContexts metaDataContexts = isGovernance ? mockMetaDataContexts() : new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        when(result.getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources()).thenReturn(Collections.emptyMap());
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
