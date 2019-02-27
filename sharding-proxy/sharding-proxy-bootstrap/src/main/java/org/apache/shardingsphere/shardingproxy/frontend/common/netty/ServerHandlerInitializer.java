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

package org.apache.shardingsphere.shardingproxy.frontend.common.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.frontend.common.DatabaseFrontendEngineFactory;
import org.apache.shardingsphere.shardingproxy.transport.common.codec.DatabasePacketCodecEngineFactory;
import org.apache.shardingsphere.shardingproxy.transport.common.codec.PacketCodec;

/**
 * Channel initializer.
 * 
 * @author xiaoyu
 */
@RequiredArgsConstructor
public final class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected void initChannel(final SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new PacketCodec(DatabasePacketCodecEngineFactory.newInstance(LogicSchemas.getInstance().getDatabaseType())));
        pipeline.addLast(new FrontendChannelInboundHandler(DatabaseFrontendEngineFactory.newInstance(LogicSchemas.getInstance().getDatabaseType())));
    }
}
