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
import org.apache.shardingsphere.data.pipeline.cdc.client.constant.ClientConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.event.CreateSubscriptionEvent;
import org.apache.shardingsphere.data.pipeline.cdc.client.importer.DataSourceImporter;
import org.apache.shardingsphere.data.pipeline.cdc.client.importer.Importer;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.RequestIdUtil;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartSubscriptionRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.util.List;

/**
 * Subscription request handler.
 */
@Slf4j
public final class SubscriptionRequestHandler extends ChannelInboundHandlerAdapter {
    
    private final StartCDCClientParameter parameter;
    
    private final Importer importer;
    
    public SubscriptionRequestHandler(final StartCDCClientParameter parameter) {
        this.parameter = parameter;
        importer = new DataSourceImporter(parameter.getDatabaseType(), parameter.getImportDataSourceParameter());
    }
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof CreateSubscriptionEvent) {
            CreateSubscriptionRequest createSubscriptionRequest = CreateSubscriptionRequest.newBuilder().setDatabase(parameter.getDatabase()).setSubscriptionMode(parameter.getSubscriptionMode())
                    .setSubscriptionName(parameter.getSubscriptionName()).addAllTableNames(parameter.getSubscribeTables()).setIncrementalGlobalOrderly(parameter.isIncrementalGlobalOrderly()).build();
            CDCRequest request = CDCRequest.newBuilder().setCreateSubscription(createSubscriptionRequest).setRequestId(RequestIdUtil.generateRequestId()).build();
            ctx.writeAndFlush(request);
        }
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCResponse response = (CDCResponse) msg;
        if (response.getStatus() != Status.SUCCEED) {
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
            subscribeDataRecords(ctx, response.getDataRecordResult());
        }
    }
    
    private void sendCreateSubscriptionRequest(final ChannelHandlerContext ctx, final CDCResponse response, final ClientConnectionContext connectionContext) {
        log.info("create subscription succeed, subscription name {}, exist {}", response.getCreateSubscriptionResult().getSubscriptionName(), response.getCreateSubscriptionResult().getExisting());
        StartSubscriptionRequest startSubscriptionRequest = StartSubscriptionRequest.newBuilder().setDatabase(parameter.getDatabase()).setSubscriptionName(parameter.getSubscriptionName()).build();
        Builder builder = CDCRequest.newBuilder().setRequestId(RequestIdUtil.generateRequestId()).setStartSubscription(startSubscriptionRequest);
        ctx.writeAndFlush(builder.build());
        connectionContext.setStatus(ClientConnectionStatus.CREATING_SUBSCRIPTION);
    }
    
    private void startSubscription(final CDCResponse response, final ClientConnectionContext connectionContext) {
        log.info("start subscription succeed, subscription name {}", response.getCreateSubscriptionResult().getSubscriptionName());
        connectionContext.setStatus(ClientConnectionStatus.SUBSCRIBING);
    }
    
    private void subscribeDataRecords(final ChannelHandlerContext ctx, final DataRecordResult result) {
        List<Record> recordsList = result.getRecordsList();
        for (Record each : recordsList) {
            try {
                importer.write(each);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new RuntimeException(ex);
            }
        }
        // TODO data needs to be processed, such as writing to a database
        ctx.channel().writeAndFlush(CDCRequest.newBuilder().setAckRequest(AckRequest.newBuilder().setAckId(result.getAckId()).build()).build());
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        importer.close();
        ctx.fireChannelInactive();
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("subscription handler error", cause);
        // TODO passing error messages to the caller
    }
}
