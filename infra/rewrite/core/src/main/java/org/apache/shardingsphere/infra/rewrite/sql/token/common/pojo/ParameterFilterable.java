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
 * Interface for SQL tokens that support parameter filtering.
 */
public interface ParameterFilterable {
    
    /**
     * Gets parameter indices that should be removed for the specified route unit.
     *
     * @param routeUnit the route unit.
     * @return set of parameter indices to remove.
     */
    Set<Integer> getRemovedParameterIndices(RouteUnit routeUnit);
    
    /**
     * Determine whether parameter filtering is supported.
     *
     * @return true If parameter filtering is supported.
     */
    default boolean isParameterFilterable() {
        return true;
    }
}
