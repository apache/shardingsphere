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

package io.shardingsphere.proxy.backend.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.proxy.backend.common.BackendHandlerFactory;
import io.shardingsphere.proxy.config.DataSourceConfig;
import io.shardingsphere.proxy.transport.common.codec.PacketCodecFactory;
import lombok.RequiredArgsConstructor;

/**
 * Channel initializer.
 *
 * @author wangkai
 * @author linjiaqi
 */
@RequiredArgsConstructor
public final class ClientHandlerInitializer extends ChannelInitializer<Channel> {
    private final DataSourceConfig dataSourceConfig;
    
    @Override
    protected void initChannel(final Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        // TODO load database type from yaml or startup arguments
        pipeline.addLast(PacketCodecFactory.createPacketCodecInstance(DatabaseType.MySQL));
        pipeline.addLast(BackendHandlerFactory.createBackendHandlerInstance(DatabaseType.MySQL, dataSourceConfig));
    }
}
