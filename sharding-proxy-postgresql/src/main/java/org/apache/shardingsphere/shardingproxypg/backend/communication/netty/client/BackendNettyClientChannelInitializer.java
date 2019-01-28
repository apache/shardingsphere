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

package org.apache.shardingsphere.shardingproxypg.backend.communication.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxypg.backend.communication.netty.client.response.ResponseHandlerFactory;
import org.apache.shardingsphere.shardingproxypg.transport.common.codec.PacketCodecFactory;

/**
 * Channel initializer for backend connection netty client.
 *
 * @author wangkai
 * @author linjiaqi
 */
@RequiredArgsConstructor
public final class BackendNettyClientChannelInitializer extends ChannelInitializer<Channel> {
    
    private final String dataSourceName;
    
    private final String schemaName;
    
    @Override
    protected void initChannel(final Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        // TODO load database type from yaml or startup arguments
        pipeline.addLast(PacketCodecFactory.newInstance(DatabaseType.MySQL));
        pipeline.addLast(ResponseHandlerFactory.newInstance(DatabaseType.MySQL, dataSourceName, schemaName));
    }
}
