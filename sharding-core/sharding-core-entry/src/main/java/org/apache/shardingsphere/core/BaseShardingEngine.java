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

package org.apache.shardingsphere.core;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLLogger;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.ShardingStrategy;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Base sharding engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class BaseShardingEngine {

    private final ShardingRule shardingRule;

    private final ShardingProperties shardingProperties;

    private final ShardingMetaData metaData;

    private final DatabaseType databaseType;

    /**
     * Shard.
     *
     * @param sql        SQL
     * @param parameters parameters of SQL
     * @return SQL route result
     */
    public SQLRouteResult shard(final String sql, final List<Object> parameters) {
        List<Object> clonedParameters = cloneParameters(parameters);
        SQLRouteResult result = route(sql, clonedParameters);
        result.getRouteUnits().addAll(HintManager.isDatabaseShardingOnly() ? convert(sql, clonedParameters, result) : rewriteAndConvert(sql, clonedParameters, result));
        if (shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW)) {
            boolean showSimple = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SIMPLE);
            SQLLogger.logSQL(sql, showSimple, result.getSqlStatement(), result.getRouteUnits());
        }
        checkSupport(result);
        return result;
    }

    private void checkSupport(final SQLRouteResult routeResult) {
        SQLStatement statement = routeResult.getSqlStatement();
        if (!(statement instanceof UpdateStatement)) {
            return;
        }
        UpdateStatement updateStatement = (UpdateStatement) statement;
        Set<Column> updateColumns = updateStatement.getAssignments().keySet();
        checkDefaultShardingStrategy(updateColumns);
        checkTableRulesShardingStrategy(updateColumns);
    }

    private void checkDefaultShardingStrategy(final Set<Column> updateColumns) {
        for (Column column : updateColumns) {
            checkShardingStrategy(shardingRule.getDefaultDatabaseShardingStrategy(), column);
            checkShardingStrategy(shardingRule.getDefaultTableShardingStrategy(), column);
        }
    }

    private void checkTableRulesShardingStrategy(final Set<Column> updateColumns) {
        if (CollectionUtils.isEmpty(shardingRule.getTableRules())) {
            return;
        }
        for (Column column : updateColumns) {
            for (TableRule tableRule : shardingRule.getTableRules()) {
                if (!tableRule.getLogicTable().equals(column.getTableName())) {
                    continue;
                }
                checkShardingStrategy(tableRule.getDatabaseShardingStrategy(), column);
                checkShardingStrategy(tableRule.getTableShardingStrategy(), column);
            }
        }
    }

    private void checkShardingStrategy(final ShardingStrategy shardingstrategy, final Column column) {
        if (shardingstrategy == null) {
            return;
        }
        if (CollectionUtils.isEmpty(shardingstrategy.getShardingColumns())) {
            return;
        }
        if (shardingstrategy.getShardingColumns().contains(column.getName())) {
            throw new UnsupportedOperationException(String.format("Cannot support update shard key,logicTable: [%s],colum: [%s].", column.getTableName(), column.getName()));
        }
    }

    protected abstract List<Object> cloneParameters(List<Object> parameters);

    protected abstract SQLRouteResult route(String sql, List<Object> parameters);

    private Collection<RouteUnit> convert(final String sql, final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : sqlRouteResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), new SQLUnit(sql, parameters)));
        }
        return result;
    }

    private Collection<RouteUnit> rewriteAndConvert(final String sql, final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, sql, databaseType, sqlRouteResult, parameters, sqlRouteResult.getOptimizeResult());
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(sqlRouteResult.getRoutingResult().isSingleRouting());
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : sqlRouteResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, metaData.getDataSource())));
        }
        return result;
    }
}
