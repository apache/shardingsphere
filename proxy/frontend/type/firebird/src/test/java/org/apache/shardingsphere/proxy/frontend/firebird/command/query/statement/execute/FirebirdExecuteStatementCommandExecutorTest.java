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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute;

import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdFetchResponsePacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdSQLResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.test.infra.framework.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyBackendHandlerFactory.class, ProxyContext.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdExecuteStatementCommandExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Mock
    private FirebirdExecuteStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private ConnectionContext connectionContext;
    
    @Mock
    private QueryHeader queryHeader;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpdateStatementContext updateContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SelectStatementContext selectContext;
    
    @BeforeEach
    void setUp() {
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(new ShardingSphereMetaData());
        when(selectContext.getSqlStatement()).thenReturn(new org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement(databaseType));
        registry.addPreparedStatement(1, new FirebirdServerPreparedStatement("SELECT * FROM tbl", selectContext, new HintValueContext()));
        when(updateContext.getSqlStatement()).thenReturn(new org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement(databaseType));
        registry.addPreparedStatement(2, new FirebirdServerPreparedStatement("UPDATE tbl SET col=1", updateContext, new HintValueContext()));
    }
    
    @Test
    void assertIsQueryResponse() throws SQLException {
        when(packet.getStatementId()).thenReturn(1);
        when(packet.getParameterTypes()).thenReturn(Collections.emptyList());
        when(packet.getParameterValues()).thenReturn(new ArrayList<>());
        when(packet.isStoredProcedure()).thenReturn(true);
        FirebirdExecuteStatementCommandExecutor executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(queryHeader)));
        when(proxyBackendHandler.next()).thenReturn(true, true);
        QueryResponseRow row = new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1)));
        when(proxyBackendHandler.getRowData()).thenReturn(row, row);
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> iterator = actual.iterator();
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
        assertThat(iterator.next(), instanceOf(FirebirdSQLResponsePacket.class));
        assertThat(iterator.next(), instanceOf(FirebirdGenericResponsePacket.class));
        assertFalse(iterator.hasNext());
        assertTrue(executor.next());
        FirebirdPacket rowPacket = executor.getQueryRowPacket();
        assertThat(rowPacket, instanceOf(FirebirdFetchResponsePacket.class));
        executor.close();
        verify(proxyBackendHandler).close();
    }
    
    @Test
    void assertIsUpdateResponse() throws SQLException {
        when(packet.getStatementId()).thenReturn(2);
        when(packet.getParameterTypes()).thenReturn(Collections.emptyList());
        when(packet.getParameterValues()).thenReturn(new ArrayList<>());
        FirebirdExecuteStatementCommandExecutor executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement(databaseType)));
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(actual.iterator().next(), instanceOf(FirebirdGenericResponsePacket.class));
    }
}
