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

package org.apache.shardingsphere.core.routing;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Batch route unit.
 * 
 * @author panjuan
 */
@Getter
@EqualsAndHashCode(of = { "routeUnit" })
@ToString
public final class BatchRouteUnit {
    
    private final RouteUnit routeUnit;
    
    private final Map<Integer, Integer> jdbcAndActualAddBatchCallTimesMap = new LinkedHashMap<>();
    
    @Getter(AccessLevel.NONE)
    private int actualCallAddBatchTimes;
    
    public BatchRouteUnit(final RouteUnit routeUnit) {
        this.routeUnit = routeUnit;
    }
    
    /**
     * Map times of use JDBC API call addBatch and times of actual call addBatch after route.
     *
     * @param jdbcAddBatchTimes times of use JDBC API call addBatch
     */
    public void mapAddBatchCount(final int jdbcAddBatchTimes) {
        jdbcAndActualAddBatchCallTimesMap.put(jdbcAddBatchTimes, actualCallAddBatchTimes++);
    }
    
    /**
     * Get parameter sets.
     * 
     * @return parameter sets
     */
    public List<List<Object>> getParameterSets() {
        List<List<Object>> result = new LinkedList<>();
        if (routeUnit.getSqlUnit().getParameters().isEmpty() || 0 == actualCallAddBatchTimes) {
            result.add(Collections.emptyList());
        } else {
            result.addAll(Lists.partition(routeUnit.getSqlUnit().getParameters(), routeUnit.getSqlUnit().getParameters().size() / actualCallAddBatchTimes));
        }
        return result;
    }
}
