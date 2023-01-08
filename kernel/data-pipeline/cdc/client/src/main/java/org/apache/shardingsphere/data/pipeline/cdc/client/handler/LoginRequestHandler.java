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

package org.apache.shardingsphere.data.pipeline.cdc.client.handler;

import com.google.common.hash.Hashing;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.constant.ClientConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.event.CreateSubscriptionEvent;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.RequestIdUtil;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest.LoginType;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;

/**
 * Login request handler.
 */
@Slf4j
@RequiredArgsConstructor
public final class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    
    private final String username;
    
    private final String password;
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ClientConnectionContext context = new ClientConnectionContext();
        context.setStatus(ClientConnectionStatus.CONNECTED);
        ctx.channel().attr(ClientConnectionContext.CONTEXT_KEY).setIfAbsent(context);
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.channel().attr(ClientConnectionContext.CONTEXT_KEY).setIfAbsent(null);
        ctx.fireChannelInactive();
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCResponse response = (CDCResponse) msg;
        ClientConnectionContext connectionContext = ctx.channel().attr(ClientConnectionContext.CONTEXT_KEY).get();
        switch (connectionContext.getStatus()) {
            case CONNECTED:
                setLogindRequest(ctx, response, connectionContext);
                break;
            case NOT_LOGGED_IN:
                sendSubscriptionEvent(ctx, response, connectionContext);
                break;
            default:
                ctx.fireChannelRead(msg);
        }
    }
    
    private void setLogindRequest(final ChannelHandlerContext ctx, final CDCResponse response, final ClientConnectionContext connectionContext) {
        ServerGreetingResult serverGreetingResult = response.getServerGreetingResult();
        log.info("Server greeting result, server version: {}, protocol version: {}", serverGreetingResult.getServerVersion(), serverGreetingResult.getProtocolVersion());
        String encryptPassword = Hashing.sha256().hashBytes(password.getBytes()).toString().toUpperCase();
        LoginRequest loginRequest = LoginRequest.newBuilder().setType(LoginType.BASIC).setBasicBody(BasicBody.newBuilder().setUsername(username).setPassword(encryptPassword).build()).build();
        String loginRequestId = RequestIdUtil.generateRequestId();
        CDCRequest data = CDCRequest.newBuilder().setType(Type.LOGIN).setVersion(1).setRequestId(loginRequestId).setLogin(loginRequest).build();
        ctx.writeAndFlush(data);
        connectionContext.setStatus(ClientConnectionStatus.NOT_LOGGED_IN);
    }
    
    private void sendSubscriptionEvent(final ChannelHandlerContext ctx, final CDCResponse response, final ClientConnectionContext connectionContext) {
        if (response.getStatus() == Status.SUCCEED) {
            log.info("login success, username {}", username);
            connectionContext.setStatus(ClientConnectionStatus.LOGGING_IN);
            ctx.fireUserEventTriggered(new CreateSubscriptionEvent());
        } else {
            log.error("login failed, username {}", username);
        }
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("login handler error", cause);
    }
}
