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

package org.apache.shardingsphere.mcp.support.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP model-facing payload contract.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPModelFacingPayloadContract {
    
    private static final Collection<String> REMOVED_MODEL_FACING_FIELDS = Set.of(
            "target_tool", "target_resource", "required_arguments", "action_kind", "suggested_next_tool", "suggested_next_tools", "recommended_next_tool",
            "recommended_recovery", "suggested_next_action", "approved_by_user", "requires_user_approval", "approval_required", "user_overrides");
    
    private static final Map<String, Collection<String>> NEXT_ACTION_REQUIRED_FIELDS = Map.of(
            "resource_read", Set.of("order", "type", "title", "resource_uri"),
            "tool_call", Set.of("order", "type", "title", "tool_name", "arguments"),
            "completion", Set.of("order", "type", "title", "ref", "argument"),
            "ask_user", Set.of("order", "type", "title", "question"),
            "terminal", Set.of("order", "type", "title"));
    
    private static final Map<String, Collection<String>> NEXT_ACTION_ALLOWED_FIELDS = Map.of(
            "resource_read", Set.of("order", "type", "title", "resource_uri", "reason", "depends_on"),
            "tool_call", Set.of("order", "type", "title", "tool_name", "arguments", "reason", "depends_on"),
            "completion", Set.of("order", "type", "title", "ref", "argument", "context", "missing_context_arguments", "resume_ref", "resume_arguments", "reason", "depends_on"),
            "ask_user", Set.of("order", "type", "title", "question", "required_inputs", "depends_on"),
            "terminal", Set.of("order", "type", "title", "reason", "depends_on"));
    
    private static final Collection<String> NEXT_ACTION_SCHEMA_ALLOWED_FIELDS = createNextActionSchemaAllowedFields();
    
    private static final Collection<String> MODEL_CRITICAL_FIELD_NAMES = List.of(
            MCPPayloadFieldNames.SUMMARY, MCPPayloadFieldNames.NEXT_ACTIONS, MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.RESOURCE,
            MCPPayloadFieldNames.SELF_RESOURCE, MCPPayloadFieldNames.PARENT_RESOURCE, MCPPayloadFieldNames.NEXT_RESOURCES, "manual_artifact_summary",
            "empty_state", "ambiguity_state", MCPPayloadFieldNames.RECOVERY, "recovery_guidance", "remediation");
    
    private static Collection<String> createNextActionSchemaAllowedFields() {
        Set<String> result = new LinkedHashSet<>();
        for (Collection<String> each : NEXT_ACTION_ALLOWED_FIELDS.values()) {
            result.addAll(each);
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Check whether a field name is a removed model-facing alias.
     *
     * @param fieldName field name
     * @return true if removed, otherwise false
     */
    public static boolean isRemovedFieldName(final String fieldName) {
        return REMOVED_MODEL_FACING_FIELDS.contains(fieldName);
    }
    
    /**
     * Get required fields for a canonical next_actions item type.
     *
     * @param actionType action type
     * @return required field names
     */
    public static Collection<String> getNextActionRequiredFields(final String actionType) {
        return NEXT_ACTION_REQUIRED_FIELDS.getOrDefault(actionType, List.of());
    }
    
    /**
     * Get allowed fields for a canonical next_actions item type.
     *
     * @param actionType action type
     * @return allowed field names
     */
    public static Collection<String> getNextActionAllowedFields(final String actionType) {
        return NEXT_ACTION_ALLOWED_FIELDS.getOrDefault(actionType, List.of());
    }
    
    /**
     * Get fields allowed by the next_actions item output schema.
     *
     * @return allowed schema field names
     */
    public static Collection<String> getNextActionSchemaAllowedFields() {
        return NEXT_ACTION_SCHEMA_ALLOWED_FIELDS;
    }
    
    /**
     * Get model-critical field names that must stay visible to MCP clients and E2E model prompts.
     *
     * @return model-critical field names
     */
    public static Collection<String> getModelCriticalFieldNames() {
        return MODEL_CRITICAL_FIELD_NAMES;
    }
}
