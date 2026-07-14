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

package org.apache.shardingsphere.mcp.core.tool.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.List;
import java.util.Map;

/**
 * Search hit for metadata discovery.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
public final class MetadataSearchHit {
    
    private final String database;
    
    private final String schema;
    
    private final String objectType;
    
    private final String table;
    
    private final String view;
    
    private final String name;
    
    @JsonProperty(MCPPayloadFieldNames.RESOURCE)
    @JsonInclude(Include.NON_EMPTY)
    private final Map<String, Object> resource;
    
    @JsonProperty(MCPPayloadFieldNames.PARENT_RESOURCE)
    @JsonInclude(Include.NON_EMPTY)
    private final Map<String, Object> parentResource;
    
    @JsonProperty(MCPPayloadFieldNames.NEXT_RESOURCES)
    @JsonInclude(Include.NON_EMPTY)
    private final List<Map<String, Object>> nextResources;
    
    @JsonProperty("derivation_status")
    private final String derivationStatus;
    
    @JsonProperty("derivation_reason")
    @JsonInclude(Include.NON_EMPTY)
    private final String derivationReason;
    
    @JsonProperty("match_kind")
    private final String matchKind;
    
    @JsonProperty("matched_fields")
    @JsonInclude(Include.NON_EMPTY)
    private final List<String> matchedFields;
    
    @JsonProperty("matched_value")
    @JsonInclude(Include.NON_EMPTY)
    private final String matchedValue;
    
    /**
     * Create search hit with match explanation.
     *
     * @param matchKind match kind
     * @param matchedFields matched fields
     * @param matchedValue first matched value
     * @return search hit with match explanation
     */
    public MetadataSearchHit withMatch(final String matchKind, final List<String> matchedFields, final String matchedValue) {
        return toBuilder().matchKind(matchKind).matchedFields(matchedFields).matchedValue(matchedValue).build();
    }
}
