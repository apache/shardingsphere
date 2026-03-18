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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

import java.util.concurrent.TimeUnit;

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
        addIdleStateHandlerIfNeeded(pipeline);
        pipeline.addLast(FrontendChannelInboundHandler.class.getSimpleName(), new FrontendChannelInboundHandler(databaseProtocolFrontendEngine, socketChannel));
        databaseProtocolFrontendEngine.initChannel(socketChannel);
    }
    
    private void addIdleStateHandlerIfNeeded(final ChannelPipeline pipeline) {
        long idleTimeout = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_CONNECTION_IDLE_TIMEOUT);
        if (0 < idleTimeout) {
            pipeline.addLast(new IdleStateHandler(0, 0, idleTimeout, TimeUnit.SECONDS));
        }
    }
}
