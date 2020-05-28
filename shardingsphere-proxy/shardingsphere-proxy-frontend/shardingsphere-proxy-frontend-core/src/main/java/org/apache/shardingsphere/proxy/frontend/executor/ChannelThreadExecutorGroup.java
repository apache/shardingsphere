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

package org.apache.shardingsphere.proxy.frontend.executor;

import io.netty.channel.ChannelId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Channel thread executor group.
 * 
 * <p>
 *     Manage the thread for each channel invoking.
 *     This ensure XA transaction framework processed by current thread id.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelThreadExecutorGroup {
    
    private static final ChannelThreadExecutorGroup INSTANCE = new ChannelThreadExecutorGroup();
    
    private final Map<ChannelId, ExecutorService> executorServices = new ConcurrentHashMap<>();
    
    /**
     * Get channel thread executor group.
     * 
     * @return channel thread executor group
     */
    public static ChannelThreadExecutorGroup getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register channel.
     *
     * @param channelId channel id
     */
    public void register(final ChannelId channelId) {
        executorServices.put(channelId, Executors.newSingleThreadExecutor());
    }
    
    /**
     * Get executor service of current channel.
     *
     * @param channelId channel id
     * @return executor service of current channel
     */
    public ExecutorService get(final ChannelId channelId) {
        return executorServices.get(channelId);
    }
    
    /**
     * Unregister channel.
     *
     * @param channelId channel id
     */
    public void unregister(final ChannelId channelId) {
        executorServices.remove(channelId).shutdown();
    }
}
