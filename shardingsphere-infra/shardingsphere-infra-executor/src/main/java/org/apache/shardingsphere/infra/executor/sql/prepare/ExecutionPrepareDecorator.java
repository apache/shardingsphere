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

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;

import java.util.Collection;

/**
 * Execution prepare decorator.
 * 
 * @param <T> type of input value
 * @param <R> type of ShardingSphere rule
 */
public interface ExecutionPrepareDecorator<T, R extends ShardingSphereRule> extends OrderedSPI<R> {
    
    /**
     * Decorate execution groups.
     * 
     * @param routeContext route context
     * @param rule ShardingSphere rule
     * @param executionGroups execution groups to be decorated
     * @return decorated execution groups
     */
    Collection<ExecutionGroup<T>> decorate(RouteContext routeContext, R rule, Collection<ExecutionGroup<T>> executionGroups);
}
