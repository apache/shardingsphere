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

import org.apache.shardingsphere.infra.executor.kernel.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Execution group engine.
 * 
 * @param <T> type of input value
 */
public interface ExecutionGroupEngine<T> {
    
    /**
     * Group execution units.
     *
     * @param routeContext route context
     * @param executionUnits execution units
     * @return execution groups
     * @throws SQLException SQL exception
     */
    Collection<ExecutionGroup<T>> group(RouteContext routeContext, Collection<ExecutionUnit> executionUnits) throws SQLException;
}
