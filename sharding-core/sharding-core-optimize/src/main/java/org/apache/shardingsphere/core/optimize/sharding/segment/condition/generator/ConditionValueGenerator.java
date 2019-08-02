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

package org.apache.shardingsphere.core.optimize.sharding.segment.condition.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.api.segment.Column;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.List;

/**
 * Condition value generator.
 *
 * @author zhangliang
 * 
 * @param <T> type of predicate right value
 */
public interface ConditionValueGenerator<T extends PredicateRightValue> {
    
    /**
     * Generate route value.
     * 
     * @param predicateRightValue predicate right value
     * @param column column
     * @param parameters SQL parameters
     * @return route value
     */
    Optional<RouteValue> generate(T predicateRightValue, Column column, List<Object> parameters);
}
