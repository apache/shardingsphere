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

package org.apache.shardingsphere.transaction.base.seata.at.fixture;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.seata.core.rpc.netty.v1.ProtocolV1Decoder;
import io.seata.core.rpc.netty.v1.ProtocolV1Encoder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mock seata server for unit test.
 */
@Slf4j
public final class MockSeataServer {
    
    private final ServerBootstrap bootstrap;
    
    private final EventLoopGroup bossGroup;
    
    private final EventLoopGroup workerGroup;
    
    private final int port;
    
    @Getter
    private final MockMessageHandler messageHandler = new MockMessageHandler();
    
    @Getter
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    public MockSeataServer() {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        port = 8891;
    }
    
    /**
     * start.
     */
    @SneakyThrows
    public void start() {
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 128)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(final SocketChannel socketChannel) {
                    socketChannel.pipeline()
                        .addLast(new ProtocolV1Decoder())
                        .addLast(new ProtocolV1Encoder())
                        .addLast(messageHandler);
                }
            });
        ChannelFuture future = bootstrap.bind(port).sync();
        initialized.set(true);
        log.info("mock seata server have started");
        future.channel().closeFuture().sync();
    }
    
    /**
     * shutdown.
     */
    @SneakyThrows
    public void shutdown() {
        if (initialized.get()) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
