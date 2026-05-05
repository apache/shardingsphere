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

package org.apache.shardingsphere.mcp.bootstrap.transport.completion;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.api.completion.descriptor.MCPCompletionTargetDescriptor;
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
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
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
 * MCP completion specification factory.
 */
public final class MCPCompletionSpecificationFactory {
    
    private static final int DEFAULT_MAX_VALUES = 50;
    
    private static final Set<String> COMPLETION_ELIGIBLE_WORKFLOW_STATUSES = Set.of(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION,
            WorkflowLifecycle.STATUS_EXECUTED, WorkflowLifecycle.STATUS_FAILED, WorkflowLifecycle.STATUS_PLANNED, WorkflowLifecycle.STATUS_VALIDATED);
    
    private final MCPRuntimeContext runtimeContext;
    
    private final List<MCPCompletionTargetDescriptor> completionTargetDescriptors;
    
    public MCPCompletionSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        completionTargetDescriptors = List.copyOf(MCPDescriptorRegistry.getCompletionTargetDescriptors());
    }
    
    /**
     * Create MCP completion specifications.
     *
     * @return completion specifications
     */
    public List<SyncCompletionSpecification> createCompletionSpecifications() {
        return completionTargetDescriptors.stream().map(each -> new SyncCompletionSpecification(createReference(each), (exchange, request) -> handle(exchange, request, each))).toList();
    }
    
    private McpSchema.CompleteReference createReference(final MCPCompletionTargetDescriptor descriptor) {
        return "prompt".equals(descriptor.getReferenceType()) ? new McpSchema.PromptReference(descriptor.getReference()) : new McpSchema.ResourceReference(descriptor.getReference());
    }
    
    private McpSchema.CompleteResult handle(final McpSyncServerExchange exchange, final McpSchema.CompleteRequest request,
                                            final MCPCompletionTargetDescriptor descriptor) {
        String argumentName = request.argument().name();
        String prefix = Objects.toString(request.argument().value(), "");
        Map<String, String> contextArguments = null == request.context() || null == request.context().arguments() ? Map.of() : request.context().arguments();
        List<CompletionCandidate> candidates = complete(exchange.sessionId(), descriptor, argumentName, contextArguments);
        int maxValues = 0 == descriptor.getMaxValues() ? DEFAULT_MAX_VALUES : descriptor.getMaxValues();
        List<CompletionCandidate> filteredCandidates = candidates.stream().filter(each -> matchesPrefix(each.value(), prefix))
                .sorted(createCandidateComparator(prefix, argumentName)).toList();
        String matchStrategy = "prefix";
        if (filteredCandidates.isEmpty() && !prefix.isEmpty()) {
            filteredCandidates = candidates.stream().filter(each -> matchesContains(each.value(), prefix)).sorted(createCandidateComparator(prefix, argumentName)).toList();
            matchStrategy = "contains_fallback";
        }
        List<CompletionCandidate> returnedCandidates = filteredCandidates.stream().limit(maxValues).toList();
        return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(returnedCandidates.stream().map(CompletionCandidate::value).toList(),
                filteredCandidates.size(), filteredCandidates.size() > returnedCandidates.size()),
                createMeta(descriptor, argumentName, prefix, matchStrategy, contextArguments, candidates, filteredCandidates, returnedCandidates));
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
        if (!"plan_id".equals(argumentName)) {
            return 0;
        }
        return Comparator.nullsLast(Comparator.<Instant>reverseOrder()).compare(left.updateTime(), right.updateTime());
    }
    
    private boolean matchesPrefix(final String value, final String prefix) {
        return value.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    private boolean matchesContains(final String value, final String prefix) {
        return value.toLowerCase(Locale.ENGLISH).contains(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    private List<CompletionCandidate> complete(final String sessionId, final MCPCompletionTargetDescriptor descriptor, final String argumentName, final Map<String, String> contextArguments) {
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
        if (database.isEmpty()) {
            return List.of();
        }
        return requestScope.getMetadataQueryFacade().querySchemas(database).stream().map(MCPSchemaMetadata::getSchema)
                .map(each -> new CompletionCandidate(each, "schema", "metadata", null)).toList();
    }
    
    private List<CompletionCandidate> completeTables(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = contextArguments.getOrDefault("schema", "");
        if (database.isEmpty() || schema.isEmpty()) {
            return List.of();
        }
        return requestScope.getMetadataQueryFacade().queryTables(database, schema).stream().map(MCPTableMetadata::getTable)
                .map(each -> new CompletionCandidate(each, "logical table", "metadata", null)).toList();
    }
    
    private List<CompletionCandidate> completeColumns(final MCPRequestScope requestScope, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = contextArguments.getOrDefault("schema", "");
        String table = contextArguments.getOrDefault("table", "");
        if (database.isEmpty() || schema.isEmpty() || table.isEmpty()) {
            return List.of();
        }
        return requestScope.getMetadataQueryFacade().queryTableColumns(database, schema, table).stream().map(MCPColumnMetadata::getColumn)
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
        if (!(items instanceof List)) {
            return List.of();
        }
        return ((List<?>) items).stream().filter(each -> each instanceof Map)
                .map(each -> createAlgorithmCandidate((Map<?, ?>) each)).filter(each -> !each.value().isEmpty()).toList();
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
                                           final Map<String, String> contextArguments, final List<CompletionCandidate> candidates,
                                           final List<CompletionCandidate> filteredCandidates, final List<CompletionCandidate> returnedCandidates) {
        Map<String, Object> result = new LinkedHashMap<>(14, 1F);
        result.put("referenceType", descriptor.getReferenceType());
        result.put("reference", descriptor.getReference());
        result.put("argument", argumentName);
        result.put("prefix", prefix);
        result.put("matchStrategy", matchStrategy);
        result.put("contextArguments", contextArguments);
        result.put("candidateCount", candidates.size());
        result.put("matchedCandidateCount", filteredCandidates.size());
        result.put("returnedCandidateCount", returnedCandidates.size());
        List<String> missingContextArguments = createMissingContextArguments(argumentName, contextArguments);
        result.put("missingContextArguments", missingContextArguments);
        String diagnostic = createDiagnostic(candidates, filteredCandidates, missingContextArguments);
        result.put("diagnostic", diagnostic);
        List<Map<String, Object>> nextActions = createNextActions(argumentName, diagnostic, missingContextArguments);
        if (!nextActions.isEmpty()) {
            result.put("next_actions", nextActions);
        }
        result.put("rankingPolicy", List.of("exact-prefix-match", "contains-fallback-when-prefix-has-no-match", "recent-plan-first-for-plan_id", "case-insensitive-lexical"));
        result.put("valueDetails", returnedCandidates.stream().map(this::createValueDetail).toList());
        return result;
    }
    
    private List<Map<String, Object>> createNextActions(final String argumentName, final String diagnostic, final List<String> missingContextArguments) {
        if ("missing_context".equals(diagnostic)) {
            return List.of(MCPNextActionUtils.completeArgument(missingContextArguments.get(0), "Complete or provide the missing context argument before retrying this completion."));
        }
        if ("prefix_filtered_all_candidates".equals(diagnostic)) {
            return List.of(MCPNextActionUtils.completeArgument(argumentName, "Retry completion with a shorter or empty prefix."));
        }
        if ("no_candidates".equals(diagnostic)) {
            return List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP capabilities before choosing another argument source."));
        }
        return List.of();
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
        if (filteredCandidates.isEmpty()) {
            return "prefix_filtered_all_candidates";
        }
        return "ok";
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
    
    record CompletionCandidate(String value, String label, String source, Instant updateTime) {
    }
}
