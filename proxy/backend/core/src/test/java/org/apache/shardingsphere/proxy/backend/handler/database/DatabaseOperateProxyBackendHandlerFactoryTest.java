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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.database.type.CreateDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.database.type.DropDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseOperateProxyBackendHandlerFactoryTest {
    
    private final ShardingSphereMetaData metaData = new ShardingSphereMetaData(
            Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(null);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
    }
    
    @Test
    void assertExecuteCreateDatabaseContext() throws SQLException {
        CreateDatabaseStatement sqlStatement = mock(CreateDatabaseStatement.class);
        when(sqlStatement.getDatabaseName()).thenReturn("new_db");
        ResponseHeader response = DatabaseOperateProxyBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropDatabaseContext() throws SQLException {
        DropDatabaseStatement sqlStatement = mock(DropDatabaseStatement.class);
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        ResponseHeader response = DatabaseOperateProxyBackendHandlerFactory.newInstance(sqlStatement, connectionSession).execute();
        assertThat(response, isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteCreateDatabaseContextWithException() {
        CreateDatabaseStatement sqlStatement = mock(CreateDatabaseStatement.class);
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        try {
            DatabaseOperateProxyBackendHandlerFactory.newInstance(sqlStatement, connectionSession);
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
    
    @Test
    void assertDatabaseOperateProxyBackendHandlerFactoryReturnCreateDatabaseProxyBackendHandler() {
        assertThat(DatabaseOperateProxyBackendHandlerFactory.newInstance(mock(CreateDatabaseStatement.class), mock(ConnectionSession.class)), isA(CreateDatabaseProxyBackendHandler.class));
    }
    
    @Test
    void assertDatabaseOperateProxyBackendHandlerFactoryReturnDropDatabaseProxyBackendHandler() {
        assertThat(DatabaseOperateProxyBackendHandlerFactory.newInstance(mock(DropDatabaseStatement.class), mock(ConnectionSession.class)), isA(DropDatabaseProxyBackendHandler.class));
    }
    
    @Test
    void assertDatabaseOperateProxyBackendHandlerFactoryThrowUnsupportedOperationException() {
        assertThrows(UnsupportedSQLOperationException.class, () -> DatabaseOperateProxyBackendHandlerFactory.newInstance(mock(AlterDatabaseStatement.class), mock(ConnectionSession.class)));
    }
}
