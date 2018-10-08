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

package io.shardingsphere.shardingproxy.backend.netty.future;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Future registry.
 *
 * @author wangkai
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FutureRegistry {
    
    private static final FutureRegistry INSTANCE = new FutureRegistry();
    
    //TODO expire time should be set.
    private final Cache<Integer, SynchronizedFuture> resultCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    
    /**
     * Get instance of future registry.
     *
     * @return instance of future registry
     */
    public static FutureRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Put synchronized future by connection ID.
     *
     * @param connectionId database connection ID
     * @param synchronizedFuture synchronized future
     */
    public void put(final int connectionId, final SynchronizedFuture synchronizedFuture) {
        resultCache.put(connectionId, synchronizedFuture);
    }
    
    /**
     * Get synchronized future by connection ID.
     *
     * @param connectionId database connection ID
     * @return synchronized future
     */
    public SynchronizedFuture get(final int connectionId) {
        return resultCache.getIfPresent(connectionId);
    }
    
    /**
     * Delete synchronized future by connection ID.
     *
     * @param connectionId database connection ID
     */
    public void delete(final int connectionId) {
        resultCache.invalidate(connectionId);
    }
}
