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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
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
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Route SQL rewrite engine.
 */
public final class RouteSQLRewriteEngine {
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @param routeContext route context
     * @return SQL rewrite result
     */
    public RouteSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        Map<RouteUnit, SQLRewriteUnit> result = new LinkedHashMap<>(routeContext.getRouteUnits().size(), 1);
        for (Entry<String, Collection<RouteUnit>> entry : aggregateRouteUnitGroups(routeContext.getRouteUnits()).entrySet()) {
            Collection<RouteUnit> routeUnits = entry.getValue();
            if (isNeedAggregateRewrite(sqlRewriteContext.getSqlStatementContext(), routeUnits)) {
                result.put(routeUnits.iterator().next(), createSQLRewriteUnit(sqlRewriteContext, routeContext, routeUnits));
            } else {
                result.putAll(createSQLRewriteUnits(sqlRewriteContext, routeContext, routeUnits));
            }
        }
        return new RouteSQLRewriteResult(result);
    }
    
    private SQLRewriteUnit createSQLRewriteUnit(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final Collection<RouteUnit> routeUnits) {
        Collection<String> sql = new LinkedList<>();
        List<Object> parameters = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            sql.add(SQLUtil.trimSemicolon(new RouteSQLBuilder(sqlRewriteContext, each).toSQL()));
            parameters.addAll(getParameters(sqlRewriteContext.getParameterBuilder(), routeContext, each));
        }
        return new SQLRewriteUnit(String.join(" UNION ALL ", sql), parameters);
    }
    
    private Map<RouteUnit, SQLRewriteUnit> createSQLRewriteUnits(final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext, final Collection<RouteUnit> routeUnits) {
        Map<RouteUnit, SQLRewriteUnit> result = new LinkedHashMap<>(routeContext.getRouteUnits().size(), 1);
        for (RouteUnit each : routeUnits) {
            result.put(each, new SQLRewriteUnit(new RouteSQLBuilder(sqlRewriteContext, each).toSQL(), getParameters(sqlRewriteContext.getParameterBuilder(), routeContext, each)));
        }
        return result;
    }
    
    private boolean isNeedAggregateRewrite(final SQLStatementContext<?> sqlStatementContext, final Collection<RouteUnit> routeUnits) {
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
        Map<String, Collection<RouteUnit>> result = new LinkedHashMap<>(routeUnits.size(), 1);
        for (RouteUnit each : routeUnits) {
            String dataSourceName = each.getDataSourceMapper().getActualName();
            if (!result.containsKey(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
    
    private List<Object> getParameters(final ParameterBuilder parameterBuilder, final RouteContext routeContext, final RouteUnit routeUnit) {
        if (parameterBuilder instanceof StandardParameterBuilder) {
            return parameterBuilder.getParameters();
        }
        return routeContext.getOriginalDataNodes().isEmpty()
                ? ((GroupedParameterBuilder) parameterBuilder).getParameters() : buildRouteParameters((GroupedParameterBuilder) parameterBuilder, routeContext, routeUnit);
    }
    
    private List<Object> buildRouteParameters(final GroupedParameterBuilder parameterBuilder, final RouteContext routeContext, final RouteUnit routeUnit) {
        List<Object> result = new LinkedList<>();
        int count = 0;
        for (Collection<DataNode> each : routeContext.getOriginalDataNodes()) {
            if (isInSameDataNode(each, routeUnit)) {
                result.addAll(parameterBuilder.getParameters(count));
            }
            count++;
        }
        result.addAll(parameterBuilder.getGenericParameterBuilder().getParameters());
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
}
