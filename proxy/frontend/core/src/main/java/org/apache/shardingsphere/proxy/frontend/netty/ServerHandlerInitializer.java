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

package org.apache.shardingsphere.proxy.frontend.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.codec.PacketCodec;
import org.apache.shardingsphere.db.protocol.netty.ChannelAttrInitializer;
import org.apache.shardingsphere.db.protocol.netty.ProxyFlowControlHandler;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Server handler initializer.
 */
@RequiredArgsConstructor
public final class ServerHandlerInitializer extends ChannelInitializer<Channel> {
    
    private final DatabaseType databaseType;
    
    @Override
    protected void initChannel(final Channel socketChannel) {
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ChannelAttrInitializer());
        pipeline.addLast(new PacketCodec(databaseProtocolFrontendEngine.getCodecEngine()));
        pipeline.addLast(new FrontendChannelLimitationInboundHandler(databaseProtocolFrontendEngine));
        pipeline.addLast(ProxyFlowControlHandler.class.getSimpleName(), new ProxyFlowControlHandler());
        pipeline.addLast(FrontendChannelInboundHandler.class.getSimpleName(), new FrontendChannelInboundHandler(databaseProtocolFrontendEngine, socketChannel));
        databaseProtocolFrontendEngine.initChannel(socketChannel);
    }
}
