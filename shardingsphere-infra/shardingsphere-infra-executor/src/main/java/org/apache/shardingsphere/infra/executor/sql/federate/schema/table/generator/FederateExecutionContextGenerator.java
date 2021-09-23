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

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.federate.execute.RelNodeScanContext;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederationTableMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Federate execution context generator.
 */
public final class FederateExecutionContextGenerator {
    
    /**
     * Generate execution context.
     * 
     * @param routeExecutionContext route execution context
     * @param tableMetaData table meta data
     * @param scanContext rel node scan context
     * @param quoteCharacter quote character
     * @return generated execution context
     */
    public ExecutionContext generate(final ExecutionContext routeExecutionContext, 
                                     final FederationTableMetaData tableMetaData, final RelNodeScanContext scanContext, final QuoteCharacter quoteCharacter) {
        FederationSQLGenerator sqlGenerator = new FederationSQLGenerator(scanContext, tableMetaData.getColumnNames(), quoteCharacter);
        RouteContext filteredRouteContext = new RouteContextFilter().filter(tableMetaData.getName(), routeExecutionContext.getRouteContext());
        return new ExecutionContext(routeExecutionContext.getLogicSQL(), generate(filteredRouteContext.getRouteUnits(), sqlGenerator), filteredRouteContext);
    }
    
    private Collection<ExecutionUnit> generate(final Collection<RouteUnit> routeUnits, final FederationSQLGenerator sqlGenerator) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (RouteUnit each: routeUnits) {
            result.addAll(generate(each, sqlGenerator));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> generate(final RouteUnit routeUnit, final FederationSQLGenerator sqlGenerator) {
        return routeUnit.getTableMappers().stream().map(each -> generate(routeUnit, each, sqlGenerator)).collect(Collectors.toList());
    }
    
    private ExecutionUnit generate(final RouteUnit routeUnit, final RouteMapper tableMapper, final FederationSQLGenerator sqlGenerator) {
        String sql = sqlGenerator.generate(tableMapper.getActualName());
        return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), new SQLUnit(sql, Collections.emptyList(), Collections.singletonList(tableMapper)));
    }
}
