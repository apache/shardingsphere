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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.exception.InvalidPageTokenException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MetadataSearchPaginator {

    private static final Map<String, Integer> OBJECT_TYPE_ORDERS = Map.of(
            "database", 0, "schema", 1, "table", 2, "view", 3, "column", 4, "index", 5, "sequence", 6);

    private static final String PAGE_TOKEN_PREFIX = "offset:";

    private final MetadataSearchMatcher matcher = new MetadataSearchMatcher();

    private final int defaultPageSize;

    private final int maxPageSize;

    MetadataSearchResult paginate(final List<MetadataSearchHit> metadataItems, final MetadataSearchRequest request,
                                  final Set<SupportedMCPMetadataObjectType> searchObjectTypes, final boolean broadSearchGuarded) {
        int actualOffset = resolvePageOffset(request.getPageToken());
        int actualPageSize = resolvePageSize(request.getPageSize());
        Map<String, Object> searchContext = createSearchContext(request, searchObjectTypes, actualPageSize, actualOffset, broadSearchGuarded);
        List<MetadataSearchHit> filteredItems = matcher.filterByQuery(metadataItems, request.getQuery());
        filteredItems.sort(this::compareSearchHits);
        if (actualOffset > filteredItems.size()) {
            return new MetadataSearchResult(Collections.emptyList(), "", searchContext, filteredItems.size(), filteredItems);
        }
        int actualEndIndex = Math.min(actualOffset + actualPageSize, filteredItems.size());
        String nextPageToken = actualEndIndex < filteredItems.size() ? encodePageToken(actualEndIndex) : "";
        return new MetadataSearchResult(new LinkedList<>(filteredItems.subList(actualOffset, actualEndIndex)), nextPageToken, searchContext, filteredItems.size(), filteredItems);
    }

    private int resolvePageOffset(final String pageToken) {
        if (pageToken.isEmpty()) {
            return 0;
        }
        if (pageToken.chars().allMatch(Character::isDigit)) {
            return parseLegacyPageOffset(pageToken);
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(pageToken), StandardCharsets.UTF_8);
            if (decoded.startsWith(PAGE_TOKEN_PREFIX)) {
                int result = Integer.parseInt(decoded.substring(PAGE_TOKEN_PREFIX.length()));
                if (0 <= result) {
                    return result;
                }
            }
        } catch (final IllegalArgumentException ignored) {
        }
        throw new InvalidPageTokenException();
    }

    private int parseLegacyPageOffset(final String pageToken) {
        try {
            int result = Integer.parseInt(pageToken);
            if (0 <= result) {
                return result;
            }
        } catch (final NumberFormatException ignored) {
        }
        throw new InvalidPageTokenException();
    }

    private String encodePageToken(final int offset) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((PAGE_TOKEN_PREFIX + offset).getBytes(StandardCharsets.UTF_8));
    }

    private int resolvePageSize(final int pageSize) {
        int result = 0 == pageSize ? defaultPageSize : pageSize;
        if (1 <= result && result <= maxPageSize) {
            return result;
        }
        throw new MCPInvalidRequestException(String.format("page_size must be an integer between 1 and %d.", maxPageSize));
    }

    private Map<String, Object> createSearchContext(final MetadataSearchRequest request, final Set<SupportedMCPMetadataObjectType> searchObjectTypes,
                                                    final int actualPageSize, final int actualOffset, final boolean broadSearchGuarded) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("query", request.getQuery());
        result.put("database", request.getDatabase());
        result.put("database_scope", request.getDatabase().isEmpty() ? "all_query_databases" : "single_database");
        result.put("schema", request.getSchema());
        result.put("object_types", createObjectTypeNames(searchObjectTypes));
        result.put("page_size", actualPageSize);
        if (broadSearchGuarded) {
            result.put("broad_search_guarded", true);
            result.put("guard_reason", "Blank cross-database metadata search lists databases only instead of expanding every object type.");
            result.put("recommended_narrowing_arguments", List.of("database", "query", "object_types"));
        }
        return result;
    }

    private List<String> createObjectTypeNames(final Set<SupportedMCPMetadataObjectType> searchObjectTypes) {
        return searchObjectTypes.stream().map(each -> each.name().toLowerCase(Locale.ENGLISH)).sorted(this::compareObjectTypeNames).collect(Collectors.toList());
    }

    private int compareObjectTypeNames(final String left, final String right) {
        return Integer.compare(getObjectTypeOrder(left), getObjectTypeOrder(right));
    }

    private int compareSearchHits(final MetadataSearchHit left, final MetadataSearchHit right) {
        int result = left.getDatabase().compareTo(right.getDatabase());
        if (0 != result) {
            return result;
        }
        result = left.getSchema().compareTo(right.getSchema());
        if (0 != result) {
            return result;
        }
        result = Integer.compare(getObjectTypeOrder(left.getObjectType()), getObjectTypeOrder(right.getObjectType()));
        if (0 != result) {
            return result;
        }
        result = left.getTable().compareTo(right.getTable());
        if (0 != result) {
            return result;
        }
        result = left.getView().compareTo(right.getView());
        if (0 != result) {
            return result;
        }
        return left.getName().compareTo(right.getName());
    }

    private int getObjectTypeOrder(final String objectType) {
        return OBJECT_TYPE_ORDERS.getOrDefault(objectType, Integer.MAX_VALUE);
    }
}
