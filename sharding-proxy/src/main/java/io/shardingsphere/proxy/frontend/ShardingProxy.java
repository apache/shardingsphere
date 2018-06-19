/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.proxy.frontend;

import io.netty.channel.WriteBufferWaterMark;
import io.shardingsphere.proxy.frontend.netty.ServerHandlerInitializer;

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
import io.shardingsphere.proxy.backend.ShardingProxyClient;
import io.shardingsphere.proxy.config.RuleRegistry;

import java.net.MalformedURLException;

/**
 * Sharding-Proxy.
 *
 * @author zhangliang
 * @author xiaoyu
 * @author wangkai
 */
public final class ShardingProxy {
    
    private static final int WORKER_MAX_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    
    private EventLoopGroup bossGroup;
    
    private EventLoopGroup workerGroup;
    
    private EventLoopGroup userGroup;
    
    /**
     * Start Sharding-Proxy.
     *
     * @param port port
     * @throws InterruptedException  interrupted exception
     * @throws MalformedURLException URL exception
     */
    public void start(final int port) throws InterruptedException, MalformedURLException {
        try {
            if (RuleRegistry.getInstance().isWithoutJdbc()) {
                ShardingProxyClient.getInstance().start();
            }
            ServerBootstrap bootstrap = new ServerBootstrap();
            bossGroup = createEventLoopGroup();
            if (bossGroup instanceof EpollEventLoopGroup) {
                groupsEpoll(bootstrap);
            } else {
                groupsNio(bootstrap);
            }
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            userGroup.shutdownGracefully();
            if (RuleRegistry.getInstance().isWithoutJdbc()) {
                ShardingProxyClient.getInstance().stop();
            }
        }
    }
    
    private EventLoopGroup createEventLoopGroup() {
        try {
            return new EpollEventLoopGroup(1);
        } catch (final UnsatisfiedLinkError ex) {
            return new NioEventLoopGroup(1);
        }
    }
    
    private void groupsEpoll(final ServerBootstrap bootstrap) {
        workerGroup = new EpollEventLoopGroup(WORKER_MAX_THREADS);
        userGroup = new EpollEventLoopGroup(WORKER_MAX_THREADS);
        bootstrap.group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class)
                .option(EpollChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024 * 1024, 16 * 1024 * 1024))
                .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer(userGroup));
    }
    
    private void groupsNio(final ServerBootstrap bootstrap) {
        workerGroup = new NioEventLoopGroup(WORKER_MAX_THREADS);
        userGroup = new NioEventLoopGroup(WORKER_MAX_THREADS);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024 * 1024, 16 * 1024 * 1024))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer(userGroup));
    }
}
