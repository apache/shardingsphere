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
import io.netty.channel.Channel;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandResponsePackets;

import java.util.concurrent.TimeUnit;

/**
 * cache for SynchronizedFuture.
 *
 * @author wangkai
 */
public class MySQLResultCache {
    private static final MySQLResultCache INSTANCE = new MySQLResultCache();
    //TODO expire time will be set.
    private Cache<String, SynchronizedFuture> cache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    
    /**
     * @param sequenceId send mysql server transaction id.
     * @param synchronizedFuture multiple result set.
     */
    public void put(int sequenceId, SynchronizedFuture<CommandResponsePackets> synchronizedFuture){
        cache.put(sequenceId + "", synchronizedFuture);
    }
    
    /**
     * @param sequenceId send mysql server transaction id.
     * @return multiple result set.
     */
    public SynchronizedFuture<CommandResponsePackets> get(int sequenceId){
        return cache.getIfPresent(sequenceId + "");
    }
    
    public void delete(int sequenceId){
        cache.invalidate(sequenceId + "");
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
