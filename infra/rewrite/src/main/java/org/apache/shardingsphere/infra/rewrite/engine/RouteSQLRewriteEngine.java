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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.impl.RouteSQLBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Route SQL rewrite engine.
 */
@RequiredArgsConstructor
public final class RouteSQLRewriteEngine {
    
    private final SQLTranslatorRule translatorRule;
    
    private final DatabaseType protocolType;
    
    private final Map<String, DatabaseType> storageTypes;
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @param routeContext route context
     * @return SQL rewrite result
     */
    public RouteSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new LinkedHashMap<>(routeContext.getRouteUnits().size(), 1F);
        for (Entry<String, Collection<RouteUnit>> entry : aggregateRouteUnitGroups(routeContext.getRouteUnits()).entrySet()) {
            Collection<RouteUnit> routeUnits = entry.getValue();
            if (isNeedAggregateRewrite(sqlRewriteContext.getSqlStatementContext(), routeUnits)) {
                sqlRewriteUnits.put(routeUnits.iterator().next(), createSQLRewriteUnit(sqlRewriteContext, routeContext, routeUnits));
            } else {
                addSQLRewriteUnits(sqlRewriteUnits, sqlRewriteContext, routeContext, routeUnits);
            }
        }
        return new RouteSQLRewriteResult(translate(sqlRewriteContext.getSqlStatementContext().getSqlStatement(), sqlRewriteUnits));
    }
    
    private SQLRewriteUnit createSQLRewriteUnit(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final Collection<RouteUnit> routeUnits) {
        Collection<String> sql = new LinkedList<>();
        List<Object> params = new LinkedList<>();
        boolean containsDollarMarker = sqlRewriteContext.getSqlStatementContext() instanceof SelectStatementContext
                && ((SelectStatementContext) (sqlRewriteContext.getSqlStatementContext())).isContainsDollarParameterMarker();
        for (RouteUnit each : routeUnits) {
            sql.add(SQLUtils.trimSemicolon(new RouteSQLBuilder(sqlRewriteContext, each).toSQL()));
            if (containsDollarMarker && !params.isEmpty()) {
                continue;
            }
            params.addAll(getParameters(sqlRewriteContext.getParameterBuilder(), routeContext, each));
        }
        return new SQLRewriteUnit(String.join(" UNION ALL ", sql), params);
    }
    
    private void addSQLRewriteUnits(final Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits, final SQLRewriteContext sqlRewriteContext,
                                    final RouteContext routeContext, final Collection<RouteUnit> routeUnits) {
        for (RouteUnit each : routeUnits) {
            sqlRewriteUnits.put(each, new SQLRewriteUnit(new RouteSQLBuilder(sqlRewriteContext, each).toSQL(), getParameters(sqlRewriteContext.getParameterBuilder(), routeContext, each)));
        }
    }
    
    private boolean isNeedAggregateRewrite(final SQLStatementContext sqlStatementContext, final Collection<RouteUnit> routeUnits) {
        if (!(sqlStatementContext instanceof SelectStatementContext) || routeUnits.size() == 1) {
            return false;
        }
        SelectStatementContext statementContext = (SelectStatementContext) sqlStatementContext;
        boolean containsSubqueryJoinQuery = statementContext.isContainsSubquery() || statementContext.isContainsJoinQuery();
        boolean containsOrderByLimitClause = !statementContext.getOrderByContext().getItems().isEmpty() || statementContext.getPaginationContext().isHasPagination();
        boolean containsLockClause = SelectStatementHandler.getLockSegment(statementContext.getSqlStatement()).isPresent();
        boolean needAggregateRewrite = !containsSubqueryJoinQuery && !containsOrderByLimitClause && !containsLockClause;
        statementContext.setNeedAggregateRewrite(needAggregateRewrite);
        return needAggregateRewrite;
    }
    
    private Map<String, Collection<RouteUnit>> aggregateRouteUnitGroups(final Collection<RouteUnit> routeUnits) {
        Map<String, Collection<RouteUnit>> result = new LinkedHashMap<>(routeUnits.size(), 1F);
        for (RouteUnit each : routeUnits) {
            String dataSourceName = each.getDataSourceMapper().getActualName();
            result.computeIfAbsent(dataSourceName, unused -> new LinkedList<>()).add(each);
        }
        return result;
    }
    
    private List<Object> getParameters(final ParameterBuilder paramBuilder, final RouteContext routeContext, final RouteUnit routeUnit) {
        if (paramBuilder instanceof StandardParameterBuilder) {
            return paramBuilder.getParameters();
        }
        return routeContext.getOriginalDataNodes().isEmpty()
                ? ((GroupedParameterBuilder) paramBuilder).getParameters()
                : buildRouteParameters((GroupedParameterBuilder) paramBuilder, routeContext, routeUnit);
    }
    
    private List<Object> buildRouteParameters(final GroupedParameterBuilder paramBuilder, final RouteContext routeContext, final RouteUnit routeUnit) {
        List<Object> result = new LinkedList<>();
        int count = 0;
        for (Collection<DataNode> each : routeContext.getOriginalDataNodes()) {
            if (isInSameDataNode(each, routeUnit)) {
                result.addAll(paramBuilder.getParameters(count));
            }
            count++;
        }
        result.addAll(paramBuilder.getGenericParameterBuilder().getParameters());
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
    
    private Map<RouteUnit, SQLRewriteUnit> translate(final SQLStatement sqlStatement, final Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits) {
        Map<RouteUnit, SQLRewriteUnit> result = new LinkedHashMap<>(sqlRewriteUnits.size(), 1F);
        for (Entry<RouteUnit, SQLRewriteUnit> entry : sqlRewriteUnits.entrySet()) {
            DatabaseType storageType = storageTypes.get(entry.getKey().getDataSourceMapper().getActualName());
            String sql = translatorRule.translate(entry.getValue().getSql(), sqlStatement, protocolType, storageType);
            SQLRewriteUnit sqlRewriteUnit = new SQLRewriteUnit(sql, entry.getValue().getParameters());
            result.put(entry.getKey(), sqlRewriteUnit);
        }
        return result;
    }
}
