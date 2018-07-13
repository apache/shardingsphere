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

package io.shardingsphere.proxy.frontend.mysql;

import io.netty.channel.ChannelId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manage the thread for each channel invoking.
 * 
 * <p>This ensure atomikos can process XA transaction by current thread id.</p>
 * 
 * @author zhaojun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelThreadHolder {
    
    private static final ChannelThreadHolder INSTANCE = new ChannelThreadHolder();
    
    private volatile Map<ChannelId, ExecutorService> threadPoolMap = new HashMap<>();
    
    /**
     * Get channel thread holder instance.
     * 
     * @return channel thread holder instance
     */
    public static ChannelThreadHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * Add active single thread pool of current channel.
     *
     * @param channelId channel id
     */
    public void add(final ChannelId channelId) {
        threadPoolMap.put(channelId, Executors.newSingleThreadExecutor());
    }
    
    /**
     * Get active single thread pool of current channel.
     *
     * @param channelId channel id
     * @return thread pool
     */
    public ExecutorService get(final ChannelId channelId) {
        return threadPoolMap.get(channelId);
    }
    
    /**
     * Remove thread when channel was closed.
     *
     * @param channelId channel id
     */
    public void remove(final ChannelId channelId) {
        threadPoolMap.remove(channelId).shutdown();
    }
}
