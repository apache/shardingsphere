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

import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchHit;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

final class MetadataSearchMatcher {
    
    List<MetadataSearchHit> filterByQuery(final List<MetadataSearchHit> metadataItems, final String query) {
        List<MetadataSearchHit> result = new LinkedList<>();
        for (MetadataSearchHit each : metadataItems) {
            SearchMatch searchMatch = createSearchMatch(each, query);
            if (!"none".equals(searchMatch.matchKind())) {
                result.add(each.withMatch(searchMatch.matchKind(), searchMatch.matchedFields(), searchMatch.matchedValue()));
            }
        }
        return result;
    }
    
    private SearchMatch createSearchMatch(final MetadataSearchHit searchHit, final String query) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ENGLISH);
        if (normalizedQuery.isEmpty()) {
            return createMatchCandidates(searchHit).stream().anyMatch(each -> !each.value().isEmpty())
                    ? new SearchMatch("all", List.of(), "")
                    : new SearchMatch("none", List.of(), "");
        }
        for (String each : List.of("exact", "prefix", "contains")) {
            SearchMatch result = createSearchMatch(searchHit, normalizedQuery, each);
            if (!"none".equals(result.matchKind())) {
                return result;
            }
        }
        return new SearchMatch("none", List.of(), "");
    }
    
    private SearchMatch createSearchMatch(final MetadataSearchHit searchHit, final String normalizedQuery, final String matchKind) {
        List<String> matchedFields = new LinkedList<>();
        String matchedValue = "";
        for (MatchCandidate each : createMatchCandidates(searchHit)) {
            if (matchesValue(each.value(), normalizedQuery, matchKind)) {
                matchedFields.add(each.field());
                if (matchedValue.isEmpty()) {
                    matchedValue = each.value();
                }
            }
        }
        return matchedFields.isEmpty() ? new SearchMatch("none", List.of(), "") : new SearchMatch(matchKind, matchedFields, matchedValue);
    }
    
    private List<MatchCandidate> createMatchCandidates(final MetadataSearchHit searchHit) {
        List<MatchCandidate> result = new LinkedList<>();
        result.add(new MatchCandidate("name", searchHit.getName()));
        if (!searchHit.getTable().isEmpty()) {
            result.add(new MatchCandidate("table", searchHit.getTable()));
        }
        if (!searchHit.getView().isEmpty()) {
            result.add(new MatchCandidate("view", searchHit.getView()));
        }
        return result;
    }
    
    private boolean matchesValue(final String value, final String normalizedQuery, final String matchKind) {
        if (null == value || value.isEmpty()) {
            return false;
        }
        String normalizedValue = value.toLowerCase(Locale.ENGLISH);
        if ("exact".equals(matchKind)) {
            return normalizedValue.equals(normalizedQuery);
        }
        return "prefix".equals(matchKind) ? normalizedValue.startsWith(normalizedQuery) : normalizedValue.contains(normalizedQuery);
    }
    
    private record MatchCandidate(String field, String value) {
    }
    
    private record SearchMatch(String matchKind, List<String> matchedFields, String matchedValue) {
    }
}
