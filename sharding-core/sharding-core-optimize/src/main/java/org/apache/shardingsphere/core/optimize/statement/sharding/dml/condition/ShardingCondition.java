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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sharding condition.
 * 
 * @author maxiaoguang
 * @author zhangliang
 */
@Getter
@ToString
public class ShardingCondition {
    
    private final List<RouteValue> routeValues = new LinkedList<>();
    
    /**
     * Get route values map.
     *
     * @return route values map
     */
    public Map<Column, List<RouteValue>> getRouteValuesMap() {
        Map<Column, List<RouteValue>> result = new LinkedHashMap<>(routeValues.size(), 1);
        for (RouteValue each : routeValues) {
            Column column = new Column(each.getColumnName(), each.getTableName());
            if (!result.containsKey(column)) {
                result.put(column, new LinkedList<RouteValue>());
            }
            result.get(column).add(each);
        }
        return result;
    }
}
