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

package org.apache.shardingsphere.sharding.cache.checker;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Range;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.cache.ShardingCache;
import org.apache.shardingsphere.sharding.cache.checker.algorithm.CacheableShardingAlgorithmChecker;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Sharding route cacheable checker.
 */
public final class ShardingRouteCacheableChecker {
    
    private final ShardingRule shardingRule;
    
    private final TimestampServiceRule timestampServiceRule;
    
    private final LoadingCache<Key, ShardingRouteCacheableCheckResult> checkingCache;
    
    public ShardingRouteCacheableChecker(final ShardingCache shardingCache) {
        shardingRule = shardingCache.getShardingRule();
        timestampServiceRule = shardingCache.getTimestampServiceRule();
        checkingCache = buildCache(shardingCache.getConfiguration().getRouteCache());
    }
    
    private LoadingCache<Key, ShardingRouteCacheableCheckResult> buildCache(final ShardingCacheOptionsConfiguration cacheOptions) {
        Caffeine<Object, Object> result = Caffeine.newBuilder().initialCapacity(cacheOptions.getInitialCapacity()).maximumSize(cacheOptions.getMaximumSize());
        if (cacheOptions.isSoftValues()) {
            result.softValues();
        }
        return result.build(this::load);
    }
    
    private ShardingRouteCacheableCheckResult load(final Key key) {
        SQLStatementContext sqlStatementContext = key.getSqlStatementContext();
        ShardingRouteCacheableCheckResult result;
        if (sqlStatementContext instanceof SelectStatementContext) {
            result = checkSelectCacheable((SelectStatementContext) sqlStatementContext, key.getParameters(), key.getDatabase());
        } else if (sqlStatementContext instanceof UpdateStatementContext) {
            result = checkUpdateCacheable((UpdateStatementContext) sqlStatementContext, key.getParameters(), key.getDatabase());
        } else if (sqlStatementContext instanceof InsertStatementContext) {
            result = checkInsertCacheable((InsertStatementContext) sqlStatementContext, key.getParameters(), key.getDatabase());
        } else if (sqlStatementContext instanceof DeleteStatementContext) {
            result = checkDeleteCacheable((DeleteStatementContext) sqlStatementContext, key.getParameters(), key.getDatabase());
        } else {
            result = new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        key.getParameters().clear();
        return result;
    }
    
    private ShardingRouteCacheableCheckResult checkSelectCacheable(final SelectStatementContext statementContext, final List<Object> params, final ShardingSphereDatabase database) {
        Collection<String> tableNames = new HashSet<>(statementContext.getTablesContext().getTableNames());
        if (!shardingRule.isAllShardingTables(tableNames)) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        if (1 != tableNames.size() && !shardingRule.isAllConfigBindingTables(tableNames) || containsNonCacheableShardingAlgorithm(tableNames)) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        List<ShardingCondition> shardingConditions = new WhereClauseShardingConditionEngine(database, shardingRule, timestampServiceRule).createShardingConditions(statementContext, params);
        return checkShardingConditionsCacheable(shardingConditions);
    }
    
    private ShardingRouteCacheableCheckResult checkUpdateCacheable(final UpdateStatementContext statementContext, final List<Object> params, final ShardingSphereDatabase database) {
        return checkUpdateOrDeleteCacheable(statementContext, params, database);
    }
    
    private ShardingRouteCacheableCheckResult checkInsertCacheable(final InsertStatementContext statementContext, final List<Object> params, final ShardingSphereDatabase database) {
        Collection<String> tableNames = statementContext.getTablesContext().getTableNames();
        if (1 != tableNames.size() || null != statementContext.getInsertSelectContext() || null != statementContext.getOnDuplicateKeyUpdateValueContext()
                || statementContext.getGeneratedKeyContext().map(GeneratedKeyContext::isGenerated).orElse(false)) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        boolean isShardingTable = shardingRule.isAllShardingTables(tableNames);
        if (!isShardingTable || containsNonCacheableShardingAlgorithm(tableNames)) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        Collection<InsertValuesSegment> values = statementContext.getSqlStatement().getValues();
        if (1 != values.size()) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        InsertValuesSegment valueSegment = values.iterator().next();
        for (ExpressionSegment each : valueSegment.getValues()) {
            if (!(each instanceof ParameterMarkerExpressionSegment || each instanceof LiteralExpressionSegment)) {
                return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
            }
        }
        List<ShardingCondition> shardingConditions = new InsertClauseShardingConditionEngine(database, shardingRule, timestampServiceRule).createShardingConditions(statementContext, params);
        return checkShardingConditionsCacheable(shardingConditions);
    }
    
    private ShardingRouteCacheableCheckResult checkDeleteCacheable(final DeleteStatementContext statementContext, final List<Object> params, final ShardingSphereDatabase database) {
        return checkUpdateOrDeleteCacheable(statementContext, params, database);
    }
    
    private ShardingRouteCacheableCheckResult checkUpdateOrDeleteCacheable(final SQLStatementContext sqlStatementContext, final List<Object> params, final ShardingSphereDatabase database) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (1 != tableNames.size()) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        boolean isShardingTable = shardingRule.isAllShardingTables(tableNames);
        if (!isShardingTable || containsNonCacheableShardingAlgorithm(tableNames)) {
            return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
        }
        List<ShardingCondition> shardingConditions = new WhereClauseShardingConditionEngine(database, shardingRule, timestampServiceRule).createShardingConditions(sqlStatementContext, params);
        return checkShardingConditionsCacheable(shardingConditions);
    }
    
    private boolean containsNonCacheableShardingAlgorithm(final Collection<String> logicTables) {
        for (String each : logicTables) {
            ShardingTable shardingTable = shardingRule.getShardingTable(each);
            String databaseShardingAlgorithmName = shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable).getShardingAlgorithmName();
            ShardingAlgorithm databaseShardingAlgorithm = shardingRule.getShardingAlgorithms().get(databaseShardingAlgorithmName);
            if (null != databaseShardingAlgorithm && !CacheableShardingAlgorithmChecker.isCacheableShardingAlgorithm(databaseShardingAlgorithm)) {
                return true;
            }
            String tableShardingAlgorithmName = shardingRule.getTableShardingStrategyConfiguration(shardingTable).getShardingAlgorithmName();
            ShardingAlgorithm tableShardingAlgorithm = shardingRule.getShardingAlgorithms().get(tableShardingAlgorithmName);
            if (null != tableShardingAlgorithm && !CacheableShardingAlgorithmChecker.isCacheableShardingAlgorithm(tableShardingAlgorithm)) {
                return true;
            }
        }
        return false;
    }
    
    private static ShardingRouteCacheableCheckResult checkShardingConditionsCacheable(final List<ShardingCondition> shardingConditions) {
        Set<Integer> result = new TreeSet<>();
        for (ShardingCondition each : shardingConditions) {
            for (ShardingConditionValue conditionValue : each.getValues()) {
                if (!isConditionTypeCacheable(conditionValue)) {
                    return new ShardingRouteCacheableCheckResult(false, Collections.emptyList());
                }
                result.addAll(conditionValue.getParameterMarkerIndexes());
            }
        }
        return new ShardingRouteCacheableCheckResult(true, new ArrayList<>(result));
    }
    
    private static boolean isConditionTypeCacheable(final ShardingConditionValue conditionValue) {
        if (conditionValue instanceof ListShardingConditionValue<?>) {
            for (Object eachValue : ((ListShardingConditionValue<?>) conditionValue).getValues()) {
                if (!(eachValue instanceof Number)) {
                    return false;
                }
            }
        }
        if (conditionValue instanceof RangeShardingConditionValue<?>) {
            Range<?> range = ((RangeShardingConditionValue<?>) conditionValue).getValueRange();
            return range.lowerEndpoint() instanceof Number && range.upperEndpoint() instanceof Number;
        }
        return true;
    }
    
    /**
     * Check if query is cacheable.
     *
     * @param database database
     * @param queryContext query context
     * @return is cacheable
     */
    public ShardingRouteCacheableCheckResult check(final ShardingSphereDatabase database, final QueryContext queryContext) {
        return checkingCache.get(new Key(database, queryContext.getSql(), queryContext.getSqlStatementContext(), queryContext.getParameters()));
    }
    
    @EqualsAndHashCode(of = "sql")
    @Getter
    private static final class Key {
        
        private final ShardingSphereDatabase database;
        
        private final String sql;
        
        private final SQLStatementContext sqlStatementContext;
        
        private final List<Object> parameters;
        
        private Key(final ShardingSphereDatabase database, final String sql, final SQLStatementContext sqlStatementContext, final List<Object> params) {
            this.database = database;
            this.sql = sql;
            this.sqlStatementContext = sqlStatementContext;
            parameters = new ArrayList<>(params);
        }
    }
}
