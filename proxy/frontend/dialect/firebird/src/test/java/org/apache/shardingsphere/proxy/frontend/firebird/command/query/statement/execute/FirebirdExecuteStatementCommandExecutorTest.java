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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdSQLResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
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
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload.FirebirdBlobUploadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyBackendHandlerFactory.class, ProxyContext.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdExecuteStatementCommandExecutorTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
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
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobUploadCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementId()).thenReturn(STATEMENT_ID);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(new ShardingSphereMetaData(Collections.emptyList(),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())));
        when(selectContext.getSqlStatement()).thenReturn(new SelectStatement(DATABASE_TYPE));
        registry.addPreparedStatement(1, new FirebirdServerPreparedStatement("SELECT * FROM tbl", selectContext, new HintValueContext()));
        when(updateContext.getSqlStatement()).thenReturn(new UpdateStatement(DATABASE_TYPE));
        registry.addPreparedStatement(2, new FirebirdServerPreparedStatement("UPDATE tbl SET col=1", updateContext, new HintValueContext()));
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobUploadCache.getInstance().unregisterConnection(CONNECTION_ID);
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
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> iterator = actual.iterator();
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
        assertThat(iterator.next(), isA(FirebirdSQLResponsePacket.class));
        assertThat(iterator.next(), isA(FirebirdGenericResponsePacket.class));
        assertFalse(iterator.hasNext());
        assertThat(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID), is(proxyBackendHandler));
    }
    
    @Test
    void assertIsUpdateResponse() throws SQLException {
        when(packet.getStatementId()).thenReturn(2);
        when(packet.getParameterTypes()).thenReturn(Collections.emptyList());
        when(packet.getParameterValues()).thenReturn(new ArrayList<>());
        FirebirdExecuteStatementCommandExecutor executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new UpdateStatement(DATABASE_TYPE)));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        assertThat(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID), nullValue());
    }
    
    @Test
    void assertSkipUnclosedBlobParameter() throws SQLException {
        int blobHandle = 7;
        long blobId = 11L;
        FirebirdBlobUploadCache.getInstance().registerBlob(CONNECTION_ID, blobHandle, blobId);
        FirebirdBlobUploadCache.getInstance().appendSegment(CONNECTION_ID, blobHandle, new byte[]{1, 2});
        List<Object> params = new ArrayList<>();
        params.add(blobId);
        when(packet.getStatementId()).thenReturn(2);
        when(packet.getParameterTypes()).thenReturn(Collections.singletonList(FirebirdBinaryColumnType.BLOB));
        when(packet.getParameterValues()).thenReturn(params);
        FirebirdExecuteStatementCommandExecutor executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new UpdateStatement(DATABASE_TYPE)));
        ArgumentCaptor<QueryContext> queryContextCaptor = ArgumentCaptor.forClass(QueryContext.class);
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), queryContextCaptor.capture(), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        executor.execute();
        List<Object> actualParams = queryContextCaptor.getValue().getParameters();
        assertThat(actualParams.size(), is(1));
        assertThat(actualParams.get(0), nullValue());
    }
}
