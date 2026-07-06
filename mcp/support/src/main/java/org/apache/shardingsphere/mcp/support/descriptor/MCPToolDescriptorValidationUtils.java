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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP tool descriptor validation utilities.
 */
public final class MCPToolDescriptorValidationUtils {
    
    private static final Collection<String> REQUIRED_WORKFLOW_PLAN_OUTPUT_FIELDS = List.of(
            "response_mode", MCPPayloadFieldNames.SUMMARY, WorkflowFieldNames.PLAN_ID, "workflow_kind", "status", "missing_required_inputs", "clarification_questions",
            "elicitation_support", "fallback_reason", "issues", "global_steps", "current_step", "algorithm_recommendations", "property_requirements",
            "validation_strategy", "delivery_mode", "execution_mode", "intent_inference", "argument_provenance", "review_focus", "proxy_topology_hint",
            "distsql_artifacts", MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.NEXT_ACTIONS);
    
    private static final Collection<String> REQUIRED_WORKFLOW_PLAN_META_FIELDS = List.of(
            "org.apache.shardingsphere/workflow-kind", "org.apache.shardingsphere/artifact-categories", "org.apache.shardingsphere/side-effect-scope",
            "org.apache.shardingsphere/related-resource-uris", "org.apache.shardingsphere/follow-up-tools");
    
    private MCPToolDescriptorValidationUtils() {
    }
    
    /**
     * Validate required workflow planning output fields.
     *
     * @param descriptor tool descriptor
     */
    public static void validateRequiredWorkflowPlanOutputFields(final MCPToolDescriptor descriptor) {
        validateRequiredOutputFields(descriptor, REQUIRED_WORKFLOW_PLAN_OUTPUT_FIELDS);
    }
    
    /**
     * Validate required workflow planning output fields with additional fields.
     *
     * @param descriptor tool descriptor
     * @param additionalFields additional required fields
     */
    public static void validateRequiredWorkflowPlanOutputFields(final MCPToolDescriptor descriptor, final Collection<String> additionalFields) {
        Collection<String> requiredFields = new LinkedList<>(REQUIRED_WORKFLOW_PLAN_OUTPUT_FIELDS);
        requiredFields.addAll(additionalFields);
        validateRequiredOutputFields(descriptor, requiredFields);
    }
    
    /**
     * Validate required output fields.
     *
     * @param descriptor tool descriptor
     * @param requiredFields required fields
     */
    public static void validateRequiredOutputFields(final MCPToolDescriptor descriptor, final Collection<String> requiredFields) {
        Map<?, ?> properties = (Map<?, ?>) descriptor.getOutputSchema().get("properties");
        for (String each : requiredFields) {
            ShardingSpherePreconditions.checkState(properties.containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` outputSchema must declare `%s`.", descriptor.getName(), each)));
            Object property = properties.get(each);
            ShardingSpherePreconditions.checkState(property instanceof Map,
                    () -> new IllegalStateException(String.format("Tool `%s` outputSchema property `%s` must be an object.", descriptor.getName(), each)));
            Object description = ((Map<?, ?>) property).get("description");
            checkDescription(null == description ? "" : description.toString(), String.format("Tool output field `%s.%s` description", descriptor.getName(), each));
        }
    }
    
    /**
     * Find input schema property.
     *
     * @param descriptor tool descriptor
     * @param fieldName field name
     * @return found input schema property
     */
    public static Optional<Map<?, ?>> findToolInputProperty(final MCPToolDescriptor descriptor, final String fieldName) {
        return findToolSchemaProperty(descriptor.getInputSchema(), fieldName);
    }
    
    /**
     * Find output schema property.
     *
     * @param descriptor tool descriptor
     * @param fieldName field name
     * @return found output schema property
     */
    public static Optional<Map<?, ?>> findToolOutputProperty(final MCPToolDescriptor descriptor, final String fieldName) {
        return findToolSchemaProperty(descriptor.getOutputSchema(), fieldName);
    }
    
    /**
     * Check whether input schema property is required.
     *
     * @param descriptor tool descriptor
     * @param fieldName field name
     * @return whether input schema property is required
     */
    public static boolean isRequiredToolInput(final MCPToolDescriptor descriptor, final String fieldName) {
        Object required = descriptor.getInputSchema().get("required");
        return required instanceof Collection && ((Collection<?>) required).contains(fieldName);
    }
    
    private static Optional<Map<?, ?>> findToolSchemaProperty(final Map<String, Object> schema, final String fieldName) {
        Object properties = schema.get("properties");
        if (!(properties instanceof Map)) {
            return Optional.empty();
        }
        Object property = ((Map<?, ?>) properties).get(fieldName);
        return property instanceof Map ? Optional.of((Map<?, ?>) property) : Optional.empty();
    }
    
    /**
     * Validate required workflow planning metadata fields.
     *
     * @param descriptor tool descriptor
     */
    public static void validateRequiredWorkflowPlanMetaFields(final MCPToolDescriptor descriptor) {
        validateRequiredMetaFields(descriptor, REQUIRED_WORKFLOW_PLAN_META_FIELDS);
    }
    
    /**
     * Validate required metadata fields.
     *
     * @param descriptor tool descriptor
     * @param requiredFields required fields
     */
    public static void validateRequiredMetaFields(final MCPToolDescriptor descriptor, final Collection<String> requiredFields) {
        Map<String, Object> meta = descriptor.getMeta();
        for (String each : requiredFields) {
            ShardingSpherePreconditions.checkState(meta.containsKey(each),
                    () -> new IllegalStateException(String.format("Tool `%s` metadata must declare `%s`.", descriptor.getName(), each)));
        }
    }
    
    /**
     * Check description.
     *
     * @param value description value
     * @param label description label
     */
    public static void checkDescription(final String value, final String label) {
        ShardingSpherePreconditions.checkState(null != value && !value.isBlank(), () -> new IllegalStateException(String.format("%s is required.", label)));
    }
}
