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

package org.apache.shardingsphere.infra.executor.sql.group;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract execution group engine.
 * 
 * @param <T> type of input value
 */
public abstract class AbstractExecutionGroupEngine<T> implements ExecutionGroupEngine<T> {
    
    static {
        ShardingSphereServiceLoader.register(ExecutionGroupDecorator.class);
    }
    
    private final int maxConnectionsSizePerQuery;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, ExecutionGroupDecorator> decorators;
    
    protected AbstractExecutionGroupEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
        decorators = OrderedSPIRegistry.getRegisteredServices(rules, ExecutionGroupDecorator.class);
    }
    
    @Override
    public final Collection<ExecutionGroup<T>> group(final RouteContext routeContext, final Collection<ExecutionUnit> executionUnits) throws SQLException {
        Collection<ExecutionGroup<T>> result = new LinkedList<>();
        for (Entry<String, List<SQLUnit>> entry : aggregateSQLUnitGroups(executionUnits).entrySet()) {
            result.addAll(group(entry.getKey(), createSQLUintGroupResult(entry.getValue())));
        }
        return decorate(routeContext, result);
    }
    
    protected abstract List<ExecutionGroup<T>> group(String dataSourceName, SQLUintGroupResult sqlUintGroupResult) throws SQLException;
    
    private Map<String, List<SQLUnit>> aggregateSQLUnitGroups(final Collection<ExecutionUnit> executionUnits) {
        Map<String, List<SQLUnit>> result = new LinkedHashMap<>(executionUnits.size(), 1);
        for (ExecutionUnit each : executionUnits) {
            if (!result.containsKey(each.getDataSourceName())) {
                result.put(each.getDataSourceName(), new LinkedList<>());
            }
            result.get(each.getDataSourceName()).add(each.getSqlUnit());
        }
        return result;
    }
    
    protected final SQLUintGroupResult createSQLUintGroupResult(final List<SQLUnit> sqlUnits) {
        int desiredPartitionSize = Math.max(0 == sqlUnits.size() % maxConnectionsSizePerQuery ? sqlUnits.size() / maxConnectionsSizePerQuery : sqlUnits.size() / maxConnectionsSizePerQuery + 1, 1);
        List<List<SQLUnit>> sqlUnitPartitions = Lists.partition(sqlUnits, desiredPartitionSize);
        ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnits.size() ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
        return new SQLUintGroupResult(sqlUnitPartitions, connectionMode);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<ExecutionGroup<T>> decorate(final RouteContext routeContext, final Collection<ExecutionGroup<T>> executionGroups) {
        Collection<ExecutionGroup<T>> result = executionGroups;
        for (Entry<ShardingSphereRule, ExecutionGroupDecorator> each : decorators.entrySet()) {
            result = each.getValue().decorate(routeContext, each.getKey(), result);
        }
        return result;
    }
}
