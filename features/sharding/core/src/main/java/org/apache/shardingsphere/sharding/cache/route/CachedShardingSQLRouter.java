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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.cache.ShardingCache;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableCheckResult;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheKey;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cached sharding SQL router.
 */
public final class CachedShardingSQLRouter {
    
    /**
     * Find {@link RouteContext} from cache or calculate and try caching.
     *
     * @param originSQLRouter origin SQL router
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @param database database
     * @param shardingCache sharding cache
     * @param props configuration properties
     * @param connectionContext connection context
     * @return route context
     */
    public Optional<RouteContext> loadRouteContext(final OriginSQLRouter originSQLRouter, final QueryContext queryContext, final RuleMetaData globalRuleMetaData,
                                                   final ShardingSphereDatabase database, final ShardingCache shardingCache, final ConfigurationProperties props,
                                                   final ConnectionContext connectionContext) {
        if (queryContext.getSql().length() > shardingCache.getConfiguration().getAllowedMaxSqlLength()) {
            return Optional.empty();
        }
        ShardingRouteCacheableCheckResult cacheableCheckResult = shardingCache.getRouteCacheableChecker().check(database, queryContext);
        if (!cacheableCheckResult.isProbablyCacheable()) {
            return Optional.empty();
        }
        List<Object> shardingConditionParams = new ArrayList<>(cacheableCheckResult.getShardingConditionParameterMarkerIndexes().size());
        for (int each : cacheableCheckResult.getShardingConditionParameterMarkerIndexes()) {
            if (each >= queryContext.getParameters().size()) {
                return Optional.empty();
            }
            shardingConditionParams.add(queryContext.getParameters().get(each));
        }
        Optional<RouteContext> cachedResult = shardingCache.getRouteCache().get(new ShardingRouteCacheKey(queryContext.getSql(), shardingConditionParams))
                .flatMap(ShardingRouteCacheValue::getCachedRouteContext);
        RouteContext result = cachedResult.orElseGet(
                () -> originSQLRouter.createRouteContext(queryContext, globalRuleMetaData, database, shardingCache.getShardingRule(), props, connectionContext));
        if (!cachedResult.isPresent() && hitOneShardOnly(result)) {
            shardingCache.getRouteCache().put(new ShardingRouteCacheKey(queryContext.getSql(), shardingConditionParams), new ShardingRouteCacheValue(result));
        }
        return Optional.of(result);
    }
    
    private boolean hitOneShardOnly(final RouteContext routeContext) {
        return 1 == routeContext.getRouteUnits().size() && 1 == routeContext.getRouteUnits().iterator().next().getTableMappers().size()
                && 1 == routeContext.getOriginalDataNodes().size() && 1 == routeContext.getOriginalDataNodes().iterator().next().size();
    }
    
    @FunctionalInterface
    public interface OriginSQLRouter {
        
        /**
         * Create route context.
         *
         * @param queryContext query context
         * @param globalRuleMetaData global rule meta data
         * @param database database
         * @param rule rule
         * @param props configuration properties
         * @param connectionContext connection context
         * @return route context
         */
        RouteContext createRouteContext(QueryContext queryContext, RuleMetaData globalRuleMetaData, ShardingSphereDatabase database, ShardingRule rule,
                                        ConfigurationProperties props, ConnectionContext connectionContext);
    }
}
