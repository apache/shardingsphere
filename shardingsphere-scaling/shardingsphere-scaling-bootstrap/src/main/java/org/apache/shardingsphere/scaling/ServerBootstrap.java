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

package org.apache.shardingsphere.scaling;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.utils.ScalingConfigUtil;
import org.apache.shardingsphere.scaling.web.HttpServerInitializer;

/**
 * Bootstrap of ShardingSphere-Scaling server.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ServerBootstrap {
    
    /**
     * Server Main entry.
     *
     * @param args running args
     */
    // CHECKSTYLE:OFF
    @SneakyThrows
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        log.info("ShardingSphere-Scaling Server Startup");
        ScalingConfigUtil.initScalingConfig();
        startScalingServer();
    }
    
    private static void startScalingServer() throws InterruptedException {
        log.info("Start scaling server");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.ServerBootstrap bootstrap = new io.netty.bootstrap.ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());
            int port = ScalingContext.getInstance().getServerConfig().getPort();
            Channel channel = bootstrap.bind(port).sync().channel();
            log.info("ShardingSphere-Scaling is server on http://127.0.0.1:{}/", port);
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
