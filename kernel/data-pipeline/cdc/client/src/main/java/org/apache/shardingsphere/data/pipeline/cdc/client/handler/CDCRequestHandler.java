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
import org.apache.shardingsphere.data.pipeline.cdc.client.event.StreamDataEvent;
import org.apache.shardingsphere.data.pipeline.cdc.client.importer.DataSourceImporter;
import org.apache.shardingsphere.data.pipeline.cdc.client.importer.Importer;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.RequestIdUtil;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.AckStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;

import java.util.List;

/**
 * CDC request handler.
 */
@Slf4j
public final class CDCRequestHandler extends ChannelInboundHandlerAdapter {
    
    private final StartCDCClientParameter parameter;
    
    private final Importer importer;
    
    public CDCRequestHandler(final StartCDCClientParameter parameter) {
        this.parameter = parameter;
        importer = new DataSourceImporter(parameter.getDatabaseType(), parameter.getImportDataSourceParameter());
    }
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof StreamDataEvent) {
            StreamDataRequestBody streamDataRequestBody = StreamDataRequestBody.newBuilder().setDatabase(parameter.getDatabase()).setFull(parameter.isFull())
                    .addAllSourceSchemaTables(parameter.getSchemaTables()).build();
            CDCRequest request = CDCRequest.newBuilder().setRequestId(RequestIdUtil.generateRequestId()).setType(Type.STREAM_DATA).setStreamDataRequestBody(streamDataRequestBody).build();
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
        if (response.hasStreamDataResult()) {
            StreamDataResult streamDataResult = response.getStreamDataResult();
            connectionContext.setStreamingId(streamDataResult.getStreamingId());
            connectionContext.setStatus(ClientConnectionStatus.STREAMING);
        } else if (response.hasDataRecordResult()) {
            processDataRecords(ctx, response.getDataRecordResult());
        }
    }
    
    // TODO not remove the method, may be used again in the future
    private void sendStartStreamingDataRequest(final ChannelHandlerContext ctx, final String streamingId, final ClientConnectionContext connectionContext) {
        StartStreamingRequestBody startStreamingRequest = StartStreamingRequestBody.newBuilder().setStreamingId(streamingId).build();
        Builder builder = CDCRequest.newBuilder().setRequestId(RequestIdUtil.generateRequestId()).setType(Type.START_STREAMING).setStartStreamingRequestBody(startStreamingRequest);
        ctx.writeAndFlush(builder.build());
        connectionContext.setStatus(ClientConnectionStatus.STREAMING);
    }
    
    private void processDataRecords(final ChannelHandlerContext ctx, final DataRecordResult result) {
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
        ctx.channel().writeAndFlush(CDCRequest.newBuilder().setType(Type.ACK_STREAMING).setAckStreamingRequestBody(AckStreamingRequestBody.newBuilder().setAckId(result.getAckId()).build()).build());
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        importer.close();
        ctx.fireChannelInactive();
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error("handler data streaming error", cause);
        // TODO passing error messages to the caller
    }
}
