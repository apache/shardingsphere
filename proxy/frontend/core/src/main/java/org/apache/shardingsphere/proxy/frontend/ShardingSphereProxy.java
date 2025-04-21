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

package org.apache.shardingsphere.proxy.frontend;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.netty.ServerHandlerInitializer;
import org.apache.shardingsphere.proxy.frontend.protocol.FrontDatabaseProtocolTypeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ShardingSphere-Proxy.
 */
@Slf4j
public final class ShardingSphereProxy {
    
    private final EventLoopGroup bossGroup;
    
    private final EventLoopGroup workerGroup;
    
    private boolean isClosed;
    
    public ShardingSphereProxy() {
        bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        workerGroup = getWorkerGroup();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }
    
    private EventLoopGroup getWorkerGroup() {
        int workerThreads = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE);
        return Epoll.isAvailable() ? new EpollEventLoopGroup(workerThreads) : new NioEventLoopGroup(workerThreads);
    }
    
    /**
     * Start ShardingSphere-Proxy.
     *
     * @param port port
     * @param addresses addresses
     */
    @SneakyThrows(InterruptedException.class)
    public void start(final int port, final List<String> addresses) {
        try {
            List<ChannelFuture> futures = startInternal(port, addresses);
            accept(futures);
        } finally {
            close();
        }
    }
    
    /**
     * Start ShardingSphere-Proxy with DomainSocket.
     *
     * @param socketPath socket path
     */
    public void start(final String socketPath) {
        if (!Epoll.isAvailable()) {
            log.error("Epoll is unavailable, DomainSocket can't start.");
            return;
        }
        ChannelFuture future = startDomainSocket(socketPath);
        future.addListener((ChannelFutureListener) futureParams -> {
            if (futureParams.isSuccess()) {
                log.info("The listening address for DomainSocket is {}", socketPath);
            } else {
                log.error("DomainSocket failed to start:{}", futureParams.cause().getMessage());
            }
        });
    }
    
    /**
     * Start ShardingSphere-Proxy.
     *
     * @param port port
     * @param addresses addresses
     * @return ChannelFuture list
     * @throws InterruptedException interrupted exception
     */
    public List<ChannelFuture> startInternal(final int port, final List<String> addresses) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        initServerBootstrap(bootstrap);
        List<ChannelFuture> result = new ArrayList<>(addresses.size());
        for (String each : addresses) {
            result.add(bootstrap.bind(each, port).sync());
        }
        return result;
    }
    
    private ChannelFuture startDomainSocket(final String socketPath) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        initServerBootstrap(bootstrap, new DomainSocketAddress(socketPath));
        return bootstrap.bind();
    }
    
    private void accept(final List<ChannelFuture> futures) throws InterruptedException {
        log.info("ShardingSphere-Proxy {} mode started successfully", ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getModeConfiguration().getType());
        ProxyInstanceMetaData instanceMetaData = (ProxyInstanceMetaData) ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance().getMetaData();
        log.info("Instance id: {}, IP: {}, port: {}", instanceMetaData.getId(), instanceMetaData.getIp(), instanceMetaData.getPort());
        for (ChannelFuture each : futures) {
            each.channel().closeFuture().sync();
        }
    }
    
    private void initServerBootstrap(final ServerBootstrap bootstrap) {
        Integer backLog = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG);
        bootstrap.group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024 * 1024, 16 * 1024 * 1024))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, backLog)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer(FrontDatabaseProtocolTypeFactory.getDatabaseType()));
    }
    
    private void initServerBootstrap(final ServerBootstrap bootstrap, final DomainSocketAddress localDomainSocketAddress) {
        bootstrap.group(bossGroup, workerGroup)
                .channel(EpollServerDomainSocketChannel.class)
                .localAddress(localDomainSocketAddress)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer(FrontDatabaseProtocolTypeFactory.getDatabaseType()));
    }
    
    /**
     * Close ShardingSphere-Proxy.
     */
    public synchronized void close() {
        if (isClosed) {
            return;
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        BackendExecutorContext.getInstance().getExecutorEngine().close();
        ProxyContext.getInstance().getContextManager().close();
        isClosed = true;
    }
}
