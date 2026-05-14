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

import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.handler.MCPHandlerContexts;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplateUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MCP completion service.
 */
public final class MCPCompletionService {
    
    private static final int DEFAULT_MAX_VALUES = 50;
    
    private static final int MAX_VALUES_LIMIT = 100;
    
    private final MCPRuntimeContext runtimeContext;
    
    private final Collection<MCPCompletionProvider<?>> completionProviders;
    
    public MCPCompletionService(final MCPRuntimeContext runtimeContext) {
        this(runtimeContext, MCPCompletionProviderLoader.load());
    }
    
    MCPCompletionService(final MCPRuntimeContext runtimeContext, final Collection<MCPCompletionProvider<?>> completionProviders) {
        this.runtimeContext = runtimeContext;
        this.completionProviders = completionProviders;
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
        Map<String, String> actualContextArguments = new LinkedHashMap<>(contextArguments);
        MCPCompletionProviderResult providerResult = completeCandidates(sessionId, descriptor, argumentName, actualContextArguments);
        Map<String, Object> inferredContextArguments = providerResult.getInferredContextArguments();
        mergeInferredContextArguments(actualContextArguments, inferredContextArguments);
        List<MCPCompletionCandidate> candidates = List.copyOf(providerResult.getCandidates());
        int maxValues = Math.min(MAX_VALUES_LIMIT, 0 == descriptor.getMaxValues() ? DEFAULT_MAX_VALUES : descriptor.getMaxValues());
        List<MCPCompletionCandidate> filteredCandidates = candidates.stream().filter(each -> matchesPrefix(each.getValue(), prefix)).sorted(createCandidateComparator(prefix, argumentName)).toList();
        String matchStrategy = "prefix";
        if (filteredCandidates.isEmpty() && !prefix.isEmpty()) {
            filteredCandidates = candidates.stream().filter(each -> matchesContains(each.getValue(), prefix)).sorted(createCandidateComparator(prefix, argumentName)).toList();
            matchStrategy = "contains_fallback";
        }
        List<MCPCompletionCandidate> returnedCandidates = filteredCandidates.stream().limit(maxValues).toList();
        Map<String, Object> meta = createMeta(descriptor, argumentName, prefix, matchStrategy, actualContextArguments, inferredContextArguments, candidates, filteredCandidates, returnedCandidates);
        return new MCPCompletionResult(returnedCandidates.stream().map(MCPCompletionCandidate::getValue).toList(), filteredCandidates.size(), filteredCandidates.size() > returnedCandidates.size(),
                meta);
    }
    
    private void mergeInferredContextArguments(final Map<String, String> contextArguments, final Map<String, Object> inferredContextArguments) {
        for (Entry<String, Object> entry : inferredContextArguments.entrySet()) {
            if (Objects.toString(contextArguments.get(entry.getKey()), "").isEmpty()) {
                contextArguments.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
            }
        }
    }
    
    private Comparator<MCPCompletionCandidate> createCandidateComparator(final String prefix, final String argumentName) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ENGLISH);
        return Comparator.comparingInt((MCPCompletionCandidate each) -> getExactMatchRank(each, normalizedPrefix))
                .thenComparing((left, right) -> comparePlanUpdateTime(left, right, argumentName))
                .thenComparing(each -> each.getValue().toLowerCase(Locale.ENGLISH))
                .thenComparing(MCPCompletionCandidate::getValue);
    }
    
    private int getExactMatchRank(final MCPCompletionCandidate candidate, final String normalizedPrefix) {
        return candidate.getValue().toLowerCase(Locale.ENGLISH).equals(normalizedPrefix) ? 0 : 1;
    }
    
    private int comparePlanUpdateTime(final MCPCompletionCandidate left, final MCPCompletionCandidate right, final String argumentName) {
        return "plan_id".equals(argumentName) ? Comparator.nullsLast(Comparator.<Instant>reverseOrder()).compare(left.getUpdateTime(), right.getUpdateTime()) : 0;
    }
    
    private boolean matchesPrefix(final String value, final String prefix) {
        return value.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    private boolean matchesContains(final String value, final String prefix) {
        return value.toLowerCase(Locale.ENGLISH).contains(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    private MCPCompletionProviderResult completeCandidates(final String sessionId, final MCPCompletionTargetDescriptor descriptor, final String argumentName,
                                                           final Map<String, String> contextArguments) {
        MCPCompletionRequestContext requestContext = new MCPCompletionRequestContext(sessionId, descriptor, argumentName, contextArguments);
        for (MCPCompletionProvider<?> each : completionProviders) {
            if (each.supports(requestContext)) {
                try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
                    return completeCandidates(requestScope, each, requestContext);
                }
            }
        }
        return MCPCompletionProviderResult.empty();
    }
    
    private <T extends MCPHandlerContext> MCPCompletionProviderResult completeCandidates(final MCPRequestScope requestScope, final MCPCompletionProvider<T> provider,
                                                                                         final MCPCompletionRequestContext requestContext) {
        return provider.complete(MCPHandlerContexts.resolve(requestScope, provider.getContextType(), provider.getClass()), requestContext);
    }
    
    private Map<String, Object> createMeta(final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String prefix, final String matchStrategy,
                                           final Map<String, String> contextArguments, final Map<String, Object> inferredContextArguments, final List<MCPCompletionCandidate> candidates,
                                           final List<MCPCompletionCandidate> filteredCandidates, final List<MCPCompletionCandidate> returnedCandidates) {
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
    
    private String createDiagnostic(final List<MCPCompletionCandidate> candidates, final List<MCPCompletionCandidate> filteredCandidates,
                                    final List<String> missingContextArguments) {
        if (!missingContextArguments.isEmpty()) {
            return "missing_context";
        }
        if (candidates.isEmpty()) {
            return "no_candidates";
        }
        return filteredCandidates.isEmpty() ? "prefix_filtered_all_candidates" : "ok";
    }
    
    private Map<String, Object> createValueDetail(final MCPCompletionCandidate candidate) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("value", candidate.getValue());
        result.put("label", candidate.getLabel());
        result.put("source", candidate.getSource());
        if (null != candidate.getUpdateTime()) {
            result.put("updateTime", candidate.getUpdateTime().toString());
            result.put("rankingReason", "recent-plan-first-for-plan_id");
        } else {
            result.put("rankingReason", "exact-prefix-match-then-lexical");
        }
        return result;
    }
}
