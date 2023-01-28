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
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.DropSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StopSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.executor.check.exception.SQLCheckException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.cdc.CDCBackendHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.SQLException;
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
        CDCConnectionContext context = new CDCConnectionContext();
        context.setStatus(CDCConnectionStatus.NOT_LOGGED_IN);
        ctx.channel().attr(CONNECTION_CONTEXT_KEY).setIfAbsent(context);
        CDCResponse response = CDCResponse.newBuilder().setServerGreetingResult(ServerGreetingResult.newBuilder().setServerVersion(ShardingSphereVersion.VERSION).setProtocolVersion("1")
                .build()).build();
        ctx.writeAndFlush(response);
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        if (null != connectionContext.getJobId()) {
            backendHandler.stopSubscription(connectionContext.getJobId());
        }
        ctx.channel().attr(CONNECTION_CONTEXT_KEY).set(null);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("caught CDC resolution error", cause);
        // TODO add CDC exception to wrapper this exception, and add the parameters requestId and whether to close connect
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        ChannelFuture channelFuture;
        if (cause instanceof ShardingSphereSQLException) {
            SQLException sqlException = ((ShardingSphereSQLException) cause).toSQLException();
            String errorMessage = String.format("ERROR %s (%s): %s", sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
            channelFuture = ctx.writeAndFlush(CDCResponseGenerator.failed("", CDCResponseErrorCode.SERVER_ERROR, errorMessage));
        } else {
            channelFuture = ctx.writeAndFlush(CDCResponseGenerator.failed("", CDCResponseErrorCode.SERVER_ERROR, cause.getMessage()));
        }
        if (CDCConnectionStatus.NOT_LOGGED_IN == connectionContext.getStatus()) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        CDCConnectionStatus status = connectionContext.getStatus();
        CDCRequest request = (CDCRequest) msg;
        if (CDCConnectionStatus.NOT_LOGGED_IN == status) {
            processLogin(ctx, request, connectionContext);
            return;
        }
        switch (request.getRequestCase()) {
            case CREATE_SUBSCRIPTION:
                processCreateSubscription(ctx, request, connectionContext);
                break;
            case START_SUBSCRIPTION:
                processStartSubscription(ctx, request, connectionContext);
                break;
            case STOP_SUBSCRIPTION:
                processStopSubscription(ctx, request, connectionContext);
                break;
            case DROP_SUBSCRIPTION:
                processDropSubscription(ctx, request, connectionContext);
                break;
            case ACK_REQUEST:
                processAckRequest(ctx, request);
                break;
            default:
                log.warn("Cannot handle this type of request {}", request);
        }
    }
    
    private void processLogin(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        if (!request.hasLogin() || !request.getLogin().hasBasicBody()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss login request body")).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        BasicBody body = request.getLogin().getBasicBody();
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Optional<ShardingSphereUser> user = authorityRule.findUser(new Grantee(body.getUsername(), getHostAddress(ctx)));
        if (user.isPresent() && Objects.equals(Hashing.sha256().hashBytes(user.get().getPassword().getBytes()).toString().toUpperCase(), body.getPassword())) {
            connectionContext.setStatus(CDCConnectionStatus.LOGGED_IN);
            connectionContext.setCurrentUser(user.get());
            ctx.writeAndFlush(CDCResponseGenerator.succeedBuilder(request.getRequestId()).build());
        } else {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_USERNAME_OR_PASSWORD, "Illegal username or password"))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    private void checkPrivileges(final Grantee grantee, final String currentDatabase) {
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(AuthorityRule.class)
                .orElseThrow(() -> new MissingRequiredRuleException("authority"));
        ShardingSpherePrivileges privileges = authorityRule.findPrivileges(grantee)
                .orElseThrow(() -> new SQLCheckException(String.format("Access denied for user '%s'@'%s'", grantee.getUsername(), grantee.getHostname())));
        ShardingSpherePreconditions.checkState(privileges.hasPrivileges(currentDatabase), () -> new SQLCheckException(String.format("Unknown database '%s'", currentDatabase)));
    }
    
    private String getHostAddress(final ChannelHandlerContext context) {
        SocketAddress socketAddress = context.channel().remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress().getHostAddress() : socketAddress.toString();
    }
    
    private void processCreateSubscription(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        if (!request.hasCreateSubscription()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss create subscription request body"))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        CreateSubscriptionRequest createSubscriptionRequest = request.getCreateSubscription();
        if (createSubscriptionRequest.getTableNamesList().isEmpty() || createSubscriptionRequest.getDatabase().isEmpty() || createSubscriptionRequest.getSubscriptionName().isEmpty()
                || createSubscriptionRequest.getSubscriptionMode() == SubscriptionMode.UNKNOWN) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Illegal create subscription request parameter"));
            return;
        }
        checkPrivileges(connectionContext.getCurrentUser().getGrantee(), createSubscriptionRequest.getDatabase());
        CDCResponse response = backendHandler.createSubscription(request);
        ctx.writeAndFlush(response);
    }
    
    private void processStartSubscription(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        if (!request.hasStartSubscription()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss start subscription request body"))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        StartSubscriptionRequest startSubscriptionRequest = request.getStartSubscription();
        if (startSubscriptionRequest.getDatabase().isEmpty() || startSubscriptionRequest.getSubscriptionName().isEmpty()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Illegal start subscription request parameter"))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        checkPrivileges(connectionContext.getCurrentUser().getGrantee(), startSubscriptionRequest.getDatabase());
        CDCResponse response = backendHandler.startSubscription(request, ctx.channel(), connectionContext);
        ctx.writeAndFlush(response);
    }
    
    private void processStopSubscription(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        StopSubscriptionRequest stopSubscriptionRequest = request.getStopSubscription();
        checkPrivileges(connectionContext.getCurrentUser().getGrantee(), stopSubscriptionRequest.getDatabase());
        backendHandler.stopSubscription(connectionContext.getJobId());
        connectionContext.setStatus(CDCConnectionStatus.LOGGED_IN);
        ctx.writeAndFlush(CDCResponseGenerator.succeedBuilder(request.getRequestId()).build());
    }
    
    private void processDropSubscription(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        DropSubscriptionRequest dropSubscriptionRequest = request.getDropSubscription();
        checkPrivileges(connectionContext.getCurrentUser().getGrantee(), dropSubscriptionRequest.getDatabase());
        try {
            backendHandler.dropSubscription(connectionContext.getJobId());
            ctx.writeAndFlush(CDCResponseGenerator.succeedBuilder(request.getRequestId()).build());
        } catch (final SQLException ex) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, ex.getMessage()));
        }
    }
    
    private void processAckRequest(final ChannelHandlerContext ctx, final CDCRequest request) {
        if (!request.hasAckRequest()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss ack request body")).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        AckRequest ackRequest = request.getAckRequest();
        if (ackRequest.getAckId().isEmpty()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Illegal ack request parameter"));
            return;
        }
        backendHandler.processAck(ackRequest);
    }
}
