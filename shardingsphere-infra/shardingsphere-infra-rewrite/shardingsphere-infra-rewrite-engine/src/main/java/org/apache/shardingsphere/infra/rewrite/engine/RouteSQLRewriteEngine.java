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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.impl.RouteSQLBuilder;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Route SQL rewrite engine.
 */
public final class RouteSQLRewriteEngine {
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @param routeResult route result
     * @return SQL rewrite result
     */
    public RouteSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext, final RouteResult routeResult) {
        Map<RouteUnit, SQLRewriteUnit> result = new LinkedHashMap<>(routeResult.getRouteUnits().size(), 1);
        for (RouteUnit each : routeResult.getRouteUnits()) {
            result.put(each, new SQLRewriteUnit(new RouteSQLBuilder(sqlRewriteContext, each).toSQL(), getParameters(sqlRewriteContext.getParameterBuilder(), routeResult, each)));
        }
        return new RouteSQLRewriteResult(result);
    }
    
    private List<Object> getParameters(final ParameterBuilder parameterBuilder, final RouteResult routeResult, final RouteUnit routeUnit) {
        if (parameterBuilder instanceof StandardParameterBuilder) {
            return parameterBuilder.getParameters();
        }
        
        // TODO decouple onDuplicateKeyUpdateParameters and infra
        if (routeResult.getOriginalDataNodes().isEmpty()) {
            List<Object> onDuplicateKeyUpdateParameters = ((GroupedParameterBuilder) parameterBuilder).getOnDuplicateKeyUpdateParametersBuilder().getParameters();
            if (onDuplicateKeyUpdateParameters.isEmpty()) {
                return parameterBuilder.getParameters();
            }
            
            List<Object> result = new LinkedList<>();
            result.addAll(parameterBuilder.getParameters());
            result.addAll(onDuplicateKeyUpdateParameters);
            return result;
        }
    
        List<Object> result = new LinkedList<>();
        int count = 0;
        for (Collection<DataNode> each : routeResult.getOriginalDataNodes()) {
            if (isInSameDataNode(each, routeUnit)) {
                result.addAll(((GroupedParameterBuilder) parameterBuilder).getParameters(count));
            }
            count++;
        }
        result.addAll(((GroupedParameterBuilder) parameterBuilder).getOnDuplicateKeyUpdateParametersBuilder().getParameters());
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
