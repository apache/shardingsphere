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
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
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
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
public final class PostgreSQLComBindExecutorTest {
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private PostgreSQLComBindPacket bindPacket;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComBindExecutor executor;
    
    @Test
    public void assertExecuteBind() throws SQLException {
        String databaseName = "postgres";
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(new PostgreSQLDatabaseType());
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(connectionSession.getDefaultDatabaseName()).thenReturn(databaseName);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        String statementId = "S_1";
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId,
                new PostgreSQLServerPreparedStatement("", new CommonSQLStatementContext<>(new PostgreSQLEmptyStatement()), Collections.emptyList()));
        when(bindPacket.getStatementId()).thenReturn(statementId);
        when(bindPacket.getPortal()).thenReturn("C_1");
        when(bindPacket.readParameters(anyList())).thenReturn(Collections.emptyList());
        when(bindPacket.readResultFormats()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase(databaseName)).thenReturn(database);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(PostgreSQLBindCompletePacket.getInstance()));
        verify(portalContext).add(any(Portal.class));
    }
}
