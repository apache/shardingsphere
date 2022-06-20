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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResultBuilder;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FrontendChannelInboundHandlerTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseProtocolFrontendEngine frontendEngine;
    
    @Mock
    private AuthenticationEngine authenticationEngine;
    
    private EmbeddedChannel channel;
    
    private FrontendChannelInboundHandler frontendChannelInboundHandler;
    
    private ConnectionSession connectionSession;
    
    @Before
    public void setup() {
        when(frontendEngine.getAuthenticationEngine()).thenReturn(authenticationEngine);
        when(frontendEngine.getType()).thenReturn("MySQL");
        when(authenticationEngine.handshake(any(ChannelHandlerContext.class))).thenReturn(CONNECTION_ID);
        channel = new EmbeddedChannel(false, true);
        try (MockedStatic<ProxyContext> mocked = mockStatic(ProxyContext.class)) {
            ProxyContext mockedProxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
            mocked.when(ProxyContext::getInstance).thenReturn(mockedProxyContext);
            ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
            when(mockedProxyContext.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
            when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(mock(TransactionRule.class));
            frontendChannelInboundHandler = new FrontendChannelInboundHandler(frontendEngine, channel);
        }
        channel.pipeline().addLast(frontendChannelInboundHandler);
        connectionSession = getConnectionSession();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ConnectionSession getConnectionSession() {
        Field connectionSessionField = FrontendChannelInboundHandler.class.getDeclaredField("connectionSession");
        connectionSessionField.setAccessible(true);
        return (ConnectionSession) connectionSessionField.get(frontendChannelInboundHandler);
    }
    
    @Test
    public void assertChannelActive() throws Exception {
        channel.register();
        verify(authenticationEngine).handshake(any(ChannelHandlerContext.class));
        assertThat(connectionSession.getConnectionId(), is(CONNECTION_ID));
    }
    
    @Test
    public void assertChannelReadNotAuthenticated() throws Exception {
        channel.register();
        AuthenticationResult authenticationResult = AuthenticationResultBuilder.finished("username", "hostname", "database");
        when(authenticationEngine.authenticate(any(ChannelHandlerContext.class), any(PacketPayload.class))).thenReturn(authenticationResult);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        assertThat(connectionSession.getGrantee(), is(new Grantee("username", "hostname")));
        assertThat(connectionSession.getDatabaseName(), is("database"));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertChannelReadNotAuthenticatedAndExceptionOccur() throws Exception {
        channel.register();
        RuntimeException cause = new RuntimeException("assertChannelReadNotAuthenticatedAndExceptionOccur");
        doThrow(cause).when(authenticationEngine).authenticate(any(ChannelHandlerContext.class), any(PacketPayload.class));
        DatabasePacket expectedPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getErrorPacket(cause)).thenReturn(expectedPacket);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        assertThat(channel.readOutbound(), is(expectedPacket));
    }
}
