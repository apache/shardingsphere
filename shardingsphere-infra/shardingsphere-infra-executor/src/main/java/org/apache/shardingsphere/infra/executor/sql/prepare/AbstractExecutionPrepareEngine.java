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

package org.apache.shardingsphere.infra.executor.sql.prepare;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract execution prepare engine.
 * 
 * @param <T> type of input value
 */
public abstract class AbstractExecutionPrepareEngine<T> implements ExecutionPrepareEngine<T> {
    
    static {
        ShardingSphereServiceLoader.register(ExecutionPrepareDecorator.class);
    }
    
    private final int maxConnectionsSizePerQuery;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, ExecutionPrepareDecorator> decorators;
    
    protected AbstractExecutionPrepareEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
        decorators = OrderedSPIRegistry.getRegisteredServices(ExecutionPrepareDecorator.class, rules);
    }
    
    @Override
    public final ExecutionGroupContext<T> prepare(final RouteContext routeContext, final Collection<ExecutionUnit> executionUnits, 
                                                  final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        Collection<ExecutionGroup<T>> result = new LinkedList<>();
        for (Entry<String, List<SQLUnit>> entry : aggregateSQLUnitGroups(executionUnits).entrySet()) {
            String dataSourceName = entry.getKey();
            List<SQLUnit> sqlUnits = entry.getValue();
            List<List<SQLUnit>> sqlUnitGroups = group(sqlUnits, sqlStatementContext);
            int sqlUnitSize = sqlStatementContext instanceof SelectStatementContext ? sqlUnitGroups.size() : sqlUnits.size();
            ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnitSize ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
            result.addAll(group(dataSourceName, sqlUnitGroups, connectionMode));
        }
        return decorate(routeContext, result);
    }
    
    private List<List<SQLUnit>> group(final List<SQLUnit> sqlUnits, final SQLStatementContext<?> sqlStatementContext) {
        int desiredPartitionSize = Math.max(0 == sqlUnits.size() % maxConnectionsSizePerQuery ? sqlUnits.size() / maxConnectionsSizePerQuery : sqlUnits.size() / maxConnectionsSizePerQuery + 1, 1);
        List<List<SQLUnit>> sqlUnitGroups = Lists.partition(sqlUnits, desiredPartitionSize);
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return sqlUnitGroups;
        }
        List<List<SQLUnit>> result = new LinkedList<>();
        for (List<SQLUnit> each : sqlUnitGroups) {
            result.add(groupSQLUnit(each));
        }
        return result;
    }
    
    protected abstract List<ExecutionGroup<T>> group(String dataSourceName, List<List<SQLUnit>> sqlUnitGroups, ConnectionMode connectionMode) throws SQLException;
    
    private List<SQLUnit> groupSQLUnit(final List<SQLUnit> sqlUnits) {
        if (1 == sqlUnits.size()) {
            return sqlUnits;
        }
        List<Object> parameters = new LinkedList<>();
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (SQLUnit each : sqlUnits) {
            parameters.addAll(each.getParameters());
            String sql = SQLUtil.trimSemicolon(each.getSql());
            if (index++ < sqlUnits.size() - 1) {
                builder.append(QuoteCharacter.PARENTHESES.getStartDelimiter()).append(sql).append(QuoteCharacter.PARENTHESES.getEndDelimiter()).append(" UNION ALL ");
            } else {
                builder.append(sql);
            }
        }
        return Collections.singletonList(new SQLUnit(builder.toString(), parameters));
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
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ExecutionGroupContext<T> decorate(final RouteContext routeContext, final Collection<ExecutionGroup<T>> executionGroups) {
        Collection<ExecutionGroup<T>> result = executionGroups;
        for (Entry<ShardingSphereRule, ExecutionPrepareDecorator> each : decorators.entrySet()) {
            result = each.getValue().decorate(routeContext, each.getKey(), result);
        }
        return new ExecutionGroupContext(result);
    }
}
