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

package io.shardingsphere.proxy.backend.common;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.shardingsphere.proxy.backend.netty.ClientHandlerInitializer;
import io.shardingsphere.proxy.config.DataSourceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NettyChannelPoolHandler.
 *
 * @author wangkai
 * @author linjiaqi
 */
@RequiredArgsConstructor
@Slf4j
public class NettyChannelPoolHandler implements ChannelPoolHandler {
    private final DataSourceConfig dataSourceConfig;
    
    @Override
    public void channelReleased(final Channel channel) throws Exception {
        log.info("channelReleased. Channel ID: {}" + channel.id().asShortText());
    }
    
    @Override
    public void channelAcquired(final Channel channel) throws Exception {
        log.info("channelAcquired. Channel ID: {}" + channel.id().asShortText());
    }
    
    @Override
    public void channelCreated(final Channel channel) throws Exception {
        log.info("channelCreated. Channel ID: {}" + channel.id().asShortText());
        channel.pipeline().addLast(new ClientHandlerInitializer(dataSourceConfig));
    }
}
