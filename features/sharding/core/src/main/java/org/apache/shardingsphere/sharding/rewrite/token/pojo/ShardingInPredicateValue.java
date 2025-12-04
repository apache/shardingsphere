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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a value within a sharding IN predicate with its route distribution information.
 * This class encapsulates not only the value information but also which route units this value belongs to.
 *
 * <p>In ShardingSphere's SQL rewriting process, IN predicates need special handling for sharding optimization.
 * Each value in an IN clause can be either:
 * <ul>
 *   <li>A parameter marker (?): represented with parameterIndex and isParameter=true</li>
 *   <li>A literal value: represented with the actual value and isParameter=false</li>
 * </ul>
 * Additionally, each value now knows which route units it belongs to, simplifying the overall data structure.
 * </p>
 *
 * @author yinh
 */
@RequiredArgsConstructor
@Getter
public final class ShardingInPredicateValue {
    
    /**
     * The index of the parameter marker in the original prepared statement.
     * Only meaningful when {@link #isParameter} is true.
     */
    private final int parameterIndex;
    
    /**
     * The actual value of this predicate component.
     * For parameter markers, this represents the bound parameter value.
     * For literals, this represents the literal value from the SQL.
     * Must implement Comparable for sharding algorithm processing.
     */
    private final Comparable<?> value;
    
    /**
     * Indicates whether this value originates from a parameter marker (?) in the SQL.
     * When true, this value should be represented as "?" in rewritten SQL.
     * When false, this value should be formatted as a literal in rewritten SQL.
     */
    private final boolean isParameter;
    
    /**
     * Set of route units that this value belongs to.
     * During SQL rewriting, this value will only be included in the IN clauses
     * for the route units specified in this set.
     */
    private final Set<RouteUnit> targetRoutes;
    
    /**
     * Indicates whether this value is an orphan parameter that doesn't belong to any route unit.
     * Orphan parameters are those that cannot be mapped to any specific shard based on sharding algorithm.
     */
    private final boolean isOrphan;
    
    /**
     * Convenience constructor for non-orphan values.
     */
    public ShardingInPredicateValue(final int parameterIndex, final Comparable<?> value,
                                    final boolean isParameter, final Set<RouteUnit> targetRoutes) {
        this(parameterIndex, value, isParameter, targetRoutes, false);
    }
    
    /**
     * Convenience constructor for orphan parameters.
     *
     * @param parameterIndex the index of the parameter marker in the original prepared statement
     * @param value the actual value of this predicate component
     * @param isParameter true if this value originates from a parameter marker, false if it's a literal
     * @return a new ShardingInPredicateValue instance representing an orphan parameter
     */
    public static ShardingInPredicateValue createOrphan(final int parameterIndex, final Comparable<?> value, final boolean isParameter) {
        return new ShardingInPredicateValue(parameterIndex, value, isParameter, Collections.emptySet(), true);
    }
    
    /**
     * Check if this value belongs to the specified route unit.
     *
     * @param routeUnit the route unit to check
     * @return true if this value belongs to the specified route unit
     */
    public boolean belongsToRoute(final RouteUnit routeUnit) {
        return !isOrphan && targetRoutes.contains(routeUnit);
    }
}
