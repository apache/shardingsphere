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

package org.apache.shardingsphere.replica.execute.group;

import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.resourced.ResourceManagedExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.group.ExecuteGroupDecorator;
import org.apache.shardingsphere.replica.constant.ReplicaOrder;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Execute group decorator for replica.
 * 
 * @param <T> type of input value 
 */
public final class ReplicaExecuteGroupDecorator<T extends ResourceManagedExecuteUnit> implements ExecuteGroupDecorator<T, ReplicaRule> {
    
    @Override
    public Collection<InputGroup<T>> decorate(final ReplicaRule rule, final Collection<InputGroup<T>> inputGroups) {
        Map<String, InputGroup<T>> result = new LinkedHashMap<>(inputGroups.size(), 1);
        for (InputGroup<T> each : inputGroups) {
            T sample = each.getInputs().get(0);
            String dataSourceName = sample.getExecutionUnit().getDataSourceName();
            Optional<String> logicDataSource = rule.findLogicDataSource(dataSourceName);
            if (logicDataSource.isPresent() && result.containsKey(dataSourceName)) {
                result.get(dataSourceName).getInputs().addAll(each.getInputs());
            } else {
                result.put(dataSourceName, each);
            }
        }
        return result.values();
    }
    
    @Override
    public int getOrder() {
        return ReplicaOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaRule> getTypeClass() {
        return ReplicaRule.class;
    }
}
