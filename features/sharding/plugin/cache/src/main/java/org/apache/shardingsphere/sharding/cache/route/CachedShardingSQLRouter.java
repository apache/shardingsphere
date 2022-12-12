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

package org.apache.shardingsphere.sharding.cache.route;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableCheckResult;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheKey;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheValue;
import org.apache.shardingsphere.sharding.cache.rule.ShardingCacheRule;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.ShardingSQLRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO Design a cache layer interface in kernel.
 * Cached sharding SQL router.
 */
public final class CachedShardingSQLRouter implements SQLRouter<ShardingCacheRule> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final ShardingSphereDatabase database, final ShardingCacheRule rule, final ConfigurationProperties props,
                                           final ConnectionContext connectionContext) {
        if (queryContext.getSql().length() > rule.getConfiguration().getAllowedMaxSqlLength()) {
            return new RouteContext();
        }
        ShardingRouteCacheableCheckResult cacheableCheckResult = rule.getRouteCacheableChecker().check(database, queryContext);
        if (!cacheableCheckResult.isProbablyCacheable()) {
            return new RouteContext();
        }
        List<Object> shardingConditionParams = new ArrayList<>(cacheableCheckResult.getShardingConditionParameterMarkerIndexes().size());
        for (int each : cacheableCheckResult.getShardingConditionParameterMarkerIndexes()) {
            if (each >= queryContext.getParameters().size()) {
                return new RouteContext();
            }
            shardingConditionParams.add(queryContext.getParameters().get(each));
        }
        Optional<RouteContext> cachedRouteContext = rule.getRouteCache().get(new ShardingRouteCacheKey(queryContext.getSql(), shardingConditionParams))
                .flatMap(ShardingRouteCacheValue::getCachedRouteContext);
        RouteContext result = cachedRouteContext.orElseGet(() -> new ShardingSQLRouter().createRouteContext(queryContext, database, rule.getShardingRule(), props, connectionContext));
        if (!cachedRouteContext.isPresent() && hitOneShardOnly(result)) {
            rule.getRouteCache().put(new ShardingRouteCacheKey(queryContext.getSql(), shardingConditionParams), new ShardingRouteCacheValue(result));
        }
        return result;
    }
    
    private boolean hitOneShardOnly(final RouteContext routeContext) {
        return 1 == routeContext.getRouteUnits().size() && 1 == routeContext.getRouteUnits().iterator().next().getTableMappers().size()
                && 1 == routeContext.getOriginalDataNodes().size() && 1 == routeContext.getOriginalDataNodes().iterator().next().size();
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database, final ShardingCacheRule rule,
                                     final ConfigurationProperties props, final ConnectionContext connectionContext) {
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER - 1;
    }
    
    @Override
    public Class<ShardingCacheRule> getTypeClass() {
        return ShardingCacheRule.class;
    }
}
