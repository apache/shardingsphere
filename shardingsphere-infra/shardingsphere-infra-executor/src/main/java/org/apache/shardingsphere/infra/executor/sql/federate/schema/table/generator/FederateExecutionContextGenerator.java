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

package org.apache.shardingsphere.infra.executor.sql.federate.schema.table.generator;

import org.apache.calcite.DataContext;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederationTableMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Federate execution context generator.
 */
public final class FederateExecutionContextGenerator {
    
    private final ExecutionContext routeExecutionContext;
    
    private final String tableName;
    
    private final FederationSQLGenerator sqlGenerator;
    
    public FederateExecutionContextGenerator(final ExecutionContext routeExecutionContext, final FederationTableMetaData tableMetaData, 
                                             final DataContext root, final List<RexNode> filters, final int[] projects, final QuoteCharacter quoteCharacter) {
        this.routeExecutionContext = routeExecutionContext;
        tableName = tableMetaData.getName();
        sqlGenerator = new FederationSQLGenerator(root, filters, projects, tableMetaData.getColumnNames(), quoteCharacter);
    }
    
    /**
     * Generate execution context.
     *
     * @return generated execution context
     */
    public ExecutionContext generate() {
        RouteContext filteredRouteContext = new RouteContextFilter().filter(tableName, routeExecutionContext.getRouteContext());
        return new ExecutionContext(routeExecutionContext.getLogicSQL(), generate(filteredRouteContext.getRouteUnits()), filteredRouteContext);
    }
    
    private Collection<ExecutionUnit> generate(final Collection<RouteUnit> routeUnits) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (RouteUnit each: routeUnits) {
            result.addAll(generate(each));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> generate(final RouteUnit routeUnit) {
        return routeUnit.getTableMappers().stream().map(each -> generate(routeUnit, each)).collect(Collectors.toList());
    }
    
    private ExecutionUnit generate(final RouteUnit routeUnit, final RouteMapper tableMapper) {
        String sql = sqlGenerator.generate(tableMapper.getActualName());
        return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), new SQLUnit(sql, Collections.emptyList(), Collections.singletonList(tableMapper)));
    }
}
