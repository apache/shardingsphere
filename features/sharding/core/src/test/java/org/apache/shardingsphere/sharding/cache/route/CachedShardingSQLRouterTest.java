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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.cache.ShardingCache;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableCheckResult;
import org.apache.shardingsphere.sharding.cache.checker.ShardingRouteCacheableChecker;
import org.apache.shardingsphere.sharding.cache.route.CachedShardingSQLRouter.OriginSQLRouter;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCache;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheKey;
import org.apache.shardingsphere.sharding.cache.route.cache.ShardingRouteCacheValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CachedShardingSQLRouterTest {
    
    @Mock
    private ShardingCache shardingCache;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatementContext sqlStatementContext;
    
    @Test
    void assertCreateRouteContextWithSQLExceedMaxAllowedLength() {
        when(shardingCache.getConfiguration()).thenReturn(new ShardingCacheConfiguration(1, null));
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        Optional<RouteContext> actual = new CachedShardingSQLRouter().loadRouteContext(null, queryContext, mock(RuleMetaData.class), null, shardingCache, Collections.emptyList(), null);
        assertFalse(actual.isPresent());
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertCreateRouteContextWithNotCacheableQuery() {
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, "INSERT INTO t VALUES (?), (?)", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        when(shardingCache.getConfiguration()).thenReturn(new ShardingCacheConfiguration(100, null));
        when(shardingCache.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCache.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(false, Collections.emptyList()));
        Optional<RouteContext> actual = new CachedShardingSQLRouter().loadRouteContext(null, queryContext, mock(RuleMetaData.class), null, shardingCache, Collections.singletonList("t"), null);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateRouteContextWithUnmatchedActualParameterSize() {
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, "INSERT INTO t VALUES (?, ?)", Collections.singletonList(0), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        when(shardingCache.getConfiguration()).thenReturn(new ShardingCacheConfiguration(100, null));
        when(shardingCache.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCache.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.singletonList(1)));
        Optional<RouteContext> actual = new CachedShardingSQLRouter().loadRouteContext(null, queryContext, mock(RuleMetaData.class), null, shardingCache, Collections.singletonList("t"), null);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateRouteContextWithCacheableQueryButCacheMissed() {
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, "INSERT INTO t VALUES (?, ?)", Arrays.asList(0, 1), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        when(shardingCache.getConfiguration()).thenReturn(new ShardingCacheConfiguration(100, null));
        when(shardingCache.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCache.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.singletonList(1)));
        when(shardingCache.getRouteCache()).thenReturn(mock(ShardingRouteCache.class));
        RouteContext expected = new RouteContext();
        expected.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t", "t"))));
        expected.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_0", (String) null, "t")));
        when(shardingCache.getRouteCache().get(any(ShardingRouteCacheKey.class))).thenReturn(Optional.empty());
        OriginSQLRouter router = (unused, globalRuleMetaData, database, rule, tableNames, props) -> expected;
        Collection<String> tableNames = Collections.singletonList("t");
        Optional<RouteContext> actual = new CachedShardingSQLRouter().loadRouteContext(router, queryContext, mock(RuleMetaData.class), null, shardingCache, tableNames, null);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
        verify(shardingCache.getRouteCache()).put(any(ShardingRouteCacheKey.class), any(ShardingRouteCacheValue.class));
    }
    
    @Test
    void assertCreateRouteContextWithCacheHit() {
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, "INSERT INTO t VALUES (?, ?)", Arrays.asList(0, 1), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        when(shardingCache.getConfiguration()).thenReturn(new ShardingCacheConfiguration(100, null));
        when(shardingCache.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCache.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.singletonList(1)));
        when(shardingCache.getRouteCache()).thenReturn(mock(ShardingRouteCache.class));
        RouteContext expected = new RouteContext();
        expected.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("t", "t"))));
        expected.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_0", (String) null, "t")));
        when(shardingCache.getRouteCache().get(any(ShardingRouteCacheKey.class))).thenReturn(Optional.of(new ShardingRouteCacheValue(expected)));
        Optional<RouteContext> actual = new CachedShardingSQLRouter().loadRouteContext(null, queryContext, mock(RuleMetaData.class), null, shardingCache, Collections.singletonList("t"), null);
        assertTrue(actual.isPresent());
        RouteContext actualRouteContext = actual.get();
        assertThat(actualRouteContext, not(expected));
        assertThat(actualRouteContext.getOriginalDataNodes(), is(expected.getOriginalDataNodes()));
        assertThat(actualRouteContext.getRouteUnits(), is(expected.getRouteUnits()));
    }
    
    @Test
    void assertCreateRouteContextWithQueryRoutedToMultiDataNodes() {
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, "SELECT * FROM t", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        when(shardingCache.getConfiguration()).thenReturn(new ShardingCacheConfiguration(100, null));
        when(shardingCache.getRouteCacheableChecker()).thenReturn(mock(ShardingRouteCacheableChecker.class));
        when(shardingCache.getRouteCacheableChecker().check(null, queryContext)).thenReturn(new ShardingRouteCacheableCheckResult(true, Collections.emptyList()));
        when(shardingCache.getRouteCache()).thenReturn(mock(ShardingRouteCache.class));
        RouteContext expected = new RouteContext();
        expected.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Arrays.asList(new RouteMapper("t", "t_0"), new RouteMapper("t", "t_1"))));
        expected.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_0", (String) null, "t_0")));
        OriginSQLRouter router = (unused, globalRuleMetaData, database, rule, tableNames, props) -> expected;
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        Collection<String> tableNames = Collections.singletonList("t");
        Optional<RouteContext> actual = new CachedShardingSQLRouter().loadRouteContext(router, queryContext, globalRuleMetaData, null, shardingCache, tableNames, null);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
        verify(shardingCache.getRouteCache(), never()).put(any(ShardingRouteCacheKey.class), any(ShardingRouteCacheValue.class));
    }
}
