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

package org.apache.shardingsphere.core.rewrite.builder;

import lombok.Getter;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parameter builder.
 *
 * @author panjuan
 */
@Getter
public final class ParameterBuilder {
    
    private final List<Object> originalParameters;
    
    private final Map<Integer, Object> assistedIndexAndParametersForUpdate;
    
    private final List<InsertParameterUnit> insertParameterUnits;
    
    public ParameterBuilder(final List<Object> parameters, final InsertOptimizeResult insertOptimizeResult) {
        originalParameters = new LinkedList<>(parameters);
        assistedIndexAndParametersForUpdate = new HashMap<>();
        insertParameterUnits = createInsertParameterUnits(insertOptimizeResult);
    }
    
    private List<InsertParameterUnit> createInsertParameterUnits(final InsertOptimizeResult insertOptimizeResult) {
        List<InsertParameterUnit> result = new LinkedList<>();
        if (null == insertOptimizeResult) {
            return result;
        }
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            result.add(new InsertParameterUnit(Arrays.asList(each.getParameters()), each.getDataNodes()));
        }
        return result;
    }
    
    /**
     * Get parameters.
     *
     * @return parameters
     */
    public List<Object> getParameters() {
        List<Object> result = getInsertParameters();
        return result.isEmpty() ? getRevisedParameters() : result;
    }
    
    /**
     * Get parameters.
     * 
     * @param routingUnit routing unit
     * @return parameters
     */
    public List<Object> getParameters(final RoutingUnit routingUnit) {
        List<Object> result = getInsertParameters(routingUnit);
        return result.isEmpty() ? getRevisedParameters() : result;
    }
    
    private List<Object> getInsertParameters() {
        List<Object> result = new LinkedList<>();
        for (InsertParameterUnit each : insertParameterUnits) {
            result.addAll(each.getParameters());
        }
        return result;
    }
    
    private List<Object> getInsertParameters(final RoutingUnit routingUnit) {
        List<Object> result = new LinkedList<>();
        for (InsertParameterUnit each : insertParameterUnits) {
            if (isAppendInsertParameter(each, routingUnit)) {
                result.addAll(each.getParameters());
            }
        }
        return result;
    }
    
    private boolean isAppendInsertParameter(final InsertParameterUnit unit, final RoutingUnit routingUnit) {
        for (DataNode each : unit.getDataNodes()) {
            if (routingUnit.getTableUnit(each.getDataSourceName(), each.getTableName()).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private List<Object> getRevisedParameters() {
        List<Object> result = new LinkedList<>(originalParameters);
        for (Entry<Integer, Object> entry : assistedIndexAndParametersForUpdate.entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
