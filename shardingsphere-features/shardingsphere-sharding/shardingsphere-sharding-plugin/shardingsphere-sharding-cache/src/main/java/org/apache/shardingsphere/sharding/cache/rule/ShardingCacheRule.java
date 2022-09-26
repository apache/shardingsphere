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

package org.apache.shardingsphere.sharding.cache.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.sharding.cache.config.ShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.cache.route.ShardingRouteCache;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

/**
 * Sharding cache rule.
 */
@Getter
public final class ShardingCacheRule implements DatabaseRule {
    
    private final ShardingCacheRuleConfiguration configuration;
    
    private final ShardingRule shardingRule;
    
    private final ShardingRouteCacheableChecker routeCacheableChecker;
    
    private final ShardingRouteCache routeCache;
    
    public ShardingCacheRule(final ShardingCacheRuleConfiguration configuration, final ShardingRule shardingRule) {
        this.configuration = configuration;
        this.shardingRule = shardingRule;
        routeCacheableChecker = new ShardingRouteCacheableChecker(this);
        routeCache = new ShardingRouteCache(configuration.getRouteCache());
    }
    
    @Override
    public String getType() {
        return ShardingCacheRule.class.getSimpleName();
    }
}
