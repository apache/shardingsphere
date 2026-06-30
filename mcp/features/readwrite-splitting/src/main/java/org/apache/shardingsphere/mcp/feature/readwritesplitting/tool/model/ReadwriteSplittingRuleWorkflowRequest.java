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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model;

import lombok.Getter;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Readwrite-splitting rule workflow request.
 */
@Getter
public final class ReadwriteSplittingRuleWorkflowRequest extends WorkflowRequest {
    
    private String ruleName = "";
    
    private String writeStorageUnit = "";
    
    private final List<String> readStorageUnits = new LinkedList<>();
    
    private String transactionalReadQueryStrategy = "";
    
    private String loadBalancerType = "";
    
    private final Map<String, String> loadBalancerProperties = new LinkedHashMap<>(8, 1F);
    
    /**
     * Set rule name.
     *
     * @param ruleName rule name
     */
    public void setRuleName(final String ruleName) {
        this.ruleName = null == ruleName ? "" : ruleName.trim();
    }
    
    /**
     * Set write storage unit.
     *
     * @param writeStorageUnit write storage unit
     */
    public void setWriteStorageUnit(final String writeStorageUnit) {
        this.writeStorageUnit = null == writeStorageUnit ? "" : writeStorageUnit.trim();
    }
    
    /**
     * Set read storage units from a comma-separated value.
     *
     * @param readStorageUnits read storage units
     */
    public void setReadStorageUnits(final String readStorageUnits) {
        setReadStorageUnits(Arrays.stream(null == readStorageUnits ? new String[0] : readStorageUnits.split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList());
    }
    
    /**
     * Set read storage units.
     *
     * @param readStorageUnits read storage units
     */
    public void setReadStorageUnits(final Collection<String> readStorageUnits) {
        if (null == readStorageUnits || readStorageUnits.isEmpty()) {
            return;
        }
        this.readStorageUnits.clear();
        readStorageUnits.stream().map(each -> null == each ? "" : each.trim()).filter(each -> !each.isEmpty()).forEach(this.readStorageUnits::add);
    }
    
    /**
     * Set transactional read query strategy.
     *
     * @param transactionalReadQueryStrategy transactional read query strategy
     */
    public void setTransactionalReadQueryStrategy(final String transactionalReadQueryStrategy) {
        this.transactionalReadQueryStrategy = null == transactionalReadQueryStrategy ? "" : transactionalReadQueryStrategy.trim();
    }
    
    /**
     * Set load balancer type.
     *
     * @param loadBalancerType load balancer type
     */
    public void setLoadBalancerType(final String loadBalancerType) {
        this.loadBalancerType = null == loadBalancerType ? "" : loadBalancerType.trim();
    }
    
    /**
     * Put load balancer properties.
     *
     * @param properties load balancer properties
     */
    public void putLoadBalancerProperties(final Map<String, String> properties) {
        if (null != properties) {
            loadBalancerProperties.putAll(properties);
        }
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        return "primary".equals(algorithmRole) ? loadBalancerProperties : super.getAlgorithmProperties(algorithmRole);
    }
    
    @Override
    public ReadwriteSplittingRuleWorkflowRequest copy() {
        ReadwriteSplittingRuleWorkflowRequest result = copyTo(new ReadwriteSplittingRuleWorkflowRequest());
        result.setRuleName(ruleName);
        result.setWriteStorageUnit(writeStorageUnit);
        result.setReadStorageUnits(readStorageUnits);
        result.setTransactionalReadQueryStrategy(transactionalReadQueryStrategy);
        result.setLoadBalancerType(loadBalancerType);
        result.loadBalancerProperties.putAll(loadBalancerProperties);
        return result;
    }
    
    /**
     * Merge the current request with the previous request.
     *
     * @param previousRequest previous workflow request
     * @param currentRequest current readwrite-splitting rule request
     * @return merged request
     */
    public static ReadwriteSplittingRuleWorkflowRequest merge(final WorkflowRequest previousRequest, final ReadwriteSplittingRuleWorkflowRequest currentRequest) {
        if (null == previousRequest && null == currentRequest) {
            return null;
        }
        ReadwriteSplittingRuleWorkflowRequest result = copyPreviousRequest(previousRequest);
        if (null != currentRequest) {
            currentRequest.overlayTo(result);
            currentRequest.overlayFeatureFieldsTo(result);
        }
        return result;
    }
    
    private void overlayFeatureFieldsTo(final ReadwriteSplittingRuleWorkflowRequest target) {
        if (!ruleName.isEmpty()) {
            target.setRuleName(ruleName);
        }
        if (!writeStorageUnit.isEmpty()) {
            target.setWriteStorageUnit(writeStorageUnit);
        }
        if (!readStorageUnits.isEmpty()) {
            target.readStorageUnits.clear();
            target.readStorageUnits.addAll(readStorageUnits);
        }
        if (!transactionalReadQueryStrategy.isEmpty()) {
            target.setTransactionalReadQueryStrategy(transactionalReadQueryStrategy);
        }
        if (!loadBalancerType.isEmpty()) {
            target.setLoadBalancerType(loadBalancerType);
        }
        target.loadBalancerProperties.putAll(loadBalancerProperties);
    }
    
    private static ReadwriteSplittingRuleWorkflowRequest copyPreviousRequest(final WorkflowRequest previousRequest) {
        if (null == previousRequest) {
            return new ReadwriteSplittingRuleWorkflowRequest();
        }
        if (previousRequest instanceof ReadwriteSplittingRuleWorkflowRequest) {
            return ((ReadwriteSplittingRuleWorkflowRequest) previousRequest).copy();
        }
        ReadwriteSplittingRuleWorkflowRequest result = new ReadwriteSplittingRuleWorkflowRequest();
        copyFieldsTo(previousRequest, result);
        return result;
    }
}
