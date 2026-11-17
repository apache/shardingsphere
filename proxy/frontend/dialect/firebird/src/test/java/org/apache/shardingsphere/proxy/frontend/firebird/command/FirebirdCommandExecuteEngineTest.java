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

package org.apache.shardingsphere.proxy.frontend.firebird.command;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureAssertion;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({AutoMockExtension.class, LogCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({
        FirebirdCommandPacketFactory.class,
        FirebirdConnectionProtocolVersion.class,
        FirebirdCommandExecutorFactory.class,
        FirebirdErrorPacketFactory.class,
        ProxyContext.class
})
class FirebirdCommandExecuteEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private Channel channel;
    
    @Mock
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private FirebirdConnectionProtocolVersion connectionProtocolVersion;
    
    @Mock
    private CommandExecutor executor;
    
    @Mock
    private CommandPacket packet;
    
    @Mock
    private ConnectionResourceLock connectionResourceLock;
    
    @Mock
    private FirebirdPacket errorPacket;
    
    @Mock
    private FirebirdCommandPacket commandPacket;
    
    @Mock
    private DatabasePacket rowPacket;
    
    private FirebirdCommandExecuteEngine engine;
    
    private Logger commandLogger;
    
    private Level originalLevel;
    
    @BeforeEach
    void setUp() {
        engine = new FirebirdCommandExecuteEngine();
        when(context.channel()).thenReturn(channel);
        commandLogger = (Logger) LoggerFactory.getLogger(FirebirdCommandExecuteEngine.class);
        originalLevel = commandLogger.getLevel();
        commandLogger.setLevel(Level.INFO);
    }
    
    @AfterEach
    void tearDown() {
        commandLogger.setLevel(originalLevel);
    }
    
    @Test
    void assertGetCommandPacketType() {
        when(byteBuf.getInt(0)).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue());
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(engine.getCommandPacketType(payload), is(FirebirdCommandPacketType.EXECUTE));
    }
    
    @Test
    void assertGetCommandPacket() {
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(FirebirdConnectionProtocolVersion.getInstance()).thenReturn(connectionProtocolVersion);
        when(connectionProtocolVersion.getProtocolVersion(1)).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.EXECUTE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION10, 1)).thenReturn(commandPacket);
        assertThat(engine.getCommandPacket(payload, FirebirdCommandPacketType.EXECUTE, connectionSession), is(commandPacket));
    }
    
    @Test
    void assertGetCommandExecutorWithGenericDiagnostics(final LogCaptureAssertion logCaptureAssertion) {
        commandLogger.setLevel(Level.DEBUG);
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, packet, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.EXECUTE, packet, connectionSession), sameInstance(executor));
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.DEBUG, "Firebird command: connectionId=1, operation=EXECUTE", true);
    }
    
    @Test
    void assertGetCommandExecutorWithDebugDisabled(final LogCaptureAssertion logCaptureAssertion) {
        FirebirdExecuteStatementPacket executePacket = mock(FirebirdExecuteStatementPacket.class);
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, executePacket, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.EXECUTE, executePacket, connectionSession), sameInstance(executor));
        logCaptureAssertion.assertLogCount(0);
        verifyNoInteractions(executePacket);
    }
    
    @Test
    void assertGetCommandExecutorWithPrepareDiagnostics(final LogCaptureAssertion logCaptureAssertion) {
        commandLogger.setLevel(Level.DEBUG);
        when(connectionSession.getConnectionId()).thenReturn(1);
        FirebirdPrepareStatementPacket preparePacket = mock(FirebirdPrepareStatementPacket.class);
        when(preparePacket.getStatementId()).thenReturn(4);
        when(preparePacket.getTransactionId()).thenReturn(2);
        when(preparePacket.getSqlDialect()).thenReturn(3);
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.PREPARE_STATEMENT, preparePacket, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.PREPARE_STATEMENT, preparePacket, connectionSession), sameInstance(executor));
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.DEBUG, "Firebird command: connectionId=1, operation=PREPARE_STATEMENT, statementId=4, transactionId=2, sqlDialect=3", true);
        verify(preparePacket, never()).getSQL();
    }
    
    @Test
    void assertGetCommandExecutorWithExecuteDiagnostics(final LogCaptureAssertion logCaptureAssertion) {
        commandLogger.setLevel(Level.DEBUG);
        when(connectionSession.getConnectionId()).thenReturn(1);
        FirebirdExecuteStatementPacket executePacket = mock(FirebirdExecuteStatementPacket.class);
        when(executePacket.getStatementId()).thenReturn(4);
        when(executePacket.getTransactionId()).thenReturn(2);
        when(executePacket.getParameterValues()).thenReturn(Collections.singletonList("foo_secret"));
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE2, executePacket, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.EXECUTE2, executePacket, connectionSession), sameInstance(executor));
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.DEBUG, "Firebird command: connectionId=1, operation=EXECUTE2, statementId=4, transactionId=2, parameterCount=1", true);
    }
    
    @Test
    void assertGetCommandExecutorWithFetchDiagnostics(final LogCaptureAssertion logCaptureAssertion) {
        commandLogger.setLevel(Level.DEBUG);
        when(connectionSession.getConnectionId()).thenReturn(1);
        FirebirdFetchStatementPacket fetchPacket = mock(FirebirdFetchStatementPacket.class);
        when(fetchPacket.getStatementId()).thenReturn(4);
        when(fetchPacket.getFetchSize()).thenReturn(10);
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.FETCH, fetchPacket, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.FETCH, fetchPacket, connectionSession), sameInstance(executor));
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.DEBUG, "Firebird command: connectionId=1, operation=FETCH, statementId=4, fetchSize=10", true);
    }
    
    @Test
    void assertGetErrorPacket() {
        Exception cause = new Exception("error");
        when(FirebirdErrorPacketFactory.newInstance(cause)).thenReturn(errorPacket);
        assertThat(engine.getErrorPacket(cause), is(errorPacket));
    }
    
    @Test
    void assertGetOtherPacket() {
        assertFalse(engine.getOtherPacket(connectionSession).isPresent());
    }
    
    @Test
    void assertWriteQueryDataWithNonQueryResponseType() throws SQLException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        engine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(queryCommandExecutor, never()).next();
        verify(databaseConnectionManager, never()).getConnectionResourceLock();
        verify(context, never()).flush();
    }
    
    @Test
    void assertWriteQueryDataWithInactiveChannel() throws SQLException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        engine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(queryCommandExecutor, never()).next();
        verify(databaseConnectionManager, never()).getConnectionResourceLock();
        verify(context, never()).flush();
    }
    
    @Test
    void assertWriteQueryData() throws SQLException {
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(rowPacket);
        mockProxyContextFlushThreshold(2);
        engine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock).doAwait(context);
        verify(context).write(rowPacket);
        verify(context).flush();
    }
    
    @Test
    void assertWriteQueryDataWithFlushThreshold() throws SQLException {
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(rowPacket);
        mockProxyContextFlushThreshold(1);
        engine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock).doAwait(context);
        verify(context).write(rowPacket);
        verify(context, times(2)).flush();
    }
    
    private void mockProxyContextFlushThreshold(final int threshold) {
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD.getKey(), String.valueOf(threshold)));
        MetaDataContexts metaDataContexts = new MetaDataContexts(new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(props)), new ShardingSphereStatistics());
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts()).thenReturn(metaDataContexts);
    }
}
