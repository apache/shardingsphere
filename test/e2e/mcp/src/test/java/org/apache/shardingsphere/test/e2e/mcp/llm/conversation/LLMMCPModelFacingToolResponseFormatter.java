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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LLMMCPModelFacingToolResponseFormatter {
    
    static String format(final Map<String, Object> response) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        copyIfPresent(response, result, "response_mode");
        copyIfPresent(response, result, "error_code");
        copyIfPresent(response, result, "message");
        copyIfPresent(response, result, "recovery_category");
        copyIfPresent(response, result, "result_kind");
        copyIfPresent(response, result, "status");
        copyIfPresent(response, result, "statement_type");
        copyIfPresent(response, result, "normalized_sql");
        copyIfPresent(response, result, "rows");
        copyIfPresent(response, result, "row_objects");
        copyIfPresent(response, result, "returned_row_count");
        copyIfPresent(response, result, "plan_id");
        copyIfPresent(response, result, "workflow_resource");
        copyIfPresent(response, result, "manual_artifact_summary");
        copyIfPresent(response, result, "manual_follow_up");
        copyCompactArtifactList(response, result, "manual_artifacts");
        copyCompactArtifactList(response, result, "exported_artifacts");
        copyIfPresent(response, result, "resources_to_read");
        copyModelFacingNextActions(response, result);
        copyIfPresent(response, result, "completion");
        copyIfPresent(response, result, "count");
        copyIfPresent(response, result, "has_more");
        copyIfPresent(response, result, "total_match_count");
        copyIfPresent(response, result, "returned_count");
        copyIfPresent(response, result, "truncated");
        copyIfPresent(response, result, "large_result_guidance");
        copyIfPresent(response, result, "search_context");
        copyIfPresent(response, result, "ambiguity_state");
        List<Map<String, Object>> resources = LLMMCPJsonValues.castToList(response.get("resources"));
        if (!resources.isEmpty()) {
            List<Map<String, Object>> compactResources = new LinkedList<>();
            for (Map<String, Object> each : resources) {
                Map<String, Object> compactResource = new LinkedHashMap<>(8, 1F);
                copyIfPresent(each, compactResource, "uri");
                copyIfPresent(each, compactResource, "name");
                copyIfPresent(each, compactResource, "title");
                copyIfPresent(each, compactResource, "mimeType");
                compactResources.add(compactResource.isEmpty() ? each : compactResource);
            }
            result.put("resources", compactResources);
        }
        copyPromptList(response, result);
        copyPromptMessages(response, result);
        copyCompactItems(response, result);
        copyCompactRecovery(response, result);
        return JsonUtils.toJsonString(result.isEmpty() ? response : result);
    }
    
    private static void copyIfPresent(final Map<String, Object> source, final Map<String, Object> target, final String fieldName) {
        if (source.containsKey(fieldName)) {
            target.put(fieldName, source.get(fieldName));
        }
    }
    
    private static void copyCompactItems(final Map<String, Object> source, final Map<String, Object> target) {
        List<Map<String, Object>> items = LLMMCPJsonValues.castToList(source.get("items"));
        if (items.isEmpty()) {
            return;
        }
        List<Map<String, Object>> compactItems = new LinkedList<>();
        for (Map<String, Object> each : items.subList(0, Math.min(items.size(), 5))) {
            Map<String, Object> compactItem = new LinkedHashMap<>(8, 1F);
            copyIfPresent(each, compactItem, "database");
            copyIfPresent(each, compactItem, "schema");
            copyIfPresent(each, compactItem, "objectType");
            copyIfPresent(each, compactItem, "table");
            copyIfPresent(each, compactItem, "view");
            copyIfPresent(each, compactItem, "column");
            copyIfPresent(each, compactItem, "name");
            copyIfPresent(each, compactItem, "resource");
            compactItems.add(compactItem.isEmpty() ? each : compactItem);
        }
        target.put("items", compactItems);
    }
    
    private static void copyPromptList(final Map<String, Object> source, final Map<String, Object> target) {
        List<Map<String, Object>> prompts = LLMMCPJsonValues.castToList(source.get("prompts"));
        if (prompts.isEmpty()) {
            return;
        }
        List<String> names = new LinkedList<>();
        for (Map<String, Object> each : prompts) {
            String name = Objects.toString(each.get("name"), "").trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        target.put("prompts", names);
    }
    
    private static void copyPromptMessages(final Map<String, Object> source, final Map<String, Object> target) {
        copyIfPresent(source, target, "description");
        List<Object> messages = LLMMCPJsonValues.castToList(source.get("messages"));
        if (!messages.isEmpty()) {
            target.put("message_count", messages.size());
        }
    }
    
    private static void copyCompactArtifactList(final Map<String, Object> source, final Map<String, Object> target, final String fieldName) {
        List<Map<String, Object>> artifacts = LLMMCPJsonValues.castToList(source.get(fieldName));
        if (artifacts.isEmpty()) {
            return;
        }
        List<Map<String, Object>> summaries = new LinkedList<>();
        for (Map<String, Object> each : artifacts.subList(0, Math.min(artifacts.size(), 3))) {
            Map<String, Object> summary = new LinkedHashMap<>(4, 1F);
            putArtifactCount(each, summary, "ddl_artifacts", "ddl_artifact_count");
            putArtifactCount(each, summary, "index_plan", "index_plan_count");
            putArtifactCount(each, summary, "distsql_artifacts", "distsql_artifact_count");
            if (!summary.isEmpty()) {
                summaries.add(summary);
            }
        }
        if (!summaries.isEmpty()) {
            target.put(fieldName, summaries);
        }
    }
    
    private static void putArtifactCount(final Map<String, Object> source, final Map<String, Object> target, final String sourceFieldName, final String targetFieldName) {
        List<Object> artifacts = LLMMCPJsonValues.castToList(source.get(sourceFieldName));
        if (!artifacts.isEmpty()) {
            target.put(targetFieldName, artifacts.size());
        }
    }
    
    private static void copyCompactRecovery(final Map<String, Object> source, final Map<String, Object> target) {
        if (!source.containsKey("recovery")) {
            return;
        }
        Map<String, Object> recovery = LLMMCPJsonValues.castToMap(source.get("recovery"));
        if (recovery.isEmpty()) {
            return;
        }
        Map<String, Object> compactRecovery = new LinkedHashMap<>(8, 1F);
        copyIfPresent(recovery, compactRecovery, "recovery_category");
        copyIfPresent(recovery, compactRecovery, "category");
        copyIfPresent(recovery, compactRecovery, "model_action");
        copyIfPresent(recovery, compactRecovery, "plan_id");
        copyIfPresent(recovery, compactRecovery, "completion_first");
        copyIfPresent(recovery, compactRecovery, "suggested_arguments");
        copyIfPresent(recovery, compactRecovery, "resources_to_read");
        copyModelFacingNextActions(recovery, compactRecovery);
        target.put("recovery", compactRecovery);
    }
    
    private static void copyModelFacingNextActions(final Map<String, Object> source, final Map<String, Object> target) {
        List<Map<String, Object>> nextActions = LLMMCPJsonValues.castToList(source.get("next_actions"));
        if (nextActions.isEmpty()) {
            return;
        }
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : nextActions) {
            if (!LLMMCPSideEffectNextAction.isExecutionAction(each)) {
                result.add(each);
            }
        }
        if (!result.isEmpty()) {
            target.put("next_actions", result);
        }
    }
}
