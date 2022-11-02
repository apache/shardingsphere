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

package org.apache.shardingsphere.sharding.cache.route.cache;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheOptions;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ShardingRouteCacheTest {
    
    @Test
    public void assertPutAndGet() {
        ShardingRouteCache cache = new ShardingRouteCache(new ShardingCacheOptions(true, 1, 1));
        ShardingRouteCacheKey key = new ShardingRouteCacheKey("select name from t where id = ?", Collections.singletonList(1));
        assertFalse(cache.get(key).isPresent());
        cache.put(key, new ShardingRouteCacheValue(new RouteContext()));
        assertTrue(cache.get(key).isPresent());
    }
}
