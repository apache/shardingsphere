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

package org.apache.shardingsphere.proxy.frontend.netty;

import com.google.common.util.concurrent.MoreExecutors;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.authentication.result.AuthenticationResultBuilder;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.exception.ExpectedExceptions;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.executor.UserExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyStateContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FrontendChannelInboundHandlerTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseProtocolFrontendEngine frontendEngine;
    
    @Mock
    private AuthenticationEngine authenticationEngine;
    
    private EmbeddedChannel channel;
    
    private FrontendChannelInboundHandler frontendChannelInboundHandler;
    
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        when(frontendEngine.getAuthenticationEngine()).thenReturn(authenticationEngine);
        when(frontendEngine.getType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        lenient().when(authenticationEngine.handshake(any(ChannelHandlerContext.class))).thenReturn(CONNECTION_ID);
        channel = new EmbeddedChannel(false, true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(mock(TransactionRule.class), mock(AuthorityRule.class))));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        frontendChannelInboundHandler = new FrontendChannelInboundHandler(frontendEngine, channel);
        channel.pipeline().addLast(frontendChannelInboundHandler);
        connectionSession = getConnectionSession();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ConnectionSession getConnectionSession() {
        return (ConnectionSession) Plugins.getMemberAccessor().get(FrontendChannelInboundHandler.class.getDeclaredField("connectionSession"), frontendChannelInboundHandler);
    }
    
    @Test
    void assertChannelActive() throws Exception {
        channel.register();
        verify(authenticationEngine).handshake(any(ChannelHandlerContext.class));
        assertThat(connectionSession.getConnectionId(), is(CONNECTION_ID));
    }
    
    @Test
    void assertChannelReadNotAuthenticated() throws Exception {
        channel.register();
        AuthenticationResult authenticationResult = AuthenticationResultBuilder.finished("username", "hostname", "database");
        when(authenticationEngine.authenticate(any(ChannelHandlerContext.class), any(PacketPayload.class))).thenReturn(authenticationResult);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        assertThat(connectionSession.getConnectionContext().getGrantee(), is(new Grantee("username", "hostname")));
        assertThat(connectionSession.getUsedDatabaseName(), is("database"));
    }
    
    @Test
    void assertChannelReadWithUnfinishedAuthentication() throws Exception {
        channel.register();
        when(authenticationEngine.authenticate(any(ChannelHandlerContext.class), any(PacketPayload.class))).thenReturn(AuthenticationResultBuilder.continued());
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        assertFalse(getAuthenticated().get());
        assertNull(connectionSession.getConnectionContext());
    }
    
    @Test
    void assertChannelReadWhenAuthenticated() throws Exception {
        channel.register();
        getAuthenticated().set(true);
        Object message = new Object();
        ChannelHandlerContext context = channel.pipeline().context(frontendChannelInboundHandler);
        try (MockedStatic<ProxyStateContext> mockedStatic = mockStatic(ProxyStateContext.class)) {
            channel.writeInbound(message);
            mockedStatic.verify(() -> ProxyStateContext.execute(context, message, frontendEngine, connectionSession));
        }
    }
    
    @Test
    void assertChannelReadWithExpectedException() throws Exception {
        channel.register();
        SQLDialectException cause = new SQLDialectException("assertChannelReadWithExpectedException") {
        };
        doThrow(cause).when(authenticationEngine).authenticate(any(ChannelHandlerContext.class), any(PacketPayload.class));
        DatabasePacket expectedPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getErrorPacket(cause)).thenReturn(expectedPacket);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        assertThat(channel.readOutbound(), is(expectedPacket));
        assertFalse(channel.isActive());
    }
    
    @Test
    void assertChannelReadNotAuthenticatedAndExceptionOccur() throws Exception {
        channel.register();
        RuntimeException cause = new RuntimeException("assertChannelReadNotAuthenticatedAndExceptionOccur");
        doThrow(cause).when(authenticationEngine).authenticate(any(ChannelHandlerContext.class), any(PacketPayload.class));
        DatabasePacket expectedPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getErrorPacket(cause)).thenReturn(expectedPacket);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        assertThat(channel.readOutbound(), is(expectedPacket));
    }
    
    @Test
    void assertChannelInactiveWithUnexpectedException() throws Exception {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.closeAllResources()).thenReturn(Collections.singleton(new SQLException("assertChannelInactiveWithUnexpectedException")));
        setDatabaseConnectionManager(databaseConnectionManager);
        ProcessEngine processEngine = mock(ProcessEngine.class);
        setProcessEngine(processEngine);
        connectionSession.setProcessId("process-id");
        ExecutorService executorService = MoreExecutors.newDirectExecutorService();
        UserExecutorGroup userExecutorGroup = mock(UserExecutorGroup.class);
        when(userExecutorGroup.getExecutorService()).thenReturn(executorService);
        ConnectionThreadExecutorGroup connectionThreadExecutorGroup = mock(ConnectionThreadExecutorGroup.class);
        try (
                MockedStatic<UserExecutorGroup> mockedUserExecutorGroup = mockStatic(UserExecutorGroup.class);
                MockedStatic<ConnectionThreadExecutorGroup> mockedConnectionThreadExecutorGroup = mockStatic(ConnectionThreadExecutorGroup.class)) {
            mockedUserExecutorGroup.when(UserExecutorGroup::getInstance).thenReturn(userExecutorGroup);
            mockedConnectionThreadExecutorGroup.when(ConnectionThreadExecutorGroup::getInstance).thenReturn(connectionThreadExecutorGroup);
            channel.register();
            channel.pipeline().fireChannelInactive();
            verify(connectionThreadExecutorGroup).unregisterAndAwaitTermination(CONNECTION_ID);
            verify(databaseConnectionManager).closeAllResources();
            verify(processEngine).disconnect("process-id");
            verify(frontendEngine).release(connectionSession);
        }
        executorService.shutdownNow();
    }
    
    @Test
    void assertChannelInactiveWithExpectedException() throws Exception {
        Collection<Class<? extends Exception>> originalExpectedExceptions = new HashSet<>(getExpectedExceptions());
        Collection<Class<? extends Exception>> expectedExceptions = getExpectedExceptions();
        expectedExceptions.add(SQLException.class);
        try {
            ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
            when(databaseConnectionManager.closeAllResources()).thenReturn(Collections.singleton(new SQLException("assertChannelInactiveWithExpectedException")));
            setDatabaseConnectionManager(databaseConnectionManager);
            ExecutorService executorService = MoreExecutors.newDirectExecutorService();
            UserExecutorGroup userExecutorGroup = mock(UserExecutorGroup.class);
            when(userExecutorGroup.getExecutorService()).thenReturn(executorService);
            ConnectionThreadExecutorGroup connectionThreadExecutorGroup = mock(ConnectionThreadExecutorGroup.class);
            try (
                    MockedStatic<UserExecutorGroup> mockedUserExecutorGroup = mockStatic(UserExecutorGroup.class);
                    MockedStatic<ConnectionThreadExecutorGroup> mockedConnectionThreadExecutorGroup = mockStatic(ConnectionThreadExecutorGroup.class)) {
                mockedUserExecutorGroup.when(UserExecutorGroup::getInstance).thenReturn(userExecutorGroup);
                mockedConnectionThreadExecutorGroup.when(ConnectionThreadExecutorGroup::getInstance).thenReturn(connectionThreadExecutorGroup);
                channel.register();
                channel.pipeline().fireChannelInactive();
                verify(connectionThreadExecutorGroup).unregisterAndAwaitTermination(CONNECTION_ID);
                verify(databaseConnectionManager).closeAllResources();
                verify(frontendEngine).release(connectionSession);
            }
            executorService.shutdownNow();
        } finally {
            Collection<Class<? extends Exception>> expectedExceptionClasses = getExpectedExceptions();
            expectedExceptionClasses.clear();
            expectedExceptionClasses.addAll(originalExpectedExceptions);
        }
    }
    
    @Test
    void assertChannelWritabilityChangedWhenWritable() throws Exception {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        ConnectionResourceLock connectionResourceLock = mock(ConnectionResourceLock.class);
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        channel.register();
        setDatabaseConnectionManager(databaseConnectionManager);
        assertThat(connectionSession.getDatabaseConnectionManager(), is(databaseConnectionManager));
        Channel channelMock = mock(Channel.class);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(channelMock.isWritable()).thenReturn(true);
        when(context.channel()).thenReturn(channelMock);
        frontendChannelInboundHandler.channelWritabilityChanged(context);
        verify(connectionResourceLock).doNotify();
    }
    
    @Test
    void assertChannelWritabilityChangedWhenNotWritable() throws Exception {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        channel.register();
        setDatabaseConnectionManager(databaseConnectionManager);
        assertThat(connectionSession.getDatabaseConnectionManager(), is(databaseConnectionManager));
        Channel channelMock = mock(Channel.class);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(channelMock.isWritable()).thenReturn(false);
        when(context.channel()).thenReturn(channelMock);
        frontendChannelInboundHandler.channelWritabilityChanged(context);
        verify(databaseConnectionManager, never()).getConnectionResourceLock();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private AtomicBoolean getAuthenticated() {
        return (AtomicBoolean) Plugins.getMemberAccessor().get(FrontendChannelInboundHandler.class.getDeclaredField("authenticated"), frontendChannelInboundHandler);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setDatabaseConnectionManager(final ProxyDatabaseConnectionManager databaseConnectionManager) {
        Plugins.getMemberAccessor().set(ConnectionSession.class.getDeclaredField("databaseConnectionManager"), connectionSession, databaseConnectionManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setProcessEngine(final ProcessEngine processEngine) {
        Plugins.getMemberAccessor().set(FrontendChannelInboundHandler.class.getDeclaredField("processEngine"), frontendChannelInboundHandler, processEngine);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<Class<? extends Exception>> getExpectedExceptions() {
        return (Collection<Class<? extends Exception>>) Plugins.getMemberAccessor().get(ExpectedExceptions.class.getDeclaredField("EXCEPTIONS"), null);
    }
}
