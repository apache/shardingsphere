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

package org.apache.shardingsphere.core.optimize.sharding.segment.condition.engine;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding condition engine for insert clause.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class InsertClauseShardingConditionEngine {
    
    private final ShardingRule shardingRule;
    
    /**
     * Create sharding conditions.
     * 
     * @param insertStatement insert statement
     * @param parameters SQL parameters
     * @param columnNames column names
     * @param values values
     * @param generatedKey generated key
     * @return sharding conditions
     */
    public List<ShardingCondition> createShardingConditions(
            final InsertStatement insertStatement, final List<Object> parameters, final Collection<String> columnNames, final Collection<InsertValue> values, final GeneratedKey generatedKey) {
        List<ShardingCondition> result = getShardingConditions(insertStatement, columnNames, values, parameters);
        Iterator<Comparable<?>> generatedValues = null == generatedKey ? Collections.<Comparable<?>>emptyList().iterator() : generatedKey.getGeneratedValues().iterator();
        for (ShardingCondition each : result) {
            if (isNeedAppendGeneratedKeyCondition(generatedKey, insertStatement.getTable().getTableName())) {
                each.getRouteValues().add(
                        new ListRouteValue<>(generatedKey.getColumnName(), insertStatement.getTable().getTableName(), Collections.<Comparable<?>>singletonList(generatedValues.next())));
            }
        }
        return result;
    }
    
    private List<ShardingCondition> getShardingConditions(
            final InsertStatement insertStatement, final Collection<String> columnNames, final Collection<InsertValue> values, final List<Object> parameters) {
        List<ShardingCondition> result = new LinkedList<>();
        for (InsertValue each : values) {
            result.add(getShardingCondition(insertStatement, columnNames.iterator(), each, parameters));
        }
        return result;
    }
    
    private ShardingCondition getShardingCondition(final InsertStatement insertStatement, final Iterator<String> columnNames, final InsertValue insertValue, final List<Object> parameters) {
        ShardingCondition result = new ShardingCondition();
        for (ExpressionSegment each : insertValue.getAssignments()) {
            String columnName = columnNames.next();
            if (each instanceof SimpleExpressionSegment) {
                fillShardingCondition(result, insertStatement.getTable().getTableName(), columnName, (SimpleExpressionSegment) each, parameters);
            }
        }
        return result;
    }
    
    private void fillShardingCondition(final ShardingCondition shardingCondition, 
                                       final String tableName, final String columnName, final SimpleExpressionSegment expressionSegment, final List<Object> parameters) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            shardingCondition.getRouteValues().add(new ListRouteValue<>(columnName, tableName, Collections.singletonList(getRouteValue(expressionSegment, parameters))));
        }
    }
    
    private Comparable<?> getRouteValue(final SimpleExpressionSegment expressionSegment, final List<Object> parameters) {
        Object result;
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            result = parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        } else {
            result = ((LiteralExpressionSegment) expressionSegment).getLiterals();
        }
        Preconditions.checkArgument(result instanceof Comparable, "Sharding value must implements Comparable.");
        return (Comparable) result;
    }
    
    private boolean isNeedAppendGeneratedKeyCondition(final GeneratedKey generatedKey, final String tableName) {
        return null != generatedKey && generatedKey.isGenerated() && shardingRule.isShardingColumn(generatedKey.getColumnName(), tableName);
    }
}
