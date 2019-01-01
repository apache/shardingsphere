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

package io.shardingsphere.shardingproxy.backend.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Backend connection client for netty.
 *
 * @author wangkai
 * @author linjiaqi
 * @author panjuan
 */
@RequiredArgsConstructor
@Slf4j
public final class BackendNettyClient {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private static final String ESTABLISH_CONNECTION_FAILED_MESSAGE = "Can't establish connection with database:%s";
    
    private final LogicSchema logicSchema;
    
    private final int maxConnections;
    
    private final int connectionTimeoutSeconds;
    
    private final EventLoopGroup workerGroup;
    
    @Getter
    private ChannelPoolMap<String, SimpleChannelPool> poolMap;
    
    private final boolean isEpoll;
    
    @Getter
    private final Map<EventLoop, ChannelPoolMap<String, SimpleChannelPool>> eventLoopChannelPoolMap;
    
    public BackendNettyClient(final LogicSchema logicSchema, final EventLoopGroup workerGroup) {
        this.logicSchema = logicSchema;
        maxConnections = GLOBAL_REGISTRY.getShardingProperties().getValue(ShardingPropertiesConstant.PROXY_BACKEND_MAX_CONNECTIONS);
        connectionTimeoutSeconds = GLOBAL_REGISTRY.getShardingProperties().getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT_SECONDS);
        this.workerGroup = workerGroup;
        this.isEpoll = workerGroup instanceof EpollEventLoopGroup;
        eventLoopChannelPoolMap = new HashMap<>();
    }
    
    /**
     * Start backend connection client for netty.
     *
     * @throws InterruptedException interrupted exception
     */
    protected void start() throws InterruptedException {
        for (Iterator<EventExecutor> it = workerGroup.iterator(); it.hasNext();) {
            EventLoop key = (EventLoop) it.next();
            initPoolMap(key);
        }
    }
    
    private Bootstrap groupsEpoll(final EventLoop eventLoop) {
        return new Bootstrap().group(eventLoop)
                .channel(EpollSocketChannel.class)
                .option(EpollChannelOption.TCP_CORK, true)
                .option(EpollChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutSeconds * 1000)
                .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }
    
    private Bootstrap groupsNio(final EventLoop eventLoop) {
        return new Bootstrap().group(eventLoop)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutSeconds * 1000)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }
    
    private void initPoolMap(final EventLoop eventLoop) throws InterruptedException {
        poolMap = new AbstractChannelPoolMap<String, SimpleChannelPool>() {
            
            @Override
            protected SimpleChannelPool newPool(final String dataSourceName) {
                DataSourceMetaData dataSourceMetaData = logicSchema.getMetaData().getDataSource().getActualDataSourceMetaData(dataSourceName);
                Bootstrap bootstrap = isEpoll ? groupsEpoll(eventLoop) : groupsNio(eventLoop);
                return new FixedChannelPool(
                        bootstrap.remoteAddress(dataSourceMetaData.getHostName(), dataSourceMetaData.getPort()),
                        new BackendNettyClientChannelPoolHandler(dataSourceName, logicSchema.getName()), maxConnections);
            }
        };
        for (String each : logicSchema.getDataSources().keySet()) {
            SimpleChannelPool pool = poolMap.get(each);
            Channel[] channels = new Channel[maxConnections];
            for (int i = 0; i < maxConnections; i++) {
                try {
                    channels[i] = pool.acquire().get(connectionTimeoutSeconds, TimeUnit.SECONDS);
                } catch (final ExecutionException | TimeoutException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            boolean establishedChannelNotExists = true;
            for (int i = 0; i < maxConnections; i++) {
                Channel channel = channels[i];
                if (channel == null) {
                    continue;
                }
                pool.release(channel);
                establishedChannelNotExists = false;
            }
            if (establishedChannelNotExists) {
                throw new ShardingException(ESTABLISH_CONNECTION_FAILED_MESSAGE, each);
            }
        }
        eventLoopChannelPoolMap.put(eventLoop, poolMap);
    }
}
