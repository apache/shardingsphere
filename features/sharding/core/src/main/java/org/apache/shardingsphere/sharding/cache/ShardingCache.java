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

package org.apache.shardingsphere.sharding.cache;

import lombok.Getter;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableChecker;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCache;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.apache.shardingsphere.timeservice.core.rule.builder.DefaultTimestampServiceConfigurationBuilder;

/**
 * <strong>EXPERIMENTAL</strong> Sharding cache.
 */
@Getter
public final class ShardingCache {
    
    private final ShardingCacheConfiguration configuration;
    
    private final ShardingRule shardingRule;
    
    private final TimestampServiceRule timestampServiceRule;
    
    private final ShardingRouteCacheableChecker routeCacheableChecker;
    
    private final ShardingRouteCache routeCache;
    
    public ShardingCache(final ShardingCacheConfiguration config, final ShardingRule shardingRule) {
        configuration = config;
        this.shardingRule = shardingRule;
        timestampServiceRule = new TimestampServiceRule(new DefaultTimestampServiceConfigurationBuilder().build());
        routeCacheableChecker = new ShardingRouteCacheableChecker(this);
        routeCache = new ShardingRouteCache(config.getRouteCache());
    }
}
