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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.bind;

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.infra.binder.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLEmptyStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
class PostgreSQLComBindExecutorTest {
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private PostgreSQLComBindPacket bindPacket;
    
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComBindExecutor executor;
    
    @Test
    void assertExecuteBind() throws SQLException {
        String databaseName = "postgres";
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(new PostgreSQLDatabaseType());
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getDefaultDatabaseName()).thenReturn(databaseName);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        String statementId = "S_1";
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId,
                new PostgreSQLServerPreparedStatement("", new UnknownSQLStatementContext(new PostgreSQLEmptyStatement()), new HintValueContext(), Collections.emptyList(), Collections.emptyList()));
        when(bindPacket.getStatementId()).thenReturn(statementId);
        when(bindPacket.getPortal()).thenReturn("C_1");
        when(bindPacket.readParameters(anyList())).thenReturn(Collections.emptyList());
        when(bindPacket.readResultFormats()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase(databaseName)).thenReturn(database);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(PostgreSQLBindCompletePacket.getInstance()));
        verify(portalContext).add(any(Portal.class));
    }
    
    @Test
    void assertExecuteBindParameters() throws SQLException {
        String databaseName = "postgres";
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(new PostgreSQLDatabaseType());
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getDefaultDatabaseName()).thenReturn(databaseName);
        String statementId = "S_1";
        List<Object> parameters = Arrays.asList(1, "updated_name");
        PostgreSQLServerPreparedStatement serverPreparedStatement = new PostgreSQLServerPreparedStatement("update test set name = $2 where id = $1",
                new UnknownSQLStatementContext(new PostgreSQLEmptyStatement()), new HintValueContext(),
                Arrays.asList(PostgreSQLColumnType.VARCHAR, PostgreSQLColumnType.INT4),
                Arrays.asList(1, 0));
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        when(bindPacket.getStatementId()).thenReturn(statementId);
        when(bindPacket.getPortal()).thenReturn("C_1");
        when(bindPacket.readParameters(anyList())).thenReturn(parameters);
        when(bindPacket.readResultFormats()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase(databaseName)).thenReturn(database);
        executor.execute();
        assertThat(connectionSession.getQueryContext().getParameters(), is(Arrays.asList(parameters.get(1), parameters.get(0))));
    }
}
