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

package org.apache.shardingsphere.infra.executor.sql.federate.original.sql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.federate.original.table.FilterableTableScanContext;
import org.apache.shardingsphere.infra.federation.metadata.FederationTableMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Filterable execution context generator.
 */
@RequiredArgsConstructor
public final class FilterableExecutionContextGenerator {
    
    private final ExecutionContext routeExecutionContext;
    
    private final QuoteCharacter quoteCharacter;
    
    /**
     * Generate execution context.
     * 
     * @param tableMetaData table meta data
     * @param scanContext filterable table scan context
     * @return generated execution context
     */
    public ExecutionContext generate(final FederationTableMetaData tableMetaData, final FilterableTableScanContext scanContext) {
        RouteContext filteredRouteContext = new RouteContextFilter().filter(tableMetaData.getName(), routeExecutionContext.getRouteContext());
        return new ExecutionContext(routeExecutionContext.getLogicSQL(), generate(filteredRouteContext.getRouteUnits(), tableMetaData, scanContext, quoteCharacter), filteredRouteContext);
    }
    
    private Collection<ExecutionUnit> generate(final Collection<RouteUnit> routeUnits,
                                               final FederationTableMetaData tableMetaData, final FilterableTableScanContext scanContext, final QuoteCharacter quoteCharacter) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        FilterableSQLGenerator sqlGenerator = new FilterableSQLGenerator(tableMetaData, scanContext, quoteCharacter);
        for (RouteUnit each: routeUnits) {
            result.addAll(generate(each, sqlGenerator));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> generate(final RouteUnit routeUnit, final FilterableSQLGenerator sqlGenerator) {
        return routeUnit.getTableMappers().stream().map(each -> generate(routeUnit, each, sqlGenerator)).collect(Collectors.toList());
    }
    
    private ExecutionUnit generate(final RouteUnit routeUnit, final RouteMapper tableMapper, final FilterableSQLGenerator sqlGenerator) {
        String sql = sqlGenerator.generate(tableMapper.getActualName());
        return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), new SQLUnit(sql, Collections.emptyList(), Collections.singletonList(tableMapper)));
    }
}
