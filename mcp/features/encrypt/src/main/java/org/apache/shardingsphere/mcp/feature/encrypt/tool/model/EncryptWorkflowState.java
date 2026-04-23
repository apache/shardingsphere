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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowFeatureData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt workflow state.
 */
public final class EncryptWorkflowState implements WorkflowFeatureData {
    
    @Getter
    @Setter
    private DerivedColumnPlan derivedColumnPlan;
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        return Map.of();
    }
    
    @Override
    public EncryptWorkflowState copy() {
        EncryptWorkflowState result = new EncryptWorkflowState();
        result.setDerivedColumnPlan(copyDerivedColumnPlan());
        return result;
    }
    
    private DerivedColumnPlan copyDerivedColumnPlan() {
        if (null == derivedColumnPlan) {
            return null;
        }
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setLogicalColumn(derivedColumnPlan.getLogicalColumn());
        result.setCipherColumnName(derivedColumnPlan.getCipherColumnName());
        result.setAssistedQueryColumnName(derivedColumnPlan.getAssistedQueryColumnName());
        result.setLikeQueryColumnName(derivedColumnPlan.getLikeQueryColumnName());
        result.setCipherColumnRequired(derivedColumnPlan.isCipherColumnRequired());
        result.setAssistedQueryColumnRequired(derivedColumnPlan.isAssistedQueryColumnRequired());
        result.setLikeQueryColumnRequired(derivedColumnPlan.isLikeQueryColumnRequired());
        result.setDataTypeStrategy(derivedColumnPlan.getDataTypeStrategy());
        derivedColumnPlan.getNameCollisions().forEach(each -> result.getNameCollisions().add(new LinkedHashMap<>(each)));
        return result;
    }
}
