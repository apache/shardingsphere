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

package io.shardingsphere.proxy.backend;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Maps;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.proxy.backend.common.NettyChannelPoolHandler;
import io.shardingsphere.proxy.config.DataSourceConfig;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Sharding-Proxy Client.
 *
 * @author wangkai
 * @author linjiaqi
 */
@Slf4j
public final class ShardingProxyClient {
    private static final ShardingProxyClient INSTANCE = new ShardingProxyClient();
    
    private static final int WORKER_MAX_THREADS = Runtime.getRuntime().availableProcessors();
    
    private static final int MAX_CONNECTIONS = 10;
    
    private static final int CONNECT_TIMEOUT = 30;
    
    private EventLoopGroup workerGroup;
    
    @Getter
    private ChannelPoolMap<String, SimpleChannelPool> poolMap;
    
    private Map<String, DataSourceConfig> dataSourceConfigMap = Maps.newHashMap();
    
    /**
     * Start Sharding-Proxy.
     *
     * @throws MalformedURLException url is illegal.
     * @throws InterruptedException  interrupted exception
     */
    public void start() throws MalformedURLException, InterruptedException {
        Map<String, DataSourceParameter> dataSourceConfigurationMap = RuleRegistry.getInstance().getDataSourceConfigurationMap();
        for (Map.Entry<String, DataSourceParameter> each : dataSourceConfigurationMap.entrySet()) {
            URL url = new URL(each.getValue().getUrl().replaceAll("jdbc:mysql:", "http:"));
            final String ip = url.getHost();
            final int port = url.getPort();
            final String database = url.getPath().substring(1);
            final String username = (each.getValue()).getUsername();
            final String password = (each.getValue()).getPassword();
            dataSourceConfigMap.put(each.getKey(), new DataSourceConfig(ip, port, database, username, password));
        }
        final Bootstrap bootstrap = new Bootstrap();
        if (workerGroup instanceof EpollEventLoopGroup) {
            groupsEpoll(bootstrap);
        } else {
            groupsNio(bootstrap);
        }
        initPoolMap(bootstrap);
    }
    
    /**
     * Stop Sharding-Proxy.
     */
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
    
    private void groupsEpoll(final Bootstrap bootstrap) {
        workerGroup = new EpollEventLoopGroup(WORKER_MAX_THREADS);
        bootstrap.group(workerGroup)
                .channel(EpollSocketChannel.class)
                .option(EpollChannelOption.TCP_CORK, true)
                .option(EpollChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.SO_BACKLOG, 128)
                .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }
    
    private void groupsNio(final Bootstrap bootstrap) {
        workerGroup = new NioEventLoopGroup(WORKER_MAX_THREADS);
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }
    
    private void initPoolMap(final Bootstrap bootstrap) throws InterruptedException {
        poolMap = new AbstractChannelPoolMap<String, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(final String datasourceName) {
                DataSourceConfig dataSourceConfig = dataSourceConfigMap.get(datasourceName);
                //TODO maxConnection should be set.
                return new FixedChannelPool(bootstrap.remoteAddress(dataSourceConfig.getIp(), dataSourceConfig.getPort()), new NettyChannelPoolHandler(dataSourceConfig), MAX_CONNECTIONS);
            }
        };
        
        for (String each : dataSourceConfigMap.keySet()) {
            SimpleChannelPool pool = poolMap.get(each);
            Channel[] channels = new Channel[MAX_CONNECTIONS];
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                try {
                    channels[i] = pool.acquire().get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
                } catch (ExecutionException | TimeoutException e) {
                    log.error(e.getMessage(), e);
                }
            }
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                pool.release(channels[i]);
            }
        }
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
