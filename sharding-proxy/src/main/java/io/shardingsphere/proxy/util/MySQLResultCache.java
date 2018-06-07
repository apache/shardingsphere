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

package io.shardingsphere.proxy.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.shardingsphere.core.merger.QueryResult;

/**
 * cache for SynchronizedFuture.
 *
 * @author wangkai
 */
public class MySQLResultCache {
    private static final MySQLResultCache INSTANCE = new MySQLResultCache();
    
    //TODO expire time should be set.
    private Cache<Integer, SynchronizedFuture<List<QueryResult>>> resultCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    
    private Cache<String, Integer> connectionCache = CacheBuilder.newBuilder().build();
    
    /**
     * put synchronizedFuture by connectionId.
     *
     * @param connectionId       mysql connection id.
     * @param synchronizedFuture multiple result set.
     */
    public void putFuture(final int connectionId, final SynchronizedFuture<List<QueryResult>> synchronizedFuture) {
        resultCache.put(connectionId, synchronizedFuture);
    }
    
    /**
     * get SynchronizedFuture by connectionId.
     *
     * @param connectionId mysql connection id.
     * @return multiple result set.
     */
    public SynchronizedFuture<List<QueryResult>> getFuture(final int connectionId) {
        return resultCache.getIfPresent(connectionId);
    }
    
    /**
     * delete SynchronizedFuture by connectionId.
     *
     * @param connectionId mysql connection id.
     */
    public void deleteFuture(final int connectionId) {
        resultCache.invalidate(connectionId);
    }
    
    /**
     * put connectionId by channelId.
     *
     * @param channelId    netty channel id.
     * @param connectionId mysql connection id.
     */
    public void putConnection(final String channelId, final int connectionId) {
        connectionCache.put(channelId, connectionId);
    }
    
    /**
     * get connectionId by channelId.
     *
     * @param channelId netty channel id.
     * @return connectionId   mysql connection id.
     */
    public int getConnection(final String channelId) {
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
