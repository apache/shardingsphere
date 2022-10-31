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
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableCheckResult;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableChecker;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCache;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheKey;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheValue;
import org.apache.shardingsphere.sharding.cache.rule.ShardingCacheRule;
import org.apache.shardingsphere.sharding.route.engine.ShardingSQLRouter;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CachedShardingSQLRouterTest {
    
    @Mock
    private ShardingCacheRule shardingCacheRule;
    
    @Test
    public void assertCreateRouteContextWithSQLExceedMaxAllowedLength() {
        when(shardingCacheRule.getConfiguration()).thenReturn(new ShardingCacheRuleConfiguration(1, null));
        QueryContext queryContext = new QueryContext(null, "select 1", Collections.emptyList());
        RouteContext actual = new CachedShardingSQLRouter().createRouteContext(queryContext, null, shardingCacheRule, null, null);
        assertRouteContextIsEmpty(actual);
    }
    
    @Test
    public void assertCreateRouteContextWithNotCacheableQuery() {
        QueryContext queryContext = new QueryContext(null, "insert into t values (?), (?)", Collections.emptyList());
        when(shardingCacheRule.getConfiguration()).thenReturn(new ShardingCacheRuleConfiguration(100, null));
        when(shardingCacheRule.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCacheRule.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(false, Collections.emptyList()));
        RouteContext actual = new CachedShardingSQLRouter().createRouteContext(queryContext, null, shardingCacheRule, null, null);
        assertRouteContextIsEmpty(actual);
    }
    
    @Test
    public void assertCreateRouteContextWithUnmatchedActualParameterSize() {
        QueryContext queryContext = new QueryContext(null, "insert into t values (?, ?)", Collections.singletonList(0));
        when(shardingCacheRule.getConfiguration()).thenReturn(new ShardingCacheRuleConfiguration(100, null));
        when(shardingCacheRule.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCacheRule.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.singletonList(1)));
        RouteContext actual = new CachedShardingSQLRouter().createRouteContext(queryContext, null, shardingCacheRule, null, null);
        assertRouteContextIsEmpty(actual);
    }
    
    private static void assertRouteContextIsEmpty(final RouteContext actual) {
        assertTrue(actual.getRouteUnits().isEmpty());
        assertTrue(actual.getOriginalDataNodes().isEmpty());
        assertTrue(actual.getRouteStageContexts().isEmpty());
    }
    
    @Test
    public void assertCreateRouteContextWithCacheableQueryButCacheMissed() {
        QueryContext queryContext = new QueryContext(null, "insert into t values (?, ?)", Arrays.asList(0, 1));
        when(shardingCacheRule.getConfiguration()).thenReturn(new ShardingCacheRuleConfiguration(100, null));
        when(shardingCacheRule.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCacheRule.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.singletonList(1)));
        when(shardingCacheRule.getRouteCache()).thenReturn(mock(ShardingRouteCache.class));
        RouteContext expected = new RouteContext();
        expected.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t", "t"))));
        expected.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_0", "t")));
        when(shardingCacheRule.getRouteCache().get(any(ShardingRouteCacheKey.class))).thenReturn(Optional.empty());
        RouteContext actual;
        try (
                MockedConstruction<ShardingSQLRouter> ignored = mockConstruction(ShardingSQLRouter.class,
                        (mock, context) -> when(mock.createRouteContext(queryContext, null, shardingCacheRule.getShardingRule(), null, null)).thenReturn(expected))) {
            actual = new CachedShardingSQLRouter().createRouteContext(queryContext, null, shardingCacheRule, null, null);
        }
        assertThat(actual, is(expected));
        verify(shardingCacheRule.getRouteCache()).put(any(ShardingRouteCacheKey.class), any(ShardingRouteCacheValue.class));
    }
    
    @Test
    public void assertCreateRouteContextWithCacheHit() {
        QueryContext queryContext = new QueryContext(null, "insert into t values (?, ?)", Arrays.asList(0, 1));
        when(shardingCacheRule.getConfiguration()).thenReturn(new ShardingCacheRuleConfiguration(100, null));
        when(shardingCacheRule.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCacheRule.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.singletonList(1)));
        when(shardingCacheRule.getRouteCache()).thenReturn(mock(ShardingRouteCache.class));
        RouteContext expected = new RouteContext();
        expected.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t", "t"))));
        expected.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_0", "t")));
        when(shardingCacheRule.getRouteCache().get(any(ShardingRouteCacheKey.class))).thenReturn(Optional.of(new ShardingRouteCacheValue(expected)));
        RouteContext actual = new CachedShardingSQLRouter().createRouteContext(queryContext, null, shardingCacheRule, null, null);
        assertThat(actual, not(expected));
        assertThat(actual.getOriginalDataNodes(), is(expected.getOriginalDataNodes()));
        assertThat(actual.getRouteUnits(), is(expected.getRouteUnits()));
    }
    
    @Test
    public void assertCreateRouteContextWithQueryRoutedToMultiDataNodes() {
        QueryContext queryContext = new QueryContext(null, "select * from t", Collections.emptyList());
        when(shardingCacheRule.getConfiguration()).thenReturn(new ShardingCacheRuleConfiguration(100, null));
        when(shardingCacheRule.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCacheRule.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.emptyList()));
        when(shardingCacheRule.getRouteCache()).thenReturn(mock(ShardingRouteCache.class));
        RouteContext expected = new RouteContext();
        expected.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Arrays.asList(new RouteMapper("t", "t_0"), new RouteMapper("t", "t_1"))));
        expected.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_0", "t_0")));
        RouteContext actual;
        try (
                MockedConstruction<ShardingSQLRouter> ignored = mockConstruction(ShardingSQLRouter.class,
                        (mock, context) -> when(mock.createRouteContext(queryContext, null, shardingCacheRule.getShardingRule(), null, null)).thenReturn(expected))) {
            actual = new CachedShardingSQLRouter().createRouteContext(queryContext, null, shardingCacheRule, null, null);
        }
        assertThat(actual, is(expected));
        verify(shardingCacheRule.getRouteCache(), never()).put(any(ShardingRouteCacheKey.class), any(ShardingRouteCacheValue.class));
    }
    
    @Test
    public void assertDecorateRouteContext() {
        RouteContext routeContext = mock(RouteContext.class);
        new CachedShardingSQLRouter().decorateRouteContext(routeContext, null, null, null, null, null);
        verifyNoInteractions(routeContext);
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(new CachedShardingSQLRouter().getOrder(), is(-11));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(new CachedShardingSQLRouter().getTypeClass(), CoreMatchers.<Class<ShardingCacheRule>>is(ShardingCacheRule.class));
    }
}
