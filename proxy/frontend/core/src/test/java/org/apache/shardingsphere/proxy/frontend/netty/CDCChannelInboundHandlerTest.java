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

import com.google.common.hash.Hashing;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCExceptionWrapper;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.cdc.handler.CDCBackendHandler;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.DropStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StopStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.protocol.FrontDatabaseProtocolTypeFactory;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, FrontDatabaseProtocolTypeFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CDCChannelInboundHandlerTest {
    
    private static final AttributeKey<CDCConnectionContext> CONNECTION_CONTEXT_KEY = AttributeKey.valueOf("connection.context");
    
    private final CDCChannelInboundHandler handler = new CDCChannelInboundHandler();
    
    private final EmbeddedChannel channel = new EmbeddedChannel(new LoggingHandler(), handler);
    
    private final ShardingSphereUser user = new ShardingSphereUser("root", "root", "%");
    
    @Mock
    private CDCBackendHandler backendHandler;
    
    @Mock
    private AuthorityRule authorityRule;
    
    @Mock
    private ShardingSpherePrivileges privileges;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @BeforeEach
    void setup() {
        Plugins.getMemberAccessor().set(CDCChannelInboundHandler.class.getDeclaredField("backendHandler"), handler, backendHandler);
        when(authorityRule.findUser(any())).thenReturn(Optional.of(user));
        when(authorityRule.findPrivileges(any())).thenReturn(Optional.of(privileges));
        when(privileges.hasPrivileges(any())).thenReturn(true);
        when(FrontDatabaseProtocolTypeFactory.getDatabaseType()).thenReturn(mock(DatabaseType.class));
        mockProxyContext(new RuleMetaData(Collections.singleton(authorityRule)));
    }
    
    private void mockProxyContext(final RuleMetaData ruleMetaData) {
        ProxyContext proxyContext = mock(ProxyContext.class);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(ruleMetaData);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE)).thenReturn(mock(DatabaseType.class));
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(props);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        when(proxyContext.getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertChannelInactiveStopsStreaming() {
        CDCConnectionContext connectionContext = new CDCConnectionContext(user);
        connectionContext.setJobId("job-id");
        channel.attr(CONNECTION_CONTEXT_KEY).set(connectionContext);
        channel.pipeline().fireChannelInactive();
        verify(backendHandler).stopStreaming("job-id", channel.id());
        assertNull(channel.attr(CONNECTION_CONTEXT_KEY).get());
    }
    
    @Test
    void assertChannelInactiveWithConnectionContextWithoutJob() {
        Attribute<CDCConnectionContext> attribute = mock(Attribute.class);
        when(attribute.get()).thenReturn(new CDCConnectionContext(user));
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        when(context.channel().attr(CONNECTION_CONTEXT_KEY)).thenReturn(attribute);
        handler.channelInactive(context);
        verify(backendHandler, never()).stopStreaming(anyString(), any());
        verify(attribute).set(null);
    }
    
    @Test
    void assertExceptionCaughtWithWrapperClosesChannel() {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        ChannelFuture channelFuture = mock(ChannelFuture.class);
        when(context.writeAndFlush(any())).thenReturn(channelFuture);
        handler.exceptionCaught(context, new CDCExceptionWrapper("request-id", new PipelineInvalidParameterException("invalid")));
        verify(context).writeAndFlush(argThat(argument -> "request-id".equals(((CDCResponse) argument).getRequestId())));
        verify(channelFuture).addListener(ChannelFutureListener.CLOSE);
    }
    
    @Test
    void assertExceptionCaughtWithNonWrapperException() {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        ChannelFuture channelFuture = mock(ChannelFuture.class);
        when(context.channel().attr(CONNECTION_CONTEXT_KEY).get()).thenReturn(new CDCConnectionContext(user));
        when(context.writeAndFlush(any())).thenReturn(channelFuture);
        handler.exceptionCaught(context, new RuntimeException("error"));
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
    
    @Test
    void assertLoginRequestFailed() {
        CDCRequest request = CDCRequest.newBuilder().setType(Type.LOGIN).setLoginRequestBody(LoginRequestBody.newBuilder()
                .setBasicBody(BasicBody.newBuilder().setUsername("root2").build()).build()).build();
        channel.writeInbound(request);
        CDCResponse greeting = channel.readOutbound();
        assertTrue(greeting.hasServerGreetingResult());
        CDCResponse loginResult = channel.readOutbound();
        assertThat(loginResult.getStatus(), is(Status.FAILED));
        assertThat(loginResult.getErrorCode(), is(XOpenSQLState.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT.getValue()));
        assertFalse(channel.isOpen());
    }
    
    @Test
    void assertIllegalLoginRequest() {
        CDCRequest request = CDCRequest.newBuilder().setType(Type.LOGIN).setVersion(1).setRequestId("test").build();
        channel.writeInbound(request);
        CDCResponse greeting = channel.readOutbound();
        assertTrue(greeting.hasServerGreetingResult());
        CDCResponse loginResult = channel.readOutbound();
        assertThat(loginResult.getStatus(), is(Status.FAILED));
        assertThat(loginResult.getErrorCode(), is(XOpenSQLState.NOT_FOUND.getValue()));
        assertFalse(channel.isOpen());
    }
    
    @Test
    void assertLoginRequestSucceed() {
        channel.connect(new InetSocketAddress("127.0.0.1", 3307));
        String encryptPassword = Hashing.sha256().hashBytes("root".getBytes()).toString().toUpperCase();
        Builder builder = CDCRequest.newBuilder().setType(Type.LOGIN).setLoginRequestBody(LoginRequestBody.newBuilder().setBasicBody(BasicBody.newBuilder().setUsername("root")
                .setPassword(encryptPassword).build()).build());
        channel.writeInbound(builder.build());
        CDCResponse greeting = channel.readOutbound();
        assertTrue(greeting.hasServerGreetingResult());
        CDCResponse loginResult = channel.readOutbound();
        assertThat(loginResult.getStatus(), is(Status.SUCCEED));
        assertThat(loginResult.getErrorCode(), is(""));
        assertThat(loginResult.getErrorMessage(), is(""));
    }
    
    @Test
    void assertLoginRequestWithExistingConnectionContext() {
        channel.attr(CONNECTION_CONTEXT_KEY).set(new CDCConnectionContext(user));
        channel.writeInbound(createLoginRequest(Hashing.sha256().hashBytes("root".getBytes()).toString().toUpperCase()));
        CDCResponse greeting = channel.readOutbound();
        assertTrue(greeting.hasServerGreetingResult());
        CDCResponse loginResult = channel.readOutbound();
        assertThat(loginResult.getStatus(), is(Status.SUCCEED));
        assertThat(channel.attr(CONNECTION_CONTEXT_KEY).get().getCurrentUser().getGrantee().getUsername(), is("root"));
    }
    
    @Test
    void assertLoginWithNonInetSocketAddress() {
        Channel channelMock = mock(Channel.class, RETURNS_DEEP_STUBS);
        Attribute<CDCConnectionContext> attribute = mock(Attribute.class);
        when(channelMock.attr(CONNECTION_CONTEXT_KEY)).thenReturn(attribute);
        SocketAddress socketAddress = new SocketAddress() {
            
            private static final long serialVersionUID = -733394185197826745L;
            
            @Override
            public String toString() {
                return "local-address";
            }
        };
        when(channelMock.remoteAddress()).thenReturn(socketAddress);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        when(context.channel()).thenReturn(channelMock);
        when(context.writeAndFlush(any())).thenReturn(mock(ChannelFuture.class));
        handler.channelRead(context, createLoginRequest(Hashing.sha256().hashBytes("root".getBytes()).toString().toUpperCase()));
        verify(context).writeAndFlush(argThat(argument -> ((CDCResponse) argument).getStatus() == Status.SUCCEED));
        verify(attribute).set(any(CDCConnectionContext.class));
    }
    
    @Test
    void assertLoginUsesInetSocketAddressHost() {
        Channel channelMock = mock(Channel.class, RETURNS_DEEP_STUBS);
        Attribute<CDCConnectionContext> attribute = mock(Attribute.class);
        when(channelMock.attr(CONNECTION_CONTEXT_KEY)).thenReturn(attribute);
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.2", 3307);
        when(channelMock.remoteAddress()).thenReturn(remoteAddress);
        ChannelFuture channelFuture = mock(ChannelFuture.class);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        when(context.channel()).thenReturn(channelMock);
        when(context.writeAndFlush(any())).thenReturn(channelFuture);
        CDCRequest request = createLoginRequest(Hashing.sha256().hashBytes("root".getBytes()).toString().toUpperCase());
        handler.channelRead(context, request);
        ArgumentCaptor<Grantee> captor = ArgumentCaptor.forClass(Grantee.class);
        verify(authorityRule, atLeastOnce()).findUser(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(grantee -> "127.0.0.2".equals(grantee.getHostname())));
    }
    
    @Test
    void assertStreamDataRequestBodyMissing() {
        CDCRequest request = CDCRequest.newBuilder().setType(Type.STREAM_DATA).setRequestId("stream-request").build();
        CDCResponse response = writeRequestWithContext(request);
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertStreamDataRequestDatabaseMissing() {
        CDCRequest request = createStreamDataRequest("");
        CDCResponse response = writeRequestWithContext(request);
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertStreamDataRequestSourceSchemaTableMissing() {
        StreamDataRequestBody body = StreamDataRequestBody.newBuilder().setDatabase("logic_db").build();
        CDCRequest request = CDCRequest.newBuilder().setType(Type.STREAM_DATA).setRequestId("stream-request").setStreamDataRequestBody(body).build();
        CDCResponse response = writeRequestWithContext(request);
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertStreamDataRequestMissingAuthorityRule() {
        mockProxyContext(new RuleMetaData(Collections.emptyList()));
        CDCRequest request = createStreamDataRequest("logic_db");
        CDCResponse response = writeRequestWithContext(request);
        SQLException expectedException = SQLExceptionTransformEngine.toSQLException(new MissingRequiredRuleException("authority"), FrontDatabaseProtocolTypeFactory.getDatabaseType());
        assertThat(response.getErrorCode(), is(expectedException.getSQLState()));
    }
    
    @Test
    void assertStreamDataRequestPrivilegesNotFound() {
        when(authorityRule.findPrivileges(any())).thenReturn(Optional.empty());
        CDCRequest request = createStreamDataRequest("logic_db");
        CDCResponse response = writeRequestWithContext(request);
        Grantee grantee = user.getGrantee();
        SQLException expectedException = SQLExceptionTransformEngine.toSQLException(new AccessDeniedException(grantee.getUsername(), grantee.getHostname(), false),
                FrontDatabaseProtocolTypeFactory.getDatabaseType());
        assertThat(response.getErrorCode(), is(expectedException.getSQLState()));
    }
    
    @Test
    void assertStreamDataRequestPrivilegesWithoutDatabasePermission() {
        when(privileges.hasPrivileges("logic_db")).thenReturn(false);
        CDCRequest request = createStreamDataRequest("logic_db");
        CDCResponse response = writeRequestWithContext(request);
        SQLException expectedException = SQLExceptionTransformEngine.toSQLException(new UnknownDatabaseException("logic_db"), FrontDatabaseProtocolTypeFactory.getDatabaseType());
        assertThat(response.getErrorCode(), is(expectedException.getSQLState()));
    }
    
    private CDCRequest createStreamDataRequest(final String database) {
        StreamDataRequestBody.Builder bodyBuilder = StreamDataRequestBody.newBuilder().setDatabase(database);
        bodyBuilder.addSourceSchemaTable(StreamDataRequestBody.SchemaTable.newBuilder().setSchema("schema").setTable("table").build());
        return CDCRequest.newBuilder().setType(Type.STREAM_DATA).setRequestId("stream-request").setStreamDataRequestBody(bodyBuilder.build()).build();
    }
    
    @Test
    void assertStreamDataRequestWrapsPipelineSQLException() {
        CDCConnectionContext connectionContext = new CDCConnectionContext(user);
        channel.attr(CONNECTION_CONTEXT_KEY).set(connectionContext);
        StreamDataRequestBody.Builder bodyBuilder = StreamDataRequestBody.newBuilder().setDatabase("logic_db");
        bodyBuilder.addSourceSchemaTable(StreamDataRequestBody.SchemaTable.newBuilder().setSchema("schema").setTable("table").build());
        CDCRequest request = CDCRequest.newBuilder().setType(Type.STREAM_DATA).setRequestId("stream-request").setStreamDataRequestBody(bodyBuilder.build()).build();
        when(backendHandler.streamData(any(), any(), any(), any())).thenThrow(mock(PipelineSQLException.class));
        ChannelHandlerContext context = channel.pipeline().context(handler);
        assertThrows(CDCExceptionWrapper.class, () -> handler.channelRead(context, request));
        assertThat(channel.attr(CONNECTION_CONTEXT_KEY).get(), is(connectionContext));
    }
    
    @Test
    void assertStreamDataRequestSucceed() {
        channel.attr(CONNECTION_CONTEXT_KEY).set(new CDCConnectionContext(user));
        StreamDataRequestBody.Builder bodyBuilder = StreamDataRequestBody.newBuilder().setDatabase("logic_db");
        bodyBuilder.addSourceSchemaTable(StreamDataRequestBody.SchemaTable.newBuilder().setSchema("schema").setTable("table").build());
        CDCRequest request = CDCRequest.newBuilder().setType(Type.STREAM_DATA).setRequestId("stream-request").setStreamDataRequestBody(bodyBuilder.build()).build();
        CDCResponse succeedResponse = CDCResponseUtils.succeed("stream-request");
        when(backendHandler.streamData(any(), any(), any(), any())).thenReturn(succeedResponse);
        channel.writeInbound(request);
        CDCResponse response = readResponseSkippingGreeting();
        assertThat(response.getStatus(), is(Status.SUCCEED));
        assertThat(response.getRequestId(), is("stream-request"));
    }
    
    @Test
    void assertAckStreamingRequestBodyMissing() {
        CDCRequest request = CDCRequest.newBuilder().setType(Type.ACK_STREAMING).setRequestId("ack-request").build();
        CDCResponse response = writeRequestWithContext(request);
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertAckStreamingRequestAckIdMissing() {
        AckStreamingRequestBody.Builder bodyBuilder = AckStreamingRequestBody.newBuilder();
        bodyBuilder.setAckId("");
        CDCResponse response = writeRequestWithContext(CDCRequest.newBuilder().setType(Type.ACK_STREAMING).setRequestId("ack-request").setAckStreamingRequestBody(bodyBuilder.build()).build());
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertAckStreamingRequestSucceed() {
        channel.attr(CONNECTION_CONTEXT_KEY).set(new CDCConnectionContext(user));
        AckStreamingRequestBody.Builder bodyBuilder = AckStreamingRequestBody.newBuilder();
        bodyBuilder.setAckId("ack-1");
        CDCRequest request = CDCRequest.newBuilder().setType(Type.ACK_STREAMING).setRequestId("ack-request").setAckStreamingRequestBody(bodyBuilder.build()).build();
        channel.writeInbound(request);
        channel.readOutbound();
        verify(backendHandler).processAck(request.getAckStreamingRequestBody());
    }
    
    @Test
    void assertStartStreamingRequestBodyMissing() {
        CDCRequest request = CDCRequest.newBuilder().setType(Type.START_STREAMING).setRequestId("start-request").build();
        CDCResponse response = writeRequestWithContext(request);
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertStartStreamingRequestIdMissing() {
        StartStreamingRequestBody.Builder bodyBuilder = StartStreamingRequestBody.newBuilder();
        bodyBuilder.setStreamingId("");
        CDCResponse response = writeRequestWithContext(CDCRequest.newBuilder().setType(Type.START_STREAMING).setRequestId("start-request").setStartStreamingRequestBody(bodyBuilder.build()).build());
        assertThat(response.getStatus(), is(Status.FAILED));
        assertThat(response.getErrorCode(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
    }
    
    @Test
    void assertStartStreamingRequestSucceed() {
        CDCConnectionContext connectionContext = new CDCConnectionContext(user);
        channel.attr(CONNECTION_CONTEXT_KEY).set(connectionContext);
        when(backendHandler.getDatabaseNameByJobId("job-1")).thenReturn("logic_db");
        StartStreamingRequestBody.Builder bodyBuilder = StartStreamingRequestBody.newBuilder();
        bodyBuilder.setStreamingId("job-1");
        channel.writeInbound(CDCRequest.newBuilder().setType(Type.START_STREAMING).setRequestId("start-request").setStartStreamingRequestBody(bodyBuilder.build()).build());
        CDCResponse response = readResponseSkippingGreeting();
        verify(backendHandler).startStreaming("job-1", connectionContext, channel);
        assertThat(response.getStatus(), is(Status.SUCCEED));
    }
    
    @Test
    void assertStopStreamingRequestSucceed() {
        CDCConnectionContext connectionContext = new CDCConnectionContext(user);
        connectionContext.setJobId("job-1");
        channel.attr(CONNECTION_CONTEXT_KEY).set(connectionContext);
        when(backendHandler.getDatabaseNameByJobId("job-1")).thenReturn("logic_db");
        StopStreamingRequestBody.Builder bodyBuilder = StopStreamingRequestBody.newBuilder();
        bodyBuilder.setStreamingId("job-1");
        channel.writeInbound(CDCRequest.newBuilder().setType(Type.STOP_STREAMING).setRequestId("stop-request").setStopStreamingRequestBody(bodyBuilder.build()).build());
        CDCResponse response = readResponseSkippingGreeting();
        verify(backendHandler).stopStreaming("job-1", channel.id());
        assertNull(connectionContext.getJobId());
        assertThat(response.getStatus(), is(Status.SUCCEED));
    }
    
    @Test
    void assertDropStreamingRequestSucceed() {
        CDCConnectionContext connectionContext = new CDCConnectionContext(user);
        connectionContext.setJobId("job-1");
        channel.attr(CONNECTION_CONTEXT_KEY).set(connectionContext);
        when(backendHandler.getDatabaseNameByJobId("job-1")).thenReturn("logic_db");
        DropStreamingRequestBody.Builder bodyBuilder = DropStreamingRequestBody.newBuilder();
        bodyBuilder.setStreamingId("job-1");
        channel.writeInbound(CDCRequest.newBuilder().setType(Type.DROP_STREAMING).setRequestId("drop-request").setDropStreamingRequestBody(bodyBuilder.build()).build());
        CDCResponse response = readResponseSkippingGreeting();
        verify(backendHandler).dropStreaming("job-1");
        assertNull(connectionContext.getJobId());
        assertThat(response.getStatus(), is(Status.SUCCEED));
    }
    
    @Test
    void assertUnknownRequestTypeIgnored() {
        channel.attr(CONNECTION_CONTEXT_KEY).set(new CDCConnectionContext(user));
        channel.writeInbound(CDCRequest.newBuilder().setType(Type.UNKNOWN).build());
        assertNotNull(channel.readOutbound());
        assertNull(channel.readOutbound());
        assertTrue(channel.isOpen());
    }
    
    private CDCRequest createLoginRequest(final String password) {
        BasicBody.Builder bodyBuilder = BasicBody.newBuilder().setUsername("root");
        bodyBuilder.setPassword(password);
        return CDCRequest.newBuilder().setType(Type.LOGIN).setLoginRequestBody(LoginRequestBody.newBuilder().setBasicBody(bodyBuilder.build()).build()).build();
    }
    
    private CDCResponse writeRequestWithContext(final CDCRequest request) {
        channel.attr(CONNECTION_CONTEXT_KEY).set(new CDCConnectionContext(user));
        channel.writeInbound(request);
        return readResponseSkippingGreeting();
    }
    
    private CDCResponse readResponseSkippingGreeting() {
        channel.readOutbound();
        return channel.readOutbound();
    }
}
