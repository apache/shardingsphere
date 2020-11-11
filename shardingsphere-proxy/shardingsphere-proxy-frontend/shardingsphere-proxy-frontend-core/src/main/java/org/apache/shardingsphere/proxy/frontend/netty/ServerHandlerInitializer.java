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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.codec.PacketCodec;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.protocol.DatabaseProtocolFrontendEngineFactory;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Channel initializer.
 */
@RequiredArgsConstructor
public final class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    
    @Override
    protected void initChannel(final SocketChannel socketChannel) {
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = DatabaseProtocolFrontendEngineFactory.newInstance(getDatabaseType());
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new PacketCodec(databaseProtocolFrontendEngine.getCodecEngine()));
        pipeline.addLast(new FrontendChannelInboundHandler(databaseProtocolFrontendEngine));
    }
    
    private DatabaseType getDatabaseType() {
        // TODO Consider loading from configuration.
        return ProxyContext.getInstance().getMetaDataContexts().getMetaDataMap().isEmpty() ? new MySQLDatabaseType() : ProxyContext.getInstance().getMetaDataContexts().getDatabaseType();
    }
}
