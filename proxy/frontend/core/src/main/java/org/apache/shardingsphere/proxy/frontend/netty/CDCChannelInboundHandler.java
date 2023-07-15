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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCExceptionWrapper;
import org.apache.shardingsphere.data.pipeline.cdc.exception.CDCLoginException;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.cdc.handler.CDCBackendHandler;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.DropStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StopStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.executor.audit.exception.SQLAuditException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.Optional;

/**
 * CDC channel inbound handler.
 */
@Slf4j
public final class CDCChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    private static final AttributeKey<CDCConnectionContext> CONNECTION_CONTEXT_KEY = AttributeKey.valueOf("connection.context");
    
    private final CDCBackendHandler backendHandler = new CDCBackendHandler();
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        CDCResponse response = CDCResponse.newBuilder().setServerGreetingResult(ServerGreetingResult.newBuilder().setServerVersion(ShardingSphereVersion.VERSION).setProtocolVersion("1")
                .build()).build();
        ctx.writeAndFlush(response);
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        if (null != connectionContext.getJobId()) {
            backendHandler.stopStreaming(connectionContext.getJobId(), ctx.channel().id());
        }
        ctx.channel().attr(CONNECTION_CONTEXT_KEY).set(null);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("caught CDC resolution error", cause);
        // TODO add CDC exception to wrapper this exception, and add the parameters requestId and whether to close connect
        ChannelFuture channelFuture;
        if (cause instanceof CDCExceptionWrapper) {
            CDCExceptionWrapper wrapper = (CDCExceptionWrapper) cause;
            ShardingSphereSQLException exception = wrapper.getException();
            channelFuture = ctx.writeAndFlush(CDCResponseUtils.failed(wrapper.getRequestId(), exception.toSQLException().getSQLState(), exception.getMessage()));
        } else {
            channelFuture = ctx.writeAndFlush(CDCResponseUtils.failed("", XOpenSQLState.GENERAL_ERROR.getValue(), String.valueOf(cause.getMessage())));
        }
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        if (null == connectionContext) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        CDCRequest request = (CDCRequest) msg;
        if (null == connectionContext) {
            processLogin(ctx, request);
            return;
        }
        switch (request.getType()) {
            case STREAM_DATA:
                processStreamDataRequest(ctx, request, connectionContext);
                break;
            case ACK_STREAMING:
                processAckStreamingRequest(request);
                break;
            case STOP_STREAMING:
                processStopStreamingRequest(ctx, request, connectionContext);
                break;
            case START_STREAMING:
                processStartStreamingRequest(ctx, request, connectionContext);
                break;
            case DROP_STREAMING:
                processDropStreamingRequest(ctx, request, connectionContext);
                break;
            default:
                log.warn("can't handle this type of request {}", request);
                break;
        }
    }
    
    private void processLogin(final ChannelHandlerContext ctx, final CDCRequest request) {
        if (!request.hasLoginRequestBody() || !request.getLoginRequestBody().hasBasicBody()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Login request body is empty"));
        }
        BasicBody body = request.getLoginRequestBody().getBasicBody();
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Optional<ShardingSphereUser> user = authorityRule.findUser(new Grantee(body.getUsername(), getHostAddress(ctx)));
        if (user.isPresent() && Objects.equals(Hashing.sha256().hashBytes(user.get().getPassword().getBytes()).toString().toUpperCase(), body.getPassword())) {
            ctx.channel().attr(CONNECTION_CONTEXT_KEY).set(new CDCConnectionContext(user.get()));
            ctx.writeAndFlush(CDCResponseUtils.succeed(request.getRequestId()));
        } else {
            throw new CDCExceptionWrapper(request.getRequestId(), new CDCLoginException("Illegal username or password"));
        }
    }
    
    private void checkPrivileges(final String requestId, final Grantee grantee, final String currentDatabase) {
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(AuthorityRule.class)
                .orElseThrow(() -> new CDCExceptionWrapper(requestId, new MissingRequiredRuleException("authority")));
        ShardingSpherePrivileges privileges = authorityRule.findPrivileges(grantee)
                .orElseThrow(() -> new CDCExceptionWrapper(requestId, new SQLAuditException(String.format("Access denied for user '%s'@'%s'", grantee.getUsername(), grantee.getHostname()))));
        ShardingSpherePreconditions.checkState(privileges.hasPrivileges(currentDatabase),
                () -> new CDCExceptionWrapper(requestId, new SQLAuditException(String.format("Unknown database '%s'", currentDatabase))));
    }
    
    private String getHostAddress(final ChannelHandlerContext context) {
        SocketAddress socketAddress = context.channel().remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress().getHostAddress() : socketAddress.toString();
    }
    
    private void processStreamDataRequest(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        if (!request.hasStreamDataRequestBody()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Stream data request body is empty"));
        }
        StreamDataRequestBody requestBody = request.getStreamDataRequestBody();
        if (requestBody.getDatabase().isEmpty()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Database is empty"));
        }
        if (requestBody.getSourceSchemaTableList().isEmpty()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Source schema table is empty"));
        }
        checkPrivileges(request.getRequestId(), connectionContext.getCurrentUser().getGrantee(), requestBody.getDatabase());
        CDCResponse response = backendHandler.streamData(request.getRequestId(), requestBody, connectionContext, ctx.channel());
        ctx.writeAndFlush(response);
    }
    
    private void processAckStreamingRequest(final CDCRequest request) {
        if (!request.hasAckStreamingRequestBody()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Ack request body is empty"));
        }
        AckStreamingRequestBody requestBody = request.getAckStreamingRequestBody();
        if (requestBody.getAckId().isEmpty()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Ack request is empty"));
        }
        backendHandler.processAck(requestBody);
    }
    
    private void processStartStreamingRequest(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        if (!request.hasStartStreamingRequestBody()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Start streaming request body is empty"));
        }
        StartStreamingRequestBody requestBody = request.getStartStreamingRequestBody();
        if (requestBody.getStreamingId().isEmpty()) {
            throw new CDCExceptionWrapper(request.getRequestId(), new PipelineInvalidParameterException("Streaming id is empty"));
        }
        String database = backendHandler.getDatabaseNameByJobId(requestBody.getStreamingId());
        checkPrivileges(request.getRequestId(), connectionContext.getCurrentUser().getGrantee(), database);
        backendHandler.startStreaming(requestBody.getStreamingId(), connectionContext, ctx.channel());
        ctx.writeAndFlush(CDCResponseUtils.succeed(request.getRequestId()));
    }
    
    private void processStopStreamingRequest(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        StopStreamingRequestBody requestBody = request.getStopStreamingRequestBody();
        String database = backendHandler.getDatabaseNameByJobId(requestBody.getStreamingId());
        checkPrivileges(request.getRequestId(), connectionContext.getCurrentUser().getGrantee(), database);
        backendHandler.stopStreaming(connectionContext.getJobId(), ctx.channel().id());
        connectionContext.setJobId(null);
        ctx.writeAndFlush(CDCResponseUtils.succeed(request.getRequestId()));
    }
    
    private void processDropStreamingRequest(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        DropStreamingRequestBody requestBody = request.getDropStreamingRequestBody();
        checkPrivileges(request.getRequestId(), connectionContext.getCurrentUser().getGrantee(), backendHandler.getDatabaseNameByJobId(requestBody.getStreamingId()));
        backendHandler.dropStreaming(connectionContext.getJobId());
        connectionContext.setJobId(null);
        ctx.writeAndFlush(CDCResponseUtils.succeed(request.getRequestId()));
    }
}
