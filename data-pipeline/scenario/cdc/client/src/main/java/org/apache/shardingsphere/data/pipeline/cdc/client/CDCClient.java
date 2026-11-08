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

package org.apache.shardingsphere.data.pipeline.cdc.client;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.config.CDCClientConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.client.constant.ClientConnectionStatus;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.CDCRequestHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.ExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.ServerErrorResultHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.CDCLoginParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartStreamingParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.RequestIdUtils;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ResponseFuture;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.DropStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody.LoginType;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StopStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * CDC client.
 */
@RequiredArgsConstructor
@Slf4j
public final class CDCClient implements AutoCloseable {
    
    private final CDCClientConfiguration config;
    
    private NioEventLoopGroup group;
    
    private Channel channel;
    
    /**
     * Connect.
     *
     * @param dataConsumer data consumer
     * @param exceptionHandler exception handler
     * @param errorResultHandler error result handler
     */
    @SneakyThrows(InterruptedException.class)
    public void connect(final Consumer<List<Record>> dataConsumer, final ExceptionHandler exceptionHandler, final ServerErrorResultHandler errorResultHandler) {
        Bootstrap bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        bootstrap.channel(NioSocketChannel.class)
                .group(group)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    
                    @Override
                    protected void initChannel(final NioSocketChannel channel) {
                        channel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                        channel.pipeline().addLast(new ProtobufDecoder(CDCResponse.getDefaultInstance()));
                        channel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                        channel.pipeline().addLast(new ProtobufEncoder());
                        channel.pipeline().addLast(new CDCRequestHandler(dataConsumer, exceptionHandler, errorResultHandler));
                    }
                });
        channel = bootstrap.connect(config.getAddress(), config.getPort()).sync().channel();
    }
    
    /**
     * Await channel close.
     *
     * @throws InterruptedException interrupted exception
     */
    public void await() throws InterruptedException {
        channel.closeFuture().sync();
    }
    
    /**
     * Login.
     *
     * @param parameter parameter
     * @throws IllegalStateException the channel is not active
     */
    public synchronized void login(final CDCLoginParameter parameter) {
        Preconditions.checkState(null != channel && channel.isActive(), "The channel is not active, call the `connect` method first.");
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        Preconditions.checkState(ClientConnectionStatus.LOGGED_IN != connectionContext.getStatus().get(), "The client is already logged in.");
        LoginRequestBody loginRequestBody = LoginRequestBody.newBuilder().setType(LoginType.BASIC).setBasicBody(BasicBody.newBuilder()
                .setUsername(parameter.getUsername()).setPassword(Hashing.sha256().hashBytes(parameter.getPassword().getBytes()).toString().toUpperCase()).build()).build();
        String requestId = RequestIdUtils.generateRequestId();
        CDCRequest data = CDCRequest.newBuilder().setType(Type.LOGIN).setVersion(1).setRequestId(requestId).setLoginRequestBody(loginRequestBody).build();
        ResponseFuture responseFuture = new ResponseFuture(requestId, Type.LOGIN);
        connectionContext.getResponseFutureMap().put(requestId, responseFuture);
        channel.writeAndFlush(data);
        responseFuture.waitResponseResult(config.getTimeoutMillis(), connectionContext);
        log.info("Login success, username: {}", parameter.getUsername());
    }
    
    /**
     * Start streaming.
     *
     * @param parameter parameter
     * @return streaming ID
     */
    public String startStreaming(final StartStreamingParameter parameter) {
        StreamDataRequestBody streamDataRequestBody = StreamDataRequestBody.newBuilder()
                .setDatabase(parameter.getDatabase()).setFull(parameter.isFull()).addAllSourceSchemaTable(parameter.getSchemaTables()).build();
        String requestId = RequestIdUtils.generateRequestId();
        CDCRequest request = CDCRequest.newBuilder().setRequestId(requestId).setType(Type.STREAM_DATA).setStreamDataRequestBody(streamDataRequestBody).build();
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        ResponseFuture responseFuture = new ResponseFuture(requestId, Type.STREAM_DATA);
        connectionContext.getResponseFutureMap().put(requestId, responseFuture);
        channel.writeAndFlush(request);
        log.info("Sending start streaming request, param: {}, timeout: {} s", parameter, TimeUnit.MILLISECONDS.toSeconds(config.getTimeoutMillis()));
        String result = responseFuture.waitResponseResult(config.getTimeoutMillis(), connectionContext).toString();
        log.info("Start streaming success, streaming id: {}", result);
        return result;
    }
    
    /**
     * Restart streaming.
     *
     * @param streamingId streaming ID
     */
    public void restartStreaming(final String streamingId) {
        String requestId = RequestIdUtils.generateRequestId();
        StartStreamingRequestBody body = StartStreamingRequestBody.newBuilder().setStreamingId(streamingId).build();
        CDCRequest request = CDCRequest.newBuilder().setRequestId(requestId).setType(Type.START_STREAMING).setStartStreamingRequestBody(body).build();
        ResponseFuture responseFuture = new ResponseFuture(requestId, Type.START_STREAMING);
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        connectionContext.getResponseFutureMap().put(requestId, responseFuture);
        channel.writeAndFlush(request);
        responseFuture.waitResponseResult(config.getTimeoutMillis(), connectionContext);
        log.info("Restart streaming success, streaming id: {}", streamingId);
    }
    
    /**
     * Stop streaming.
     *
     * @param streamingId streaming ID
     */
    public void stopStreaming(final String streamingId) {
        String requestId = RequestIdUtils.generateRequestId();
        StopStreamingRequestBody body = StopStreamingRequestBody.newBuilder().setStreamingId(streamingId).build();
        CDCRequest request = CDCRequest.newBuilder().setRequestId(requestId).setType(Type.STOP_STREAMING).setStopStreamingRequestBody(body).build();
        ResponseFuture responseFuture = new ResponseFuture(requestId, Type.STOP_STREAMING);
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        connectionContext.getResponseFutureMap().put(requestId, responseFuture);
        channel.writeAndFlush(request);
        responseFuture.waitResponseResult(config.getTimeoutMillis(), connectionContext);
        connectionContext.getStreamingIds().remove(streamingId);
        log.info("Stop streaming success, streaming id: {}", streamingId);
    }
    
    /**
     * Drop streaming.
     *
     * @param streamingId streaming ID
     */
    public void dropStreaming(final String streamingId) {
        String requestId = RequestIdUtils.generateRequestId();
        DropStreamingRequestBody body = DropStreamingRequestBody.newBuilder().setStreamingId(streamingId).build();
        CDCRequest request = CDCRequest.newBuilder().setRequestId(requestId).setType(Type.DROP_STREAMING).setDropStreamingRequestBody(body).build();
        ResponseFuture responseFuture = new ResponseFuture(requestId, Type.DROP_STREAMING);
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        connectionContext.getResponseFutureMap().put(requestId, responseFuture);
        channel.writeAndFlush(request);
        responseFuture.waitResponseResult(config.getTimeoutMillis(), connectionContext);
        connectionContext.getStreamingIds().remove(streamingId);
        log.info("Drop streaming success, streaming id: {}", streamingId);
    }
    
    @Override
    public void close() {
        if (null != channel) {
            channel.close().awaitUninterruptibly();
        }
        if (null != group) {
            group.shutdownGracefully();
        }
    }
}
