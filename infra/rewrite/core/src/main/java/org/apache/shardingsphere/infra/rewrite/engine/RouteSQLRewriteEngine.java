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

package org.apache.shardingsphere.infra.rewrite.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.impl.RouteSQLBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.ParameterFilterable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Route SQL rewrite engine.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class RouteSQLRewriteEngine {
    
    private final SQLTranslatorRule translatorRule;
    
    private final ShardingSphereDatabase database;
    
    private final RuleMetaData globalRuleMetaData;
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @param routeContext route context
     * @param queryContext query context
     * @return SQL rewrite result
     */
    public RouteSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final QueryContext queryContext) {
        return new RouteSQLRewriteResult(translate(queryContext, createSQLRewriteUnits(sqlRewriteContext, routeContext)));
    }
    
    private Map<RouteUnit, SQLRewriteUnit> createSQLRewriteUnits(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        Map<RouteUnit, SQLRewriteUnit> result = new LinkedHashMap<>(routeContext.getRouteUnits().size(), 1F);
        for (Entry<String, Collection<RouteUnit>> entry : aggregateRouteUnitGroups(routeContext.getRouteUnits()).entrySet()) {
            Collection<RouteUnit> routeUnits = entry.getValue();
            if (isNeedAggregateRewrite(sqlRewriteContext.getSqlStatementContext(), routeUnits)) {
                result.put(routeUnits.iterator().next(), createSQLRewriteUnit(sqlRewriteContext, routeContext, routeUnits));
            } else {
                for (RouteUnit each : routeUnits) {
                    result.put(each, createSQLRewriteUnit(sqlRewriteContext, routeContext, each));
                }
            }
        }
        return result;
    }
    
    private Map<String, Collection<RouteUnit>> aggregateRouteUnitGroups(final Collection<RouteUnit> routeUnits) {
        Map<String, Collection<RouteUnit>> result = new LinkedHashMap<>(routeUnits.size(), 1F);
        for (RouteUnit each : routeUnits) {
            result.computeIfAbsent(each.getDataSourceMapper().getActualName(), unused -> new LinkedList<>()).add(each);
        }
        return result;
    }
    
    private boolean isNeedAggregateRewrite(final SQLStatementContext sqlStatementContext, final Collection<RouteUnit> routeUnits) {
        if (!(sqlStatementContext instanceof SelectStatementContext) || 1 == routeUnits.size()) {
            return false;
        }
        SelectStatementContext statementContext = (SelectStatementContext) sqlStatementContext;
        boolean containsSubqueryJoinQuery = statementContext.isContainsSubquery() || statementContext.isContainsJoinQuery();
        boolean containsOrderByLimitClause = !statementContext.getOrderByContext().getItems().isEmpty() || statementContext.getPaginationContext().isHasPagination();
        boolean containsLockClause = statementContext.getSqlStatement().getLock().isPresent();
        boolean result = !containsSubqueryJoinQuery && !containsOrderByLimitClause && !containsLockClause;
        statementContext.setNeedAggregateRewrite(result);
        return result;
    }
    
    private SQLRewriteUnit createSQLRewriteUnit(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final Collection<RouteUnit> routeUnits) {
        Collection<String> sql = new LinkedList<>();
        List<Object> params = new LinkedList<>();
        boolean containsDollarMarker = sqlRewriteContext.getSqlStatementContext() instanceof SelectStatementContext
                && ((SelectStatementContext) (sqlRewriteContext.getSqlStatementContext())).isContainsDollarParameterMarker();
        for (RouteUnit each : routeUnits) {
            String sqlStr = SQLUtils.trimSemicolon(new RouteSQLBuilder(sqlRewriteContext.getSql(), sqlRewriteContext.getSqlTokens(), each).toSQL());
            sql.add(sqlStr);
            if (containsDollarMarker && !params.isEmpty()) {
                continue;
            }
            List<Object> parameters = getParameters(sqlRewriteContext, routeContext, each);
            params.addAll(parameters);
        }
        return new SQLRewriteUnit(String.join(" UNION ALL ", sql), params);
    }
    
    private SQLRewriteUnit createSQLRewriteUnit(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final RouteUnit routeUnit) {
        String sql = getActualSQL(sqlRewriteContext, routeUnit);
        List<Object> parameters = getParameters(sqlRewriteContext, routeContext, routeUnit);
        return new SQLRewriteUnit(sql, parameters);
    }
    
    private String getActualSQL(final SQLRewriteContext sqlRewriteContext, final RouteUnit routeUnit) {
        return new RouteSQLBuilder(sqlRewriteContext.getSql(), sqlRewriteContext.getSqlTokens(), routeUnit).toSQL();
    }
    
    private List<Object> getParameters(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final RouteUnit routeUnit) {
        if (sqlRewriteContext.getParameters().isEmpty()) {
            return Collections.emptyList();
        }
        ParameterBuilder parameterBuilder = sqlRewriteContext.getParameterBuilder();
        if (parameterBuilder instanceof StandardParameterBuilder) {
            return filterParametersIfNeeded(sqlRewriteContext, parameterBuilder.getParameters(), routeUnit);
        }
        return routeContext.getOriginalDataNodes().isEmpty()
                ? ((GroupedParameterBuilder) parameterBuilder).getParameters()
                : buildRouteParameters((GroupedParameterBuilder) parameterBuilder, routeContext, routeUnit);
    }
    
    private List<Object> filterParametersIfNeeded(final SQLRewriteContext sqlRewriteContext,
                                                  final List<Object> originalParameters,
                                                  final RouteUnit routeUnit) {
        List<ParameterFilterable> filterableTokens = findParameterFilterableTokens(sqlRewriteContext.getSqlTokens());
        if (filterableTokens.isEmpty()) {
            return originalParameters;
        }
        return applyParameterFiltering(originalParameters, filterableTokens, routeUnit);
    }
    
    private List<ParameterFilterable> findParameterFilterableTokens(final List<SQLToken> sqlTokens) {
        return sqlTokens.stream()
                .filter(token -> token instanceof ParameterFilterable)
                .map(token -> (ParameterFilterable) token)
                .filter(ParameterFilterable::isParameterFilterable)
                .collect(Collectors.toList());
    }
    
    private List<Object> applyParameterFiltering(final List<Object> originalParameters,
                                                 final List<ParameterFilterable> filterableTokens,
                                                 final RouteUnit routeUnit) {
        Set<Integer> allRemovedIndices = new TreeSet<>(Collections.reverseOrder());
        for (ParameterFilterable filterable : filterableTokens) {
            Set<Integer> removedIndices = filterable.getRemovedParameterIndices(routeUnit);
            allRemovedIndices.addAll(removedIndices);
        }
        List<Object> result = new ArrayList<>(originalParameters);
        for (Integer index : allRemovedIndices) {
            if (index >= 0 && index < result.size()) {
                result.remove(index.intValue());
            }
        }
        return result;
    }
    
    private List<Object> buildRouteParameters(final GroupedParameterBuilder paramBuilder, final RouteContext routeContext, final RouteUnit routeUnit) {
        List<Object> result = new LinkedList<>(paramBuilder.getBeforeGenericParameterBuilder().getParameters());
        int count = 0;
        for (Collection<DataNode> each : routeContext.getOriginalDataNodes()) {
            if (isInSameDataNode(each, routeUnit)) {
                result.addAll(paramBuilder.getParameters(count));
            }
            count++;
        }
        result.addAll(paramBuilder.getAfterGenericParameterBuilder().getParameters());
        return result;
    }
    
    private boolean isInSameDataNode(final Collection<DataNode> dataNodes, final RouteUnit routeUnit) {
        if (dataNodes.isEmpty()) {
            return true;
        }
        for (DataNode each : dataNodes) {
            if (routeUnit.findTableMapper(each.getDataSourceName(), each.getTableName()).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private Map<RouteUnit, SQLRewriteUnit> translate(final QueryContext queryContext, final Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits) {
        Map<RouteUnit, SQLRewriteUnit> result = new LinkedHashMap<>(sqlRewriteUnits.size(), 1F);
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        for (Entry<RouteUnit, SQLRewriteUnit> entry : sqlRewriteUnits.entrySet()) {
            DatabaseType storageType = storageUnits.get(entry.getKey().getDataSourceMapper().getActualName()).getStorageType();
            String sql = entry.getValue().getSql();
            List<Object> parameters = entry.getValue().getParameters();
            Optional<SQLTranslatorContext> sqlTranslatorContext = translatorRule.translate(sql, parameters, queryContext, storageType, database, globalRuleMetaData);
            String translatedSQL = sqlTranslatorContext.isPresent() ? sqlTranslatorContext.get().getSql() : sql;
            List<Object> translatedParameters = sqlTranslatorContext.isPresent() ? sqlTranslatorContext.get().getParameters() : parameters;
            SQLRewriteUnit sqlRewriteUnit = new SQLRewriteUnit(translatedSQL, translatedParameters);
            result.put(entry.getKey(), sqlRewriteUnit);
        }
        return result;
    }
}
