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

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DropDatabaseBackendHandlerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private DropDatabaseStatement sqlStatement;
    
    private DropDatabaseBackendHandler handler;
    
    @BeforeEach
    void setUp() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        when(ProxyContext.getInstance().databaseExists("bar_db")).thenReturn(true);
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(null);
        handler = new DropDatabaseBackendHandler(sqlStatement, connectionSession);
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereDatabase database1 = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database1.getName()).thenReturn("foo_db");
        ShardingSphereDatabase database2 = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database2.getName()).thenReturn("bar_db");
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Arrays.asList(database1, database2));
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database1);
        when(metaDataContexts.getMetaData().getDatabase("bar_db")).thenReturn(database2);
        when(metaDataContexts.getMetaData().getDatabase("test_not_exist_db")).thenReturn(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mock(AuthorityRule.class))));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    void assertExecuteDropNotExistDatabase() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_not_exist_db");
        assertThrows(DatabaseDropNotExistsException.class, () -> handler.execute());
    }
    
    @Test
    void assertExecuteDropNotExistDatabaseWithIfExists() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_not_exist_db");
        when(sqlStatement.isIfExists()).thenReturn(true);
        assertDoesNotThrow(() -> handler.execute());
    }
    
    @Test
    void assertExecuteDropWithoutCurrentDatabase() throws SQLException {
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        ResponseHeader responseHeader = handler.execute();
        verify(connectionSession, times(0)).setCurrentDatabaseName(null);
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropCurrentDatabaseWithMySQL() throws SQLException {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        ResponseHeader responseHeader = handler.execute();
        verify(connectionSession).setCurrentDatabaseName(null);
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropCurrentDatabaseWithPostgreSQL() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        assertThrows(UnsupportedOperationException.class, () -> handler.execute());
    }
    
    @Test
    void assertExecuteDropOtherDatabase() throws SQLException {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(sqlStatement.getDatabaseName()).thenReturn("bar_db");
        ResponseHeader responseHeader = handler.execute();
        verify(connectionSession, times(0)).setCurrentDatabaseName(null);
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
}
