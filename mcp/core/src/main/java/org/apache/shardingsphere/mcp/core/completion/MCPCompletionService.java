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

package org.apache.shardingsphere.mcp.core.completion;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.MCPResourceController;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplateUtils;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * MCP completion service.
 */
public final class MCPCompletionService {
    
    private static final int DEFAULT_MAX_VALUES = 50;
    
    private static final Set<String> COMPLETION_ELIGIBLE_WORKFLOW_STATUSES = Set.of(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION,
            WorkflowLifecycle.STATUS_EXECUTED, WorkflowLifecycle.STATUS_FAILED, WorkflowLifecycle.STATUS_PLANNED, WorkflowLifecycle.STATUS_VALIDATED);
    
    private final MCPRuntimeContext runtimeContext;
    
    public MCPCompletionService(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }
    
    /**
     * Complete one MCP argument.
     *
     * @param sessionId session id
     * @param descriptor completion target descriptor
     * @param argumentName argument name
     * @param prefix argument prefix
     * @param contextArguments context arguments
     * @return completion result
     */
    public MCPCompletionResult complete(final String sessionId, final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String prefix,
                                        final Map<String, String> contextArguments) {
        Map<String, Object> inferredContextArguments = applySingleSchemaDefault(argumentName, contextArguments);
        List<CompletionCandidate> candidates = completeCandidates(sessionId, descriptor, argumentName, contextArguments);
        int maxValues = 0 == descriptor.getMaxValues() ? DEFAULT_MAX_VALUES : descriptor.getMaxValues();
        List<CompletionCandidate> filteredCandidates = candidates.stream().filter(each -> matchesPrefix(each.value(), prefix)).sorted(createCandidateComparator(prefix, argumentName)).toList();
        String matchStrategy = "prefix";
        if (filteredCandidates.isEmpty() && !prefix.isEmpty()) {
            filteredCandidates = candidates.stream().filter(each -> matchesContains(each.value(), prefix)).sorted(createCandidateComparator(prefix, argumentName)).toList();
            matchStrategy = "contains_fallback";
        }
        List<CompletionCandidate> returnedCandidates = filteredCandidates.stream().limit(maxValues).toList();
        Map<String, Object> meta = createMeta(descriptor, argumentName, prefix, matchStrategy, contextArguments, inferredContextArguments, candidates, filteredCandidates, returnedCandidates);
        return new MCPCompletionResult(returnedCandidates.stream().map(CompletionCandidate::value).toList(), filteredCandidates.size(), filteredCandidates.size() > returnedCandidates.size(), meta);
    }
    
    private Map<String, Object> applySingleSchemaDefault(final String argumentName, final Map<String, String> contextArguments) {
        if (!requiresSchemaContext(argumentName) || !Objects.toString(contextArguments.get("schema"), "").isEmpty()
                || Objects.toString(contextArguments.get("database"), "").isEmpty()) {
            return Map.of();
        }
        try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
            return applySingleSchemaDefault(requestScope, contextArguments);
        }
    }
    
    private Map<String, Object> applySingleSchemaDefault(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        return requestScope.getMetadataQueryFacade().queryDatabase(contextArguments.get("database"))
                .filter(each -> 1 == each.getSchemas().size())
                .map(each -> applySingleSchemaDefault(contextArguments, each.getSchemas().iterator().next().getSchema()))
                .orElseGet(Map::of);
    }
    
    private Map<String, Object> applySingleSchemaDefault(final Map<String, String> contextArguments, final String schema) {
        contextArguments.put("schema", schema);
        return Map.of("schema", schema);
    }
    
    private boolean requiresSchemaContext(final String argumentName) {
        return "table".equals(argumentName) || "column".equals(argumentName) || "index".equals(argumentName) || "sequence".equals(argumentName);
    }
    
    private Comparator<CompletionCandidate> createCandidateComparator(final String prefix, final String argumentName) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ENGLISH);
        return Comparator.comparingInt((CompletionCandidate each) -> getExactMatchRank(each, normalizedPrefix))
                .thenComparing((left, right) -> comparePlanUpdateTime(left, right, argumentName))
                .thenComparing(each -> each.value().toLowerCase(Locale.ENGLISH))
                .thenComparing(CompletionCandidate::value);
    }
    
    private int getExactMatchRank(final CompletionCandidate candidate, final String normalizedPrefix) {
        return candidate.value().toLowerCase(Locale.ENGLISH).equals(normalizedPrefix) ? 0 : 1;
    }
    
    private int comparePlanUpdateTime(final CompletionCandidate left, final CompletionCandidate right, final String argumentName) {
        return "plan_id".equals(argumentName) ? Comparator.nullsLast(Comparator.<Instant>reverseOrder()).compare(left.updateTime(), right.updateTime()) : 0;
    }
    
    private boolean matchesPrefix(final String value, final String prefix) {
        return value.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    private boolean matchesContains(final String value, final String prefix) {
        return value.toLowerCase(Locale.ENGLISH).contains(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    private List<CompletionCandidate> completeCandidates(final String sessionId, final MCPCompletionTargetDescriptor descriptor, final String argumentName,
                                                         final Map<String, String> contextArguments) {
        if (isAlgorithmArgument(argumentName)) {
            return completeAlgorithms(descriptor, argumentName);
        }
        if ("plan_id".equals(argumentName)) {
            return completePlanIds(sessionId);
        }
        try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
            return completeMetadata(requestScope, argumentName, contextArguments);
        }
    }
    
    private boolean isAlgorithmArgument(final String argumentName) {
        return "algorithm_type".equals(argumentName) || "assisted_query_algorithm_type".equals(argumentName) || "like_query_algorithm_type".equals(argumentName);
    }
    
    private List<CompletionCandidate> completeMetadata(final MCPRequestScope requestScope, final String argumentName, final Map<String, String> contextArguments) {
        if ("database".equals(argumentName)) {
            return completeDatabases(requestScope);
        }
        if ("schema".equals(argumentName)) {
            return completeSchemas(requestScope, contextArguments);
        }
        if ("table".equals(argumentName)) {
            return completeTables(requestScope, contextArguments);
        }
        if ("column".equals(argumentName)) {
            return completeColumns(requestScope, contextArguments);
        }
        if ("index".equals(argumentName)) {
            return completeIndexes(requestScope, contextArguments);
        }
        if ("sequence".equals(argumentName)) {
            return completeSequences(requestScope, contextArguments);
        }
        return List.of();
    }
    
    private List<CompletionCandidate> completeDatabases(final MCPRequestScope requestScope) {
        return requestScope.getMetadataQueryFacade().queryDatabases().stream()
                .map(each -> new CompletionCandidate(each.getDatabase(), String.format("%s %s", each.getDatabaseType(), each.getDatabaseVersion()), "metadata", null)).toList();
    }
    
    private List<CompletionCandidate> completeSchemas(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        return database.isEmpty() ? List.of()
                : requestScope.getMetadataQueryFacade().querySchemas(database).stream().map(MCPSchemaMetadata::getSchema)
                        .map(each -> new CompletionCandidate(each, "schema", "metadata", null)).toList();
    }
    
    private List<CompletionCandidate> completeTables(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = contextArguments.getOrDefault("schema", "");
        return database.isEmpty() || schema.isEmpty() ? List.of()
                : requestScope.getMetadataQueryFacade().queryTables(database, schema).stream().map(MCPTableMetadata::getTable)
                        .map(each -> new CompletionCandidate(each, "logical table", "metadata", null)).toList();
    }
    
    private List<CompletionCandidate> completeColumns(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = contextArguments.getOrDefault("schema", "");
        String table = contextArguments.getOrDefault("table", "");
        return database.isEmpty() || schema.isEmpty() || table.isEmpty() ? List.of()
                : requestScope.getMetadataQueryFacade().queryTableColumns(database, schema, table).stream().map(MCPColumnMetadata::getColumn)
                        .map(each -> new CompletionCandidate(each, "column", "metadata", null)).toList();
    }
    
    private List<CompletionCandidate> completeIndexes(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = contextArguments.getOrDefault("schema", "");
        String table = contextArguments.getOrDefault("table", "");
        if (database.isEmpty() || schema.isEmpty() || table.isEmpty()) {
            return List.of();
        }
        try {
            return requestScope.getMetadataQueryFacade().queryIndexes(database, schema, table).stream().map(MCPIndexMetadata::getIndex)
                    .map(each -> new CompletionCandidate(each, "index", "metadata", null)).toList();
        } catch (final MCPUnsupportedException ignored) {
            return List.of();
        }
    }
    
    private List<CompletionCandidate> completeSequences(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = contextArguments.getOrDefault("schema", "");
        if (database.isEmpty() || schema.isEmpty()) {
            return List.of();
        }
        try {
            return requestScope.getMetadataQueryFacade().querySequences(database, schema).stream().map(MCPSequenceMetadata::getSequence)
                    .map(each -> new CompletionCandidate(each, "sequence", "metadata", null)).toList();
        } catch (final MCPUnsupportedException ignored) {
            return List.of();
        }
    }
    
    private List<CompletionCandidate> completeAlgorithms(final MCPCompletionTargetDescriptor descriptor, final String argumentName) {
        boolean encryptAlgorithm = descriptor.getReference().contains("encrypt") || !"algorithm_type".equals(argumentName);
        String resourceUri = encryptAlgorithm ? "shardingsphere://features/encrypt/algorithms" : "shardingsphere://features/mask/algorithms";
        MCPResponse response = new MCPResourceController(runtimeContext).handle(resourceUri);
        Object items = response.toPayload().get("items");
        return items instanceof List ? ((List<?>) items).stream().filter(each -> each instanceof Map)
                .map(each -> createAlgorithmCandidate((Map<?, ?>) each)).filter(each -> !each.value().isEmpty()).toList()
                : List.of();
    }
    
    private CompletionCandidate createAlgorithmCandidate(final Map<?, ?> row) {
        String value = Objects.toString(row.get("type"), "").trim();
        String label = Objects.toString(row.containsKey("description") ? row.get("description") : "algorithm", "algorithm");
        return new CompletionCandidate(value, label, "algorithm-resource", null);
    }
    
    private List<CompletionCandidate> completePlanIds(final String sessionId) {
        return runtimeContext.getWorkflowSessionContext().list(sessionId).stream().filter(this::isCompletionEligiblePlan)
                .map(each -> new CompletionCandidate(each.getPlanId(), String.format("%s %s", each.getWorkflowKind(), each.getStatus()), "workflow-session", each.getUpdateTime())).toList();
    }
    
    private boolean isCompletionEligiblePlan(final WorkflowContextSnapshot snapshot) {
        return COMPLETION_ELIGIBLE_WORKFLOW_STATUSES.contains(Objects.toString(snapshot.getStatus(), ""));
    }
    
    private Map<String, Object> createMeta(final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String prefix, final String matchStrategy,
                                           final Map<String, String> contextArguments, final Map<String, Object> inferredContextArguments, final List<CompletionCandidate> candidates,
                                           final List<CompletionCandidate> filteredCandidates, final List<CompletionCandidate> returnedCandidates) {
        Map<String, Object> result = new LinkedHashMap<>(14, 1F);
        result.put(MCPShardingSphereMetadataKeys.RESPONSE_MODE, MCPResponseMode.LIST);
        result.put(MCPShardingSphereMetadataKeys.REFERENCE_TYPE, descriptor.getReferenceType());
        result.put(MCPShardingSphereMetadataKeys.REFERENCE, descriptor.getReference());
        result.put(MCPShardingSphereMetadataKeys.ARGUMENT, argumentName);
        result.put(MCPShardingSphereMetadataKeys.PREFIX_ARGUMENT, prefix);
        result.put(MCPShardingSphereMetadataKeys.MATCH_STRATEGY, matchStrategy);
        result.put(MCPShardingSphereMetadataKeys.CONTEXT_ARGUMENTS, contextArguments);
        result.put(MCPShardingSphereMetadataKeys.CANDIDATE_COUNT, candidates.size());
        result.put(MCPShardingSphereMetadataKeys.MATCHED_CANDIDATE_COUNT, filteredCandidates.size());
        result.put(MCPShardingSphereMetadataKeys.RETURNED_CANDIDATE_COUNT, returnedCandidates.size());
        result.put(MCPShardingSphereMetadataKeys.CONTINUATION_MODE, filteredCandidates.size() > returnedCandidates.size() ? "pagination" : "none");
        putInferredContext(result, inferredContextArguments);
        List<String> missingContextArguments = createMissingContextArguments(argumentName, contextArguments);
        result.put(MCPShardingSphereMetadataKeys.MISSING_CONTEXT_ARGUMENTS, missingContextArguments);
        String diagnostic = createDiagnostic(candidates, filteredCandidates, missingContextArguments);
        result.put(MCPShardingSphereMetadataKeys.DIAGNOSTIC, diagnostic);
        if (!"ok".equals(diagnostic)) {
            result.put(MCPShardingSphereMetadataKeys.RECOVERY, createRecovery(argumentName, prefix, contextArguments, diagnostic, missingContextArguments));
        }
        List<Map<String, Object>> nextActions = createNextActions(descriptor, argumentName, prefix, contextArguments, diagnostic, missingContextArguments);
        if (!nextActions.isEmpty()) {
            result.put(MCPShardingSphereMetadataKeys.NEXT_ACTIONS, nextActions);
        }
        result.put(MCPShardingSphereMetadataKeys.RANKING_POLICY, List.of("exact-prefix-match", "contains-fallback-when-prefix-has-no-match", "recent-plan-first-for-plan_id",
                "case-insensitive-lexical"));
        result.put(MCPShardingSphereMetadataKeys.VALUE_DETAILS, returnedCandidates.stream().map(this::createValueDetail).toList());
        return result;
    }
    
    private void putInferredContext(final Map<String, Object> target, final Map<String, Object> inferredContextArguments) {
        if (!inferredContextArguments.isEmpty()) {
            target.put(MCPShardingSphereMetadataKeys.INFERRED_CONTEXT_ARGUMENTS, inferredContextArguments);
            target.put(MCPShardingSphereMetadataKeys.ARGUMENT_PROVENANCE, createArgumentProvenance(inferredContextArguments));
        }
    }
    
    private Map<String, Object> createArgumentProvenance(final Map<String, Object> inferredContextArguments) {
        Map<String, Object> result = new LinkedHashMap<>(inferredContextArguments.size(), 1F);
        for (String each : inferredContextArguments.keySet()) {
            result.put(each, "server_defaulted");
        }
        return result;
    }
    
    private List<Map<String, Object>> createNextActions(final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String prefix,
                                                        final Map<String, String> contextArguments, final String diagnostic, final List<String> missingContextArguments) {
        if ("missing_context".equals(diagnostic)) {
            String resourceUri = createNearestResourceUri(missingContextArguments.get(0), contextArguments);
            return !resourceUri.isEmpty()
                    ? List.of(MCPNextActionUtils.readResource(resourceUri, "Read the nearest metadata resource before retrying this completion."))
                    : List.of(createCompletionAction(descriptor, missingContextArguments.get(0), "", contextArguments, missingContextArguments,
                            "Complete or provide the missing context argument before retrying this completion."));
        }
        if ("prefix_filtered_all_candidates".equals(diagnostic)) {
            return List.of(createCompletionAction(descriptor, argumentName, prefix, contextArguments, List.of(), "Retry completion with a shorter or empty prefix."));
        }
        if ("no_candidates".equals(diagnostic)) {
            String resourceUri = createNearestResourceUri(argumentName, contextArguments);
            return List.of(MCPNextActionUtils.readResource(resourceUri.isEmpty() ? "shardingsphere://capabilities" : resourceUri,
                    "Read the nearest metadata resource before choosing another argument source."));
        }
        return List.of();
    }
    
    private Map<String, Object> createRecovery(final String argumentName, final String prefix, final Map<String, String> contextArguments,
                                               final String diagnostic, final List<String> missingContextArguments) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        String recoveryCategory = "missing_context".equals(diagnostic) ? "missing_context" : "empty_scope";
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put("recovery_category", recoveryCategory);
        result.put("category", recoveryCategory);
        result.put("diagnostic", diagnostic);
        if (!prefix.isEmpty()) {
            result.put("requested_token", prefix);
        }
        if (!missingContextArguments.isEmpty()) {
            result.put("missing_fields", missingContextArguments);
        }
        String resourceUri = createNearestResourceUri(missingContextArguments.isEmpty() ? argumentName : missingContextArguments.get(0), contextArguments);
        if (!resourceUri.isEmpty()) {
            result.put("parent_resource_uri", resourceUri);
        }
        return result;
    }
    
    private String createNearestResourceUri(final String argumentName, final Map<String, String> contextArguments) {
        String database = Objects.toString(contextArguments.get("database"), "");
        String schema = Objects.toString(contextArguments.get("schema"), "");
        if ("database".equals(argumentName)) {
            return "shardingsphere://databases";
        }
        if ("schema".equals(argumentName) && !database.isEmpty()) {
            return String.format("shardingsphere://databases/%s/schemas", encode(database));
        }
        if ("table".equals(argumentName) && !database.isEmpty() && !schema.isEmpty()) {
            return String.format("shardingsphere://databases/%s/schemas/%s/tables", encode(database), encode(schema));
        }
        if ("sequence".equals(argumentName) && !database.isEmpty() && !schema.isEmpty()) {
            return String.format("shardingsphere://databases/%s/schemas/%s/sequences", encode(database), encode(schema));
        }
        String table = Objects.toString(contextArguments.get("table"), "");
        if ("column".equals(argumentName) && !database.isEmpty() && !schema.isEmpty() && !table.isEmpty()) {
            return String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/columns", encode(database), encode(schema), encode(table));
        }
        if ("index".equals(argumentName) && !database.isEmpty() && !schema.isEmpty() && !table.isEmpty()) {
            return String.format("shardingsphere://databases/%s/schemas/%s/tables/%s/indexes", encode(database), encode(schema), encode(table));
        }
        return "";
    }
    
    private String encode(final String value) {
        return MCPUriTemplateUtils.encodePathSegment(value);
    }
    
    private Map<String, Object> createCompletionAction(final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String argumentPrefix,
                                                       final Map<String, String> contextArguments, final List<String> missingContextArguments, final String reason) {
        return MCPNextActionUtils.completeArgument(descriptor.getReferenceType(), descriptor.getReference(), argumentName, argumentPrefix, contextArguments, missingContextArguments,
                descriptor.getReferenceType(), descriptor.getReference(), contextArguments, reason);
    }
    
    private List<String> createMissingContextArguments(final String argumentName, final Map<String, String> contextArguments) {
        if ("schema".equals(argumentName)) {
            return createMissingArguments(contextArguments, "database");
        }
        if ("table".equals(argumentName) || "sequence".equals(argumentName)) {
            return createMissingArguments(contextArguments, "database", "schema");
        }
        if ("column".equals(argumentName) || "index".equals(argumentName)) {
            return createMissingArguments(contextArguments, "database", "schema", "table");
        }
        return List.of();
    }
    
    private List<String> createMissingArguments(final Map<String, String> contextArguments, final String... requiredArguments) {
        return Stream.of(requiredArguments).filter(each -> Objects.toString(contextArguments.get(each), "").isEmpty()).toList();
    }
    
    private String createDiagnostic(final List<CompletionCandidate> candidates, final List<CompletionCandidate> filteredCandidates,
                                    final List<String> missingContextArguments) {
        if (!missingContextArguments.isEmpty()) {
            return "missing_context";
        }
        if (candidates.isEmpty()) {
            return "no_candidates";
        }
        return filteredCandidates.isEmpty() ? "prefix_filtered_all_candidates" : "ok";
    }
    
    private Map<String, Object> createValueDetail(final CompletionCandidate candidate) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("value", candidate.value());
        result.put("label", candidate.label());
        result.put("source", candidate.source());
        if (null != candidate.updateTime()) {
            result.put("updateTime", candidate.updateTime().toString());
            result.put("rankingReason", "recent-plan-first-for-plan_id");
        } else {
            result.put("rankingReason", "exact-prefix-match-then-lexical");
        }
        return result;
    }
    
    private record CompletionCandidate(String value, String label, String source, Instant updateTime) {
    }
}
