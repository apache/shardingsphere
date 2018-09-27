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

package io.shardingsphere.shardingproxy.runtime;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Channel registry.
 *
 * @author wangkai
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelRegistry {
    
    private static final ChannelRegistry INSTANCE = new ChannelRegistry();
    
    // TODO :wangkai do not use cache, should use map, and add unregister feature
    private final Cache<String, Integer> connectionIds = CacheBuilder.newBuilder().build();
    
    /**
     * Get instance of channel registry.
     *
     * @return instance of channel registry
     */
    public static ChannelRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put connection id by channel ID.
     *
     * @param channelId netty channel ID
     * @param connectionId database connection ID
     */
    public void putConnectionId(final String channelId, final int connectionId) {
        connectionIds.put(channelId, connectionId);
    }
    
    /**
     * Get connection id by channel ID.
     *
     * @param channelId netty channel ID
     * @return connectionId database connection ID
     */
    public int getConnectionId(final String channelId) {
        Integer result = connectionIds.getIfPresent(channelId);
        Preconditions.checkNotNull(result, String.format("Can not get connection id via channel id: %s", channelId));
        return result;
    }
}
