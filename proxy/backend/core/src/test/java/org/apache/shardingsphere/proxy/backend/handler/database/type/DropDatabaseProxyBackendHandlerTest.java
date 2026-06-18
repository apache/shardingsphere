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

package org.apache.shardingsphere.proxy.backend.handler.database.type;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DropDatabaseProxyBackendHandlerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private DropDatabaseStatement sqlStatement;
    
    private DropDatabaseProxyBackendHandler handler;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(null);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = mockMetaData();
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        handler = new DropDatabaseProxyBackendHandler(sqlStatement, contextManager, connectionSession);
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereDatabase database1 = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database1.getName()).thenReturn("foo_db");
        ShardingSphereDatabase database2 = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database2.getName()).thenReturn("bar_db");
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getAllDatabases()).thenReturn(Arrays.asList(database1, database2));
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.containsDatabase("bar_db")).thenReturn(true);
        when(result.getDatabase("foo_db")).thenReturn(database1);
        when(result.getDatabase("bar_db")).thenReturn(database2);
        when(result.getDatabase("test_not_exist_db")).thenReturn(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        when(result.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mock(AuthorityRule.class))));
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
    void assertExecuteDropWithoutCurrentDatabase() {
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        ResponseHeader responseHeader = handler.execute();
        assertThat(responseHeader, isA(UpdateResponseHeader.class));
        verify(connectionSession, never()).setCurrentDatabaseName(null);
    }
    
    @Test
    void assertExecuteDropCurrentDatabaseWithMySQL() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        ResponseHeader responseHeader = handler.execute();
        assertThat(responseHeader, isA(UpdateResponseHeader.class));
        verify(connectionSession).setCurrentDatabaseName(null);
    }
    
    @Test
    void assertExecuteDropCurrentDatabaseWithPostgreSQL() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(sqlStatement.getDatabaseName()).thenReturn("foo_db");
        assertThrows(UnsupportedOperationException.class, () -> handler.execute());
    }
    
    @Test
    void assertExecuteDropOtherDatabase() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(sqlStatement.getDatabaseName()).thenReturn("bar_db");
        ResponseHeader responseHeader = handler.execute();
        assertThat(responseHeader, isA(UpdateResponseHeader.class));
        verify(connectionSession, never()).setCurrentDatabaseName(null);
    }
}
