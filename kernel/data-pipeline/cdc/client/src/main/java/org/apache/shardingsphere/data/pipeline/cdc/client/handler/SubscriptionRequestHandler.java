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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.event.CreateSubscriptionEvent;
import org.apache.shardingsphere.data.pipeline.cdc.proto.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.proto.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.proto.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.proto.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.cdc.proto.request.StartSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.proto.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.proto.response.CDCResponse.Status;

import java.util.Arrays;
import java.util.UUID;

/**
 * Subscription request handler.
 */
@Slf4j
public final class SubscriptionRequestHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof CreateSubscriptionEvent) {
            Builder builder = CDCRequest.newBuilder();
            builder.setCreateSubscription(buildCreateSubscriptionRequest());
            builder.setRequestId(UUID.randomUUID().toString());
            ctx.writeAndFlush(builder.build());
        }
    }
    
    private CreateSubscriptionRequest buildCreateSubscriptionRequest() {
        return CreateSubscriptionRequest.newBuilder().setSubscriptionMode(SubscriptionMode.INCREMENTAL).setSubscriptionName("sharding_db").setDatabase("sharding_db")
                .addAllTableNames(Arrays.asList("t_order", "t_order_item")).build();
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCResponse response = (CDCResponse) msg;
        if (Status.SUCCEED == response.getStatus()) {
            processSucceed(ctx, response);
        } else {
            log.error("subscription response error {}", msg);
        }
    }
    
    private void processSucceed(final ChannelHandlerContext ctx, final CDCResponse response) {
        if (response.hasCreateSubscriptionResult()) {
            log.info("create subscription succeed, subcrption name {}", response.getCreateSubscriptionResult().getSubscriptionName());
            Builder builder = CDCRequest.newBuilder();
            builder.setStartSubscription(buildStartSubscriptionRequest(response.getCreateSubscriptionResult().getSubscriptionName()));
            builder.setRequestId(UUID.randomUUID().toString());
            ctx.writeAndFlush(builder.build());
        }
        // TODO waiting for pipeline refactoring finished
    }
    
    private StartSubscriptionRequest buildStartSubscriptionRequest(final String subscriptionName) {
        return StartSubscriptionRequest.newBuilder().setSubscriptionName(subscriptionName).build();
    }
}
