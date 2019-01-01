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

package io.shardingsphere.shardingproxy.frontend.common.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandlerFactory;
import io.shardingsphere.shardingproxy.runtime.RuntimeContext;
import io.shardingsphere.shardingproxy.transport.common.codec.PacketCodecFactory;
import io.shardingsphere.shardingproxy.util.ChannelUtils;
import lombok.RequiredArgsConstructor;

/**
 * Channel initializer.
 *
 * @author xiaoyu
 */
@RequiredArgsConstructor
public final class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    
    private static final RuntimeContext RUNTIME_CONTEXT = RuntimeContext.getInstance();
    
    @Override
    protected void initChannel(final SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // TODO load database type from yaml or startup arguments
        pipeline.addLast(PacketCodecFactory.newInstance(DatabaseType.MySQL));
        FrontendHandler frontendHandler = FrontendHandlerFactory.createFrontendHandlerInstance(DatabaseType.MySQL);
        pipeline.addLast(frontendHandler);
        RUNTIME_CONTEXT.getFrontendChannelHandler().put(ChannelUtils.getLongTextId(socketChannel), frontendHandler);
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        RUNTIME_CONTEXT.getFrontendChannelHandler().remove(ChannelUtils.getLongTextId(ctx.channel()));
        ctx.fireChannelInactive();
    }
}
