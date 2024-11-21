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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.infra.exception.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.infra.exception.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.StandardDatabaseConnector;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLSetVariableAdminExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        SetStatement setStatement = prepareSetStatement();
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getAttributeMap()).thenReturn(new DefaultAttributeMap());
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedConstruction<StandardDatabaseConnector> mockConstruction = mockConstruction(StandardDatabaseConnector.class)) {
            executor.execute(connectionSession);
            verify(mockConstruction.constructed().get(0)).execute();
        }
        assertThat(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get(), is(StandardCharsets.UTF_8));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private SetStatement prepareSetStatement() {
        VariableSegment maxConnectionVariableSegment = new VariableSegment(0, 0, "max_connections", "global");
        VariableAssignSegment setGlobalMaxConnectionAssignSegment = new VariableAssignSegment(0, 0, maxConnectionVariableSegment, "151");
        VariableSegment characterSetClientSegment = new VariableSegment(0, 0, "character_set_client");
        VariableAssignSegment setCharacterSetClientVariableSegment = new VariableAssignSegment(0, 0, characterSetClientSegment, "'utf8mb4'");
        SetStatement result = new MySQLSetStatement();
        result.getVariableAssigns().add(setGlobalMaxConnectionAssignSegment);
        result.getVariableAssigns().add(setCharacterSetClientVariableSegment);
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new RuleMetaData(Collections.singleton(new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(1, 1L), new CacheOption(1, 1L))))));
        return result;
    }
    
    @Test
    void assertSetUnknownSystemVariable() {
        SetStatement setStatement = new MySQLSetStatement();
        setStatement.getVariableAssigns().add(new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "unknown_variable"), ""));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        assertThrows(UnknownSystemVariableException.class, () -> executor.execute(mock(ConnectionSession.class)));
    }
    
    @Test
    void assertSetVariableWithIncorrectScope() {
        SetStatement setStatement = new MySQLSetStatement();
        setStatement.getVariableAssigns().add(new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "max_connections"), ""));
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        assertThrows(ErrorGlobalVariableException.class, () -> executor.execute(mock(ConnectionSession.class)));
    }
}
