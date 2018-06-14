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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manage the thread for each channel invoking.
 * this ensure atomikos can process xa transaction by current thread Id
 *
 * @author zhaojun
 */
public class ChannelThreadHolder {
    
    private static Map<ChannelId, ExecutorService> threadPoolMap = new ConcurrentHashMap<>();
    
    /**
     * Get active single thread pool of current channel.
     *
     * @param channelId id of channel
     * @return Thread
     */
    public static ExecutorService get(final ChannelId channelId) {
        ExecutorService result = threadPoolMap.get(channelId);
        if (null == result) {
            threadPoolMap.put(channelId, Executors.newSingleThreadExecutor());
            result = threadPoolMap.get(channelId);
        }
        return result;
    }
    
    /**
     * Remove the thread when channel was closed.
     *
     * @param channelId id of channel
     */
    public static void remove(final ChannelId channelId) {
        threadPoolMap.get(channelId).shutdown();
        threadPoolMap.remove(channelId);
    }
}
