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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.ParameterFilterable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SQL token for optimizing sharding IN predicates with value filtering per route unit.
 * Simplified data structure with route information embedded in individual values.
 */
public final class ShardingInPredicateToken extends SQLToken implements Substitutable, RouteUnitAware, ParameterFilterable {
    
    @Getter
    private final int stopIndex;
    
    /**
     * Column name for this IN predicate.
     */
    @Getter
    private final String columnName;
    
    /**
     * All values in this IN predicate with their route distribution information.
     */
    private final List<ShardingInPredicateValue> values;
    
    public ShardingInPredicateToken(final int startIndex,
                                    final int stopIndex,
                                    final String columnName,
                                    final List<ShardingInPredicateValue> values) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.columnName = columnName;
        this.values = values;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        List<ShardingInPredicateValue> routeValues = getValuesForRoute(routeUnit);
        if (routeValues.isEmpty()) {
            return "";
        }
        
        return buildInClause(columnName, routeValues);
    }
    
    @Override
    public Set<Integer> getRemovedParameterIndices(final RouteUnit routeUnit) {
        Set<Integer> result = new HashSet<>();
        for (ShardingInPredicateValue each : values) {
            // Only consider parameter markers (not literals)
            if (each.isParameter()) {
                // Remove parameters that don't belong to this route or are orphans
                if (!each.belongsToRoute(routeUnit) || each.isOrphan()) {
                    result.add(each.getParameterIndex());
                }
            }
        }
        return result;
    }
    
    @Override
    public boolean isParameterFilterable() {
        return values.stream().anyMatch(ShardingInPredicateValue::isParameter);
    }
    
    /**
     * Get values that belong to the specified route unit.
     *
     * @param routeUnit the route unit to filter values for
     * @return a list of values that belong to the specified route unit
     */
    private List<ShardingInPredicateValue> getValuesForRoute(final RouteUnit routeUnit) {
        return values.stream()
                .filter(value -> value.belongsToRoute(routeUnit))
                .collect(Collectors.toList());
    }
    
    /**
     * Build optimized IN clause for the given values.
     *
     * @param column the column name for the IN clause
     * @param valueList the list of values to include in the IN clause
     * @return the optimized IN clause as a string
     */
    private String buildInClause(final String column, final List<ShardingInPredicateValue> valueList) {
        if (valueList.size() == 1) {
            ShardingInPredicateValue single = valueList.get(0);
            return String.format("%s = %s", column,
                    single.isParameter() ? "?" : formatValue(single.getValue()));
        }
        String valueString = valueList.stream()
                .map(value -> value.isParameter() ? "?" : formatValue(value.getValue()))
                .collect(Collectors.joining(", "));
        return String.format("%s IN (%s)", column, valueString);
    }
    
    /**
     * Format a value for SQL output.
     *
     * @param value the value to format
     * @return the formatted value as a string
     */
    private String formatValue(final Comparable<?> value) {
        if (null == value) {
            return "NULL";
        }
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }
        return value.toString();
    }
}
