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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo;

import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Set;

/**
 * Parameter filterable.
 * 
 * <p>
 * This interface allows SQL tokens to declare which parameters should be removed
 * for specific route units during SQL rewriting. It provides a generic mechanism
 * for parameter filtering that can be used by various features, though it is
 * currently designed primarily for sharding IN predicate optimization.
 * </p>
 * 
 * <p>
 * Tokens implementing this interface can control parameter filtering on a per-route
 * basis, enabling optimizations such as sending only relevant IN clause values to
 * each shard.
 * </p>
 */
public interface ParameterFilterable {
    
    /**
     * Get removed parameter indices for the specified route unit.
     *
     * <p>
     * Returns the set of parameter indices that should be removed from the
     * parameter list when generating SQL for the given route unit. Parameter
     * indices are zero-based and correspond to the positions in the original
     * parameter list.
     * </p>
     *
     * @param routeUnit route unit
     * @return set of parameter indices to remove; empty set if no parameters should be removed
     */
    Set<Integer> getRemovedParameterIndices(RouteUnit routeUnit);
    
    /**
     * Determine whether parameter filtering is enabled.
     *
     * <p>
     * This default method allows implementations to conditionally enable or disable
     * parameter filtering. By default, filtering is enabled for all implementations.
     * </p>
     *
     * @return true if parameter filtering is enabled; false otherwise
     */
    default boolean isParameterFilterable() {
        return true;
    }
}
