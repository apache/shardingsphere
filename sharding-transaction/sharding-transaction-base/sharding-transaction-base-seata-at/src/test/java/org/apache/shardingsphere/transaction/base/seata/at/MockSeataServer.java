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

package org.apache.shardingsphere.transaction.base.seata.at;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.seata.common.XID;
import io.seata.core.rpc.netty.MessageCodecHandler;
import io.seata.core.rpc.netty.NettyServerConfig;
import io.seata.discovery.registry.RegistryFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mock seata server for unit test.
 *
 * @author zhaojun
 */
@Slf4j
public final class MockSeataServer {
    
    private final ServerBootstrap serverBootstrap;
    
    private final EventLoopGroup eventLoopGroupBoss;
    
    private final EventLoopGroup eventLoopGroupWorker;
    
    private final NettyServerConfig nettyServerConfig;
    
    private int listenPort;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    public MockSeataServer() {
        this.serverBootstrap = new ServerBootstrap();
        this.nettyServerConfig = new NettyServerConfig();
        this.eventLoopGroupBoss = new NioEventLoopGroup(1);
        this.eventLoopGroupWorker = new NioEventLoopGroup();
        listenPort = 8091;
    }
    
    /**
     * start.
     */
    public void start() {
        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupWorker)
            .channel(nettyServerConfig.SERVER_CHANNEL_CLAZZ)
            .option(ChannelOption.SO_BACKLOG, nettyServerConfig.getSoBackLogSize())
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSendBufSize())
            .childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketResvBufSize())
            .localAddress(new InetSocketAddress(listenPort))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(final SocketChannel ch) {
                    ch.pipeline().addLast(new IdleStateHandler(nettyServerConfig.getChannelMaxReadIdleSeconds(), 0, 0))
                        .addLast(new MessageCodecHandler())
                        .addLast(new MockCommandHandler());
                }
            });
        try {
            log.info("Server started ... ");
            RegistryFactory.getInstance().register(new InetSocketAddress(XID.getIpAddress(), XID.getPort()));
            initialized.set(true);
            ChannelFuture future = this.serverBootstrap.bind(listenPort).sync();
            future.channel().closeFuture().sync();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * shutdown.
     */
    @SneakyThrows
    public void shutdown() {
        if (initialized.get()) {
            RegistryFactory.getInstance().unregister(new InetSocketAddress(XID.getIpAddress(), XID.getPort()));
            RegistryFactory.getInstance().close();
            TimeUnit.SECONDS.sleep(nettyServerConfig.getServerShutdownWaitTime());
        }
        this.eventLoopGroupBoss.shutdownGracefully();
        this.eventLoopGroupWorker.shutdownGracefully();
    }
}
