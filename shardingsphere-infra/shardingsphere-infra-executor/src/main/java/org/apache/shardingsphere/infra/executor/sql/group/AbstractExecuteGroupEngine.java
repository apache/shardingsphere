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

import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract execute group engine.
 * 
 * @param <T> type of input value
 */
public abstract class AbstractExecuteGroupEngine<T> implements ExecuteGroupEngine<T> {
    
    static {
        ShardingSphereServiceLoader.register(ExecuteGroupDecorator.class);
    }
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, ExecuteGroupDecorator> decorators;

    protected AbstractExecuteGroupEngine(final Collection<ShardingSphereRule> rules) {
        decorators = OrderedSPIRegistry.getRegisteredServices(rules, ExecuteGroupDecorator.class);
    }
    
    @Override
    public final Collection<InputGroup<T>> generate(final RouteContext routeContext, final Collection<ExecutionUnit> executionUnits) throws SQLException {
        Collection<InputGroup<T>> result = new LinkedList<>();
        for (Entry<String, List<SQLUnit>> entry : aggregateSQLUnitGroups(executionUnits).entrySet()) {
            result.addAll(generateSQLExecuteGroups(entry.getKey(), entry.getValue()));
        }
        return decorate(routeContext, result);
    }
    
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
    
    protected abstract List<InputGroup<T>> generateSQLExecuteGroups(String dataSourceName, List<SQLUnit> sqlUnits) throws SQLException;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<InputGroup<T>> decorate(final RouteContext routeContext, final Collection<InputGroup<T>> inputGroups) {
        Collection<InputGroup<T>> result = inputGroups;
        for (Entry<ShardingSphereRule, ExecuteGroupDecorator> each : decorators.entrySet()) {
            result = each.getValue().decorate(routeContext, each.getKey(), result);
        }
        return result;
    }
}
