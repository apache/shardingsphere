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

package io.shardingjdbc.proxy.backend;

import com.google.common.collect.Maps;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
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
import io.shardingjdbc.proxy.backend.netty.ClientHandlerInitializer;
import io.shardingjdbc.proxy.config.ShardingRuleRegistry;
import lombok.Getter;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Sharding-Proxy Client.
 *
 * @author wangkai
 */
public final class ShardingProxyClient {
    private static final ShardingProxyClient INSTANCE = new ShardingProxyClient();
    
    private static final int WORKER_MAX_THREADS = Runtime.getRuntime().availableProcessors();
    
    private EventLoopGroup workerGroup;
    
    @Getter
    private Map<String, Channel> channelMap = Maps.newHashMap();
    private Map<String, Bootstrap> bootstrapMap = Maps.newHashMap();
    
    /**
     * Start Sharding-Proxy.
     *
     * @throws InterruptedException  interrupted exception
     * @throws MalformedURLException url is illegal.
     */
    public void start() throws InterruptedException, MalformedURLException {
        Map<String, DataSource> dataSourceMap = ShardingRuleRegistry.getInstance().getDataSourceMap();
        for (Map.Entry<String, DataSource> each : dataSourceMap.entrySet()) {
            URL url = new URL(((BasicDataSource) each.getValue()).getUrl().replaceAll("jdbc:mysql://", "http://"));
            String ip = url.getHost();
            int port = url.getPort();
            String database = url.getPath().substring(1);
            String username = ((BasicDataSource) each.getValue()).getUsername();
            String password = ((BasicDataSource) each.getValue()).getPassword();
            Bootstrap bootstrap = new Bootstrap();
            if (workerGroup instanceof EpollEventLoopGroup) {
                groupsEpoll(bootstrap, ip, port, database, username, password);
            } else {
                groupsNio(bootstrap, ip, port, database, username, password);
            }
            //TODO use connection pool.
            bootstrapMap.put(each.getKey(), bootstrap);
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            channelMap.put(each.getKey(), future.channel());
        }
    }
    
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
    
    private void groupsEpoll(final Bootstrap bootstrap, String ip, int port, String database, String username, String password) {
        workerGroup = new EpollEventLoopGroup(WORKER_MAX_THREADS);
        bootstrap.group(workerGroup)
                .channel(EpollServerSocketChannel.class)
                .option(EpollChannelOption.TCP_CORK, true)
                .option(EpollChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.SO_BACKLOG, 128)
                .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ClientHandlerInitializer(ip, port, database, username, password));
    }
    
    private void groupsNio(final Bootstrap bootstrap, String ip, int port, String database, String username, String password) {
        workerGroup = new NioEventLoopGroup(WORKER_MAX_THREADS);
        bootstrap.group(workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ClientHandlerInitializer(ip, port, database, username, password));
    }
    
    /**
     * Get instance of sharding-proxy client.
     *
     * @return instance of sharding-proxy client
     */
    public static ShardingProxyClient getInstance() {
        return INSTANCE;
    }
}
