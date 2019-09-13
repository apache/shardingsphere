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

package org.apache.shardingsphere.core.rewrite.builder.parameter.group;

import lombok.Getter;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Parameter builder for group.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class GroupParameterBuilder implements ParameterBuilder {
    
    @Getter
    private final List<Object> originalParameters;
    
    private final Collection<ValueParametersGroup> parametersGroups;
    
    public GroupParameterBuilder(final List<Object> parameters, final InsertOptimizedStatement insertOptimizedStatement) {
        originalParameters = new LinkedList<>(parameters);
        parametersGroups = createParametersGroup(insertOptimizedStatement);
    }
    
    private Collection<ValueParametersGroup> createParametersGroup(final InsertOptimizedStatement insertOptimizedStatement) {
        Collection<ValueParametersGroup> result = new LinkedList<>();
        Iterator<ShardingCondition> shardingConditions = null;
        if (insertOptimizedStatement instanceof ShardingInsertOptimizedStatement) {
            shardingConditions = ((ShardingInsertOptimizedStatement) insertOptimizedStatement).getShardingConditions().getConditions().iterator();
        }
        for (InsertValue each : insertOptimizedStatement.getInsertValues()) {
            Collection<DataNode> dataNodes = null == shardingConditions ? Collections.<DataNode>emptyList() : shardingConditions.next().getDataNodes();
            result.add(new ValueParametersGroup(each.getParameters(), dataNodes));
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>();
        for (ValueParametersGroup each : parametersGroups) {
            result.addAll(each.getParameters());
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters(final RoutingUnit routingUnit) {
        List<Object> result = new LinkedList<>();
        for (ValueParametersGroup each : parametersGroups) {
            if (isAppendInsertParameter(each, routingUnit)) {
                result.addAll(each.getParameters());
            }
        }
        return result;
    }
    
    private boolean isAppendInsertParameter(final ValueParametersGroup parametersGroup, final RoutingUnit routingUnit) {
        return parametersGroup.getDataNodes().isEmpty() || isInSameDataNode(parametersGroup, routingUnit);
    }
    
    private boolean isInSameDataNode(final ValueParametersGroup parametersGroup, final RoutingUnit routingUnit) {
        for (DataNode each : parametersGroup.getDataNodes()) {
            if (routingUnit.getTableUnit(each.getDataSourceName(), each.getTableName()).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
