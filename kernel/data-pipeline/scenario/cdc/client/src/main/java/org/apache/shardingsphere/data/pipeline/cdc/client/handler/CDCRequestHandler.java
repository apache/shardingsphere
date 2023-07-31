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
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ResponseFuture;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * CDC request handler.
 */
@RequiredArgsConstructor
@Slf4j
public final class CDCRequestHandler extends ChannelInboundHandlerAdapter {
    
    private final Consumer<List<Record>> consumer;
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        CDCResponse response = (CDCResponse) msg;
        ClientConnectionContext connectionContext = ctx.channel().attr(ClientConnectionContext.CONTEXT_KEY).get();
        Optional<ResponseFuture> responseFuture = Optional.ofNullable(connectionContext.getResponseFutureMap().get(response.getRequestId()));
        if (response.hasStreamDataResult()) {
            StreamDataResult streamDataResult = response.getStreamDataResult();
            responseFuture.ifPresent(future -> future.setResult(response.getStreamDataResult().getStreamingId()));
            connectionContext.getStreamingIds().add(streamDataResult.getStreamingId());
        } else if (response.hasDataRecordResult()) {
            processDataRecords(ctx, response.getDataRecordResult());
        }
        responseFuture.ifPresent(future -> future.getCountDownLatch().countDown());
    }
    
    private void processDataRecords(final ChannelHandlerContext ctx, final DataRecordResult result) {
        consumer.accept(result.getRecordList());
        ctx.channel().writeAndFlush(CDCRequest.newBuilder().setType(Type.ACK_STREAMING).setAckStreamingRequestBody(AckStreamingRequestBody.newBuilder().setAckId(result.getAckId()).build()).build());
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        log.info("Channel inactive, stop data streaming");
        ctx.fireChannelInactive();
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("Handler data streaming error", cause);
        // TODO passing error messages to the caller
    }
}
