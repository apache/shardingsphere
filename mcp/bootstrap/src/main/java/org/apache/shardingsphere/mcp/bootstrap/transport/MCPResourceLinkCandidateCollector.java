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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class MCPResourceLinkCandidateCollector {
    
    private static final String RESOURCES_TO_READ_FIELD = "resources_to_read";
    
    private static final String RESOURCE_FIELD = "resource";
    
    private static final String PARENT_RESOURCE_FIELD = "parent_resource";
    
    private static final String NEXT_RESOURCES_FIELD = "next_resources";
    
    private static final String RECOVERY_FIELD = "recovery";
    
    private static final String ITEMS_FIELD = "items";
    
    private static final String URI_FIELD = "uri";
    
    private static final String RESOURCE_KIND_FIELD = "resource_kind";
    
    private static final String SOURCE_FIELD = "source_field";
    
    private static final String REASON_FIELD = "reason";
    
    private static final String PURPOSE_FIELD = "purpose";
    
    private static final String DEFAULT_DESCRIPTION = "Read this ShardingSphere MCP resource.";
    
    private final int limit;
    
    MCPResourceLinkCandidateCollector(final int limit) {
        this.limit = limit;
    }
    
    ResourceLinkCandidates collect(final Map<String, Object> payload) {
        List<OrderedResourceLinkCandidate> candidates = new LinkedList<>();
        collectResourceLinkCandidates(payload, candidates);
        candidates.sort(MCPResourceLinkCandidateCollector::compareResourceLinkCandidates);
        Map<String, OrderedResourceLinkCandidate> uniqueCandidates = deduplicateCandidates(candidates);
        return new ResourceLinkCandidates(createLimitedCandidates(uniqueCandidates), uniqueCandidates.size());
    }
    
    private void collectResourceLinkCandidates(final Map<?, ?> payload, final List<OrderedResourceLinkCandidate> result) {
        collectResourceLinkFields(payload, result);
        collectRecoveryResourceLinkFields(payload, result);
        collectItemResourceLinkFields(payload, result);
    }
    
    private void collectResourceLinkFields(final Map<?, ?> value, final List<OrderedResourceLinkCandidate> result) {
        collectResourceLinkValue(value.get(RESOURCES_TO_READ_FIELD), RESOURCES_TO_READ_FIELD, result);
        collectResourceLinkValue(value.get(RESOURCE_FIELD), RESOURCE_FIELD, result);
        collectResourceLinkValue(value.get(PARENT_RESOURCE_FIELD), PARENT_RESOURCE_FIELD, result);
        collectResourceLinkValue(value.get(NEXT_RESOURCES_FIELD), NEXT_RESOURCES_FIELD, result);
    }
    
    private void collectResourceLinkValue(final Object value, final String sourceField, final List<OrderedResourceLinkCandidate> result) {
        if (value instanceof Map<?, ?> resourceLinkValue) {
            collectResourceLinkHint(resourceLinkValue, sourceField, result);
        } else if (value instanceof Iterable<?> resourceLinkValues) {
            collectIterableResourceLinkHints(resourceLinkValues, sourceField, result);
        }
    }
    
    private void collectResourceLinkHint(final Map<?, ?> value, final String sourceField, final List<OrderedResourceLinkCandidate> result) {
        if (isResourceHint(value)) {
            result.add(createCandidate(value, sourceField, result.size()));
        }
    }
    
    private boolean isResourceHint(final Map<?, ?> value) {
        return value.containsKey(URI_FIELD) && value.containsKey(RESOURCE_KIND_FIELD) && !Objects.toString(value.get(URI_FIELD), "").isBlank();
    }
    
    private OrderedResourceLinkCandidate createCandidate(final Map<?, ?> value, final String sourceField, final int order) {
        return new OrderedResourceLinkCandidate(
                Objects.toString(value.get(URI_FIELD), ""),
                Objects.toString(value.get(RESOURCE_KIND_FIELD), "resource"),
                Objects.toString(value.get(REASON_FIELD), DEFAULT_DESCRIPTION),
                Objects.toString(value.get(RESOURCE_KIND_FIELD), ""),
                Objects.toString(value.get(PURPOSE_FIELD), ""),
                resolveSourceField(value, sourceField),
                order);
    }
    
    private String resolveSourceField(final Map<?, ?> value, final String sourceField) {
        String result = Objects.toString(value.get(SOURCE_FIELD), "");
        return result.isEmpty() ? sourceField : result;
    }
    
    private void collectIterableResourceLinkHints(final Iterable<?> value, final String sourceField, final List<OrderedResourceLinkCandidate> result) {
        for (Object each : value) {
            if (each instanceof Map<?, ?> resourceLinkValue) {
                collectResourceLinkHint(resourceLinkValue, sourceField, result);
            }
        }
    }
    
    private void collectRecoveryResourceLinkFields(final Map<?, ?> payload, final List<OrderedResourceLinkCandidate> result) {
        Object recovery = payload.get(RECOVERY_FIELD);
        if (recovery instanceof Map<?, ?> recoveryPayload) {
            collectResourceLinkFields(recoveryPayload, result);
        }
    }
    
    private void collectItemResourceLinkFields(final Map<?, ?> payload, final List<OrderedResourceLinkCandidate> result) {
        Object items = payload.get(ITEMS_FIELD);
        if (!(items instanceof Iterable<?> itemValues)) {
            return;
        }
        for (Object each : itemValues) {
            if (each instanceof Map<?, ?> item) {
                collectResourceLinkFields(item, result);
            }
        }
    }
    
    private static int compareResourceLinkCandidates(final OrderedResourceLinkCandidate left, final OrderedResourceLinkCandidate right) {
        int result = Integer.compare(resolveResourceLinkPriority(left.sourceField()), resolveResourceLinkPriority(right.sourceField()));
        return 0 == result ? Integer.compare(left.order(), right.order()) : result;
    }
    
    private static int resolveResourceLinkPriority(final String sourceField) {
        if (RESOURCES_TO_READ_FIELD.equals(sourceField)) {
            return 0;
        }
        if (RESOURCE_FIELD.equals(sourceField)) {
            return 1;
        }
        if (PARENT_RESOURCE_FIELD.equals(sourceField)) {
            return 2;
        }
        return NEXT_RESOURCES_FIELD.equals(sourceField) ? 3 : 4;
    }
    
    private Map<String, OrderedResourceLinkCandidate> deduplicateCandidates(final List<OrderedResourceLinkCandidate> candidates) {
        Map<String, OrderedResourceLinkCandidate> result = new LinkedHashMap<>(candidates.size(), 1F);
        for (OrderedResourceLinkCandidate each : candidates) {
            result.putIfAbsent(each.uri(), each);
        }
        return result;
    }
    
    private List<ResourceLinkCandidate> createLimitedCandidates(final Map<String, OrderedResourceLinkCandidate> candidates) {
        List<ResourceLinkCandidate> result = new LinkedList<>();
        for (OrderedResourceLinkCandidate each : candidates.values()) {
            if (result.size() >= limit) {
                break;
            }
            result.add(new ResourceLinkCandidate(each.uri(), each.title(), each.description(), each.resourceKind(), each.purpose(), each.sourceField()));
        }
        return result;
    }
    
    record ResourceLinkCandidates(List<ResourceLinkCandidate> candidates, int totalCount) {
    }
    
    record ResourceLinkCandidate(String uri, String title, String description, String resourceKind, String purpose, String sourceField) {
    }
    
    private record OrderedResourceLinkCandidate(String uri, String title, String description, String resourceKind, String purpose, String sourceField, int order) {
    }
}
