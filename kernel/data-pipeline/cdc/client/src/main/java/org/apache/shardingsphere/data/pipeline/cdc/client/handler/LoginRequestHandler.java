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
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Objects;

/**
 * Login request handler.
 */
@Slf4j
@RequiredArgsConstructor
public final class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    
    private static final AttributeKey<String> LOGIN_REQUEST_ID_KEY = AttributeKey.valueOf("login.request.id");
    
    private final String username;
    
    private final String password;
    
    private boolean loggedIn;
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        ctx.channel().attr(LOGIN_REQUEST_ID_KEY).set(null);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (loggedIn) {
            ctx.fireChannelRead(msg);
            return;
        }
        CDCResponse response = (CDCResponse) msg;
        if (response.hasServerGreetingResult()) {
            ServerGreetingResult serverGreetingResult = response.getServerGreetingResult();
            log.info("Server greeting result, server version: {}, min protocol version: {}", serverGreetingResult.getServerVersion(), serverGreetingResult.getProtocolVersion());
            sendLoginRequest(ctx);
            return;
        }
        if (Status.FAILED == response.getStatus()) {
            log.error("login failed, {}", msg);
            return;
        }
        if (Status.SUCCEED == response.getStatus() && Objects.equals(ctx.channel().attr(LOGIN_REQUEST_ID_KEY).get(), response.getRequestId())) {
            log.info("login success, username {}", username);
            loggedIn = true;
            ctx.fireUserEventTriggered(new CreateSubscriptionEvent());
        }
    }
    
    private void sendLoginRequest(final ChannelHandlerContext ctx) {
        String encryptPassword = Hashing.sha256().hashBytes(password.getBytes()).toString().toUpperCase();
        LoginRequest loginRequest = LoginRequest.newBuilder().setType(LoginType.BASIC).setBasicBody(BasicBody.newBuilder().setUsername(username).setPassword(encryptPassword).build()).build();
        String loginRequestId = RequestIdUtil.generateRequestId();
        ctx.channel().attr(LOGIN_REQUEST_ID_KEY).setIfAbsent(loginRequestId);
        CDCRequest data = CDCRequest.newBuilder().setType(Type.LOGIN).setVersion(1).setRequestId(loginRequestId).setLogin(loginRequest).build();
        ctx.writeAndFlush(data);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("login handler error", cause);
    }
}
