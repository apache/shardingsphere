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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.cdc.CDCBackendHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Collection;
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
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCConnectionContext connectionContext = ctx.channel().attr(CONNECTION_CONTEXT_KEY).get();
        CDCConnectionStatus status = connectionContext.getStatus();
        CDCRequest request = (CDCRequest) msg;
        if (CDCConnectionStatus.NOT_LOGGED_IN == status) {
            processLogin(ctx, request);
            return;
        }
        switch (request.getRequestCase()) {
            case CREATE_SUBSCRIPTION:
                processCreateSubscription(ctx, request);
                break;
            case START_SUBSCRIPTION:
                processStartSubscription(ctx, request, connectionContext);
                break;
            case STOP_SUBSCRIPTION:
                stopStartSubscription(ctx, request, connectionContext);
                break;
            case DROP_SUBSCRIPTION:
                dropStartSubscription(ctx, request, connectionContext);
                break;
            case ACK_REQUEST:
                processAckRequest(ctx, request);
                break;
            default:
                log.warn("Cannot handle this type of request {}", request);
        }
    }
    
    private void processLogin(final ChannelHandlerContext ctx, final CDCRequest request) {
        if (!request.hasLogin() || !request.getLogin().hasBasicBody()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss login request body")).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        BasicBody body = request.getLogin().getBasicBody();
        Collection<ShardingSphereRule> globalRules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules();
        Optional<AuthorityRule> authorityRule = globalRules.stream().filter(rule -> rule instanceof AuthorityRule).map(rule -> (AuthorityRule) rule).findFirst();
        if (!authorityRule.isPresent()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, "Not find authority rule")).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        Optional<ShardingSphereUser> user = authorityRule.get().findUser(new Grantee(body.getUsername(), getHostAddress(ctx)));
        if (user.isPresent() && Objects.equals(Hashing.sha256().hashBytes(user.get().getPassword().getBytes()).toString().toUpperCase(), body.getPassword())) {
            ctx.channel().attr(CONNECTION_CONTEXT_KEY).get().setStatus(CDCConnectionStatus.LOGGED_IN);
            ctx.writeAndFlush(CDCResponseGenerator.succeedBuilder(request.getRequestId()).build());
            return;
        }
        ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, "Incorrect username or password")).addListener(ChannelFutureListener.CLOSE);
    }
    
    private String getHostAddress(final ChannelHandlerContext context) {
        SocketAddress socketAddress = context.channel().remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress().getHostAddress() : socketAddress.toString();
    }
    
    private void processCreateSubscription(final ChannelHandlerContext ctx, final CDCRequest request) {
        if (!request.hasCreateSubscription()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss create subscription request body"))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        CreateSubscriptionRequest createSubscriptionRequest = request.getCreateSubscription();
        if (createSubscriptionRequest.getTableNamesList().isEmpty() || createSubscriptionRequest.getDatabase().isEmpty() || createSubscriptionRequest.getSubscriptionName().isEmpty()
                || createSubscriptionRequest.getSubscriptionMode() == SubscriptionMode.UNKNOWN) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Illegal create subscription request parameter"))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
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
        CDCResponse response = backendHandler.startSubscription(request, ctx.channel(), connectionContext);
        ctx.writeAndFlush(response);
    }
    
    private void stopStartSubscription(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        backendHandler.stopSubscription(connectionContext.getJobId());
        connectionContext.setStatus(CDCConnectionStatus.LOGGED_IN);
        ctx.writeAndFlush(CDCResponseGenerator.succeedBuilder(request.getRequestId()).build());
    }
    
    private void dropStartSubscription(final ChannelHandlerContext ctx, final CDCRequest request, final CDCConnectionContext connectionContext) {
        try {
            backendHandler.dropSubscription(connectionContext.getJobId());
            ctx.writeAndFlush(CDCResponseGenerator.succeedBuilder(request.getRequestId()).build());
        } catch (final SQLException ex) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.SERVER_ERROR, ex.getMessage()));
        }
    }
    
    private void processAckRequest(final ChannelHandlerContext ctx, final CDCRequest request) {
        if (!request.hasAckRequest()) {
            ctx.writeAndFlush(CDCResponseGenerator.failed(request.getRequestId(), CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR, "Miss ack request body"))
                    .addListener(ChannelFutureListener.CLOSE);
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
