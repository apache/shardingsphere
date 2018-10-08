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

package io.shardingsphere.shardingproxy.backend.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Channel pool handler of backend netty client.
 *
 * @author wangkai
 * @author linjiaqi
 */
@RequiredArgsConstructor
@Slf4j
public final class BackendNettyClientChannelPoolHandler implements ChannelPoolHandler {
    
    private final String dataSourceName;
    
    private final String schemaName;
    
    @Override
    public void channelReleased(final Channel channel) {
        log.info("channelReleased. Channel ID: {}" + channel.id().asShortText());
    }
    
    @Override
    public void channelAcquired(final Channel channel) {
        log.info("channelAcquired. Channel ID: {}" + channel.id().asShortText());
    }
    
    @Override
    public void channelCreated(final Channel channel) {
        log.info("channelCreated. Channel ID: {}" + channel.id().asShortText());
        channel.pipeline().addLast(new BackendNettyClientChannelInitializer(dataSourceName, schemaName));
    }
}
