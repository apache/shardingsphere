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

package org.apache.shardingsphere.core.rewrite.parameter.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.rewrite.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Grouped Parameter builder.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class GroupedParameterBuilder implements ParameterBuilder {
    
    @Getter
    private final List<List<Object>> parameterGroups;
    
    @Setter
    private ShardingConditions shardingConditions;
    
    @Getter
    private final List<Map<Integer, Object>> addedIndexAndParameterGroups;
    
    @Getter
    private final List<Map<Integer, Object>> replacedIndexAndParameterGroups;
    
    public GroupedParameterBuilder(final List<List<Object>> parameterGroups) {
        this.parameterGroups = parameterGroups;
        addedIndexAndParameterGroups = createAdditionalParameterGroups();
        replacedIndexAndParameterGroups = createAdditionalParameterGroups();
    }
    
    private List<Map<Integer, Object>> createAdditionalParameterGroups() {
        List<Map<Integer, Object>> result = new ArrayList<>(parameterGroups.size());
        for (int i = 0; i < parameterGroups.size(); i++) {
            result.add(new HashMap<Integer, Object>());
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>();
        int count = 0;
        for (List<Object> each : parameterGroups) {
            result.addAll(getParameters(each, count));
            count++;
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters(final RoutingUnit routingUnit) {
        List<Object> result = new LinkedList<>();
        Iterator<ShardingCondition> shardingConditionIterator = shardingConditions.getConditions().iterator();
        int count = 0;
        for (List<Object> each : parameterGroups) {
            if (!shardingConditionIterator.hasNext() || isInSameDataNode(shardingConditionIterator.next(), routingUnit)) {
                result.addAll(getParameters(each, count));
            }
            count++;
        }
        return result;
    }
    
    private List<Object> getParameters(final List<Object> parameterGroup, final int count) {
        List<Object> result = new LinkedList<>();
        result.addAll(parameterGroup);
        for (Entry<Integer, Object> entry : replacedIndexAndParameterGroups.get(count).entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        for (Entry<Integer, Object> entry : addedIndexAndParameterGroups.get(count).entrySet()) {
            int index = entry.getKey();
            if (index < result.size()) {
                result.add(index, entry.getValue());
            } else {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    
    private boolean isInSameDataNode(final ShardingCondition shardingCondition, final RoutingUnit routingUnit) {
        if (shardingCondition.getDataNodes().isEmpty()) {
            return true;
        }
        for (DataNode each : shardingCondition.getDataNodes()) {
            if (routingUnit.getTableUnit(each.getDataSourceName(), each.getTableName()).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
