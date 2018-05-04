/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandResponsePackets;

import java.util.concurrent.TimeUnit;

/**
 * cache for SynchronizedFuture.
 *
 * @author wangkai
 */
public class MySQLResultCache {
    private static final MySQLResultCache INSTANCE = new MySQLResultCache();
    //TODO expire time should be set.
    private Cache<Integer, SynchronizedFuture> resultCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    
    private Cache<String, Integer> connectionCache = CacheBuilder.newBuilder().build();
    
    private Cache<String, Integer> channelCache = CacheBuilder.newBuilder().build();
    
    /**
     * @param connectionId       mysql connection id.
     * @param synchronizedFuture multiple result set.
     */
    public void put(int connectionId, SynchronizedFuture<CommandResponsePackets> synchronizedFuture) {
        resultCache.put(connectionId, synchronizedFuture);
    }
    
    /**
     * @param connectionId mysql connection id.
     * @return multiple result set.
     */
    public SynchronizedFuture<CommandResponsePackets> get(int connectionId) {
        return resultCache.getIfPresent(connectionId);
    }
    
    /**
     * @param connectionId mysql connection id.
     */
    public void delete(int connectionId) {
        resultCache.invalidate(connectionId);
    }
    
    /**
     * @param channelId    netty channel id.
     * @param connectionId mysql connection id.
     */
    public void putConnectionMap(String channelId, int connectionId) {
        connectionCache.put(channelId, connectionId);
    }
    
    /**
     * @param channelId netty channel id.
     * @return connectionId   mysql connection id.
     */
    public int getonnectionMap(String channelId) {
        return connectionCache.getIfPresent(channelId);
    }
    
    /**
     * Get instance of MySQLResultCache.
     *
     * @return instance of MySQLResultCache
     */
    public static MySQLResultCache getInstance() {
        return INSTANCE;
    }
}
