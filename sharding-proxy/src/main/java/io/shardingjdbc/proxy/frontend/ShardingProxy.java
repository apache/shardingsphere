/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.proxy.frontend;

import com.google.common.base.StandardSystemProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.shardingjdbc.proxy.frontend.netty.ServerHandlerInitializer;

import java.util.Objects;

/**
 * Sharding-Proxy.
 *
 * @author zhangliang
 * @author xiaoyu
 */
public final class ShardingProxy {
    
    private static final int WORK_MAX_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    
    private static final String OS_NAME = "Linux";
    
    private EventLoopGroup bossGroup;
    
    private EventLoopGroup workerGroup;
    
    private EventLoopGroup userGroup;
    
    /**
     * Start Sharding-Proxy.
     *
     * @param port port
     * @throws InterruptedException interrupted exception
     */
    public void start(final int port) throws InterruptedException {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            groups(bootstrap, WORK_MAX_THREADS);
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            userGroup.shutdownGracefully();
        }
    }
    
    private void groups(final ServerBootstrap bootstrap, final int workThreads) {
        if (Objects.equals(StandardSystemProperty.OS_NAME.value(), OS_NAME)) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(workThreads);
            userGroup = new EpollEventLoopGroup(workThreads);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .option(EpollChannelOption.TCP_CORK, true)
                    .option(EpollChannelOption.SO_KEEPALIVE, true)
                    .option(EpollChannelOption.SO_BACKLOG, 128)
                    .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerHandlerInitializer(userGroup));
        } else {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(workThreads);
            userGroup = new NioEventLoopGroup(workThreads);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerHandlerInitializer(userGroup));
        }
    }
}
