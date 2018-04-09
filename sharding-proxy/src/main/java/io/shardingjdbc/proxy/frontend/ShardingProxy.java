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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.proxy.transport.common.codec.PacketCodecFactory;
import io.shardingjdbc.proxy.frontend.common.FrontendHandlerFactory;

/**
 * Sharding-Proxy.
 *
 * @author zhangliang
 */
public final class ShardingProxy {
    
    /**
     * Start Sharding-Proxy.
     * 
     * @param port port
     * @throws InterruptedException interrupted exception
     */
    public void start(final int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        
                        @Override
                        public void initChannel(final SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // TODO load database type from yaml or startup arguments
                            pipeline.addLast(PacketCodecFactory.createPacketCodecInstance(DatabaseType.MySQL));
                            pipeline.addLast(FrontendHandlerFactory.createFrontendHandlerInstance(DatabaseType.MySQL));
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
