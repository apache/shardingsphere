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

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.order.OrderedSPI;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;

import java.util.Collection;

/**
 * Execute group decorator.
 * 
 * @param <T> type of input value
 * @param <R> type of ShardingSphere rule
 */
public interface ExecuteGroupDecorator<T, R extends ShardingSphereRule> extends OrderedSPI<R> {
    
    /**
     * Decorate input groups.
     * 
     * @param routeContext route context
     * @param rule ShardingSphere rule
     * @param inputGroups input groups to be decorated
     * @return decorated input groups.
     */
    Collection<InputGroup<T>> decorate(RouteContext routeContext, R rule, Collection<InputGroup<T>> inputGroups);
}
