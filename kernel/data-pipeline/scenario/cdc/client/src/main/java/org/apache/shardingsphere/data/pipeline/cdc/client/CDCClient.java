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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.context.ClientConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.CDCRequestHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.LoginRequestHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StartStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StopStreamingRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * CDC client.
 */
@Slf4j
public final class CDCClient {
    
    private final StartCDCClientParameter parameter;
    
    private final Consumer<List<Record>> consumer;
    
    private NioEventLoopGroup group;
    
    private Channel channel;
    
    public CDCClient(final StartCDCClientParameter parameter, final Consumer<List<Record>> consumer) {
        validateParameter(parameter);
        this.parameter = parameter;
        this.consumer = consumer;
    }
    
    private void validateParameter(final StartCDCClientParameter parameter) {
        if (null == parameter.getDatabase() || parameter.getDatabase().isEmpty()) {
            throw new IllegalArgumentException("The database parameter can't be null");
        }
        if (null == parameter.getUsername() || parameter.getUsername().isEmpty()) {
            throw new IllegalArgumentException("The username parameter can't be null");
        }
        if (null == parameter.getAddress() || parameter.getAddress().isEmpty()) {
            throw new IllegalArgumentException("The address parameter can't be null");
        }
        if (null == parameter.getSchemaTables() || parameter.getSchemaTables().isEmpty()) {
            throw new IllegalArgumentException("The schema tables parameter can't be null");
        }
    }
    
    /**
     * Start ShardingSphere CDC client.
     */
    public void start() {
        if (null != channel && channel.isActive()) {
            stop();
        }
        startInternal(parameter.getAddress(), parameter.getPort());
    }
    
    @SneakyThrows(InterruptedException.class)
    private void startInternal(final String address, final int port) {
        Bootstrap bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
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
                        channel.pipeline().addLast(new LoginRequestHandler(parameter.getUsername(), parameter.getPassword()));
                        channel.pipeline().addLast(new CDCRequestHandler(parameter, consumer));
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(address, port).sync();
            channel = future.channel();
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    
    /**
     * Restart streaming.
     *
     * @throws IllegalStateException the channel is not active
     */
    public void restartStreaming() {
        Optional<String> optional = findStreamingIdFromContext();
        if (!optional.isPresent()) {
            log.info("The streaming id not exist, ignored");
            return;
        }
        StartStreamingRequestBody body = StartStreamingRequestBody.newBuilder().setStreamingId(optional.get()).build();
        CDCRequest request = CDCRequest.newBuilder().setRequestId(UUID.randomUUID().toString()).setType(Type.START_STREAMING).setStartStreamingRequestBody(body).build();
        channel.writeAndFlush(request);
    }
    
    private Optional<String> findStreamingIdFromContext() {
        if (null == channel || !channel.isActive()) {
            throw new IllegalStateException("The channel is not active, please start the client first");
        }
        ClientConnectionContext connectionContext = channel.attr(ClientConnectionContext.CONTEXT_KEY).get();
        if (null == connectionContext) {
            log.warn("The connection context not exist");
            return Optional.empty();
        }
        return Optional.ofNullable(connectionContext.getStreamingId());
    }
    
    /**
     * Stop streaming.
     *
     * @throws IllegalStateException the channel is not active
     */
    public void stopStreaming() {
        Optional<String> optional = findStreamingIdFromContext();
        if (!optional.isPresent()) {
            log.info("The streaming id not exist, ignored");
            return;
        }
        StopStreamingRequestBody body = StopStreamingRequestBody.newBuilder().setStreamingId(optional.get()).build();
        CDCRequest request = CDCRequest.newBuilder().setRequestId(UUID.randomUUID().toString()).setType(Type.STOP_STREAMING).setStopStreamingRequestBody(body).build();
        channel.writeAndFlush(request);
    }
    
    /**
     * Stop ShardingSphere CDC client.
     */
    public void stop() {
        if (null != channel) {
            channel.close().awaitUninterruptibly();
        }
        if (null != group) {
            group.shutdownGracefully();
        }
    }
}
