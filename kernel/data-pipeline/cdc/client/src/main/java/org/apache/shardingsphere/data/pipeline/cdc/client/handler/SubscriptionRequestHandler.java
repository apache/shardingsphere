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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.constant.ClientConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.event.CreateSubscriptionEvent;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.RequestIdUtil;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.TableName;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;

import java.util.List;

/**
 * Subscription request handler.
 */
@Slf4j
@RequiredArgsConstructor
public final class SubscriptionRequestHandler extends ChannelInboundHandlerAdapter {
    
    private final String database;
    
    private final String subscriptionName;
    
    private final List<TableName> subscribeTables;
    
    private final SubscriptionMode subscribeMode;
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof CreateSubscriptionEvent) {
            CreateSubscriptionRequest createSubscriptionRequest = CreateSubscriptionRequest.newBuilder().setDatabase(database).setSubscriptionMode(subscribeMode).setSubscriptionName(subscriptionName)
                    .addAllTableNames(subscribeTables).build();
            CDCRequest request = CDCRequest.newBuilder().setCreateSubscription(createSubscriptionRequest).setRequestId(RequestIdUtil.generateRequestId()).build();
            ctx.writeAndFlush(request);
        }
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCResponse response = (CDCResponse) msg;
        if (response.getStatus() == Status.FAILED) {
            log.error("received error response {}", msg);
        }
        ClientConnectionContext connectionContext = ctx.channel().attr(ClientConnectionContext.CONTEXT_KEY).get();
        if (connectionContext.getStatus() == ClientConnectionStatus.LOGGING_IN) {
            if (!response.hasCreateSubscriptionResult()) {
                log.error("not find the create subscription result");
                return;
            }
            sendCreateSubscriptionRequest(ctx, response, connectionContext);
        } else if (connectionContext.getStatus() == ClientConnectionStatus.CREATING_SUBSCRIPTION) {
            startSubscription(response, connectionContext);
        } else {
            subscribeDataRecords(ctx);
        }
    }
    
    private void sendCreateSubscriptionRequest(final ChannelHandlerContext ctx, final CDCResponse response, final ClientConnectionContext connectionContext) {
        log.info("create subscription succeed, subscription name {}, exist {}", response.getCreateSubscriptionResult().getSubscriptionName(), response.getCreateSubscriptionResult().getExisting());
        StartSubscriptionRequest startSubscriptionRequest = StartSubscriptionRequest.newBuilder().setDatabase(database).setSubscriptionName(subscriptionName).build();
        Builder builder = CDCRequest.newBuilder().setRequestId(RequestIdUtil.generateRequestId()).setStartSubscription(startSubscriptionRequest);
        ctx.writeAndFlush(builder.build());
        connectionContext.setStatus(ClientConnectionStatus.CREATING_SUBSCRIPTION);
    }
    
    private void startSubscription(final CDCResponse response, final ClientConnectionContext connectionContext) {
        log.info("start subscription succeed, subscription name {}", response.getCreateSubscriptionResult().getSubscriptionName());
        connectionContext.setStatus(ClientConnectionStatus.SUBSCRIBING);
    }
    
    private void subscribeDataRecords(final ChannelHandlerContext ctx) {
        // TODO to be implemented
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("subscription handler error", cause);
        // TODO passing error messages to the caller
    }
}
